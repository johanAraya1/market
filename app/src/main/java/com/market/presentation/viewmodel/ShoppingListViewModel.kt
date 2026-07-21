package com.market.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.data.remote.AuthDataSource
import com.market.domain.model.MemberRole
import com.market.domain.model.ShoppingItem
import com.market.domain.model.Trip
import com.market.domain.model.TripItem
import com.market.domain.usecase.item.AddItemUseCase
import com.market.domain.usecase.item.DeleteItemUseCase
import com.market.domain.usecase.item.EditItemUseCase
import com.market.domain.usecase.item.ObserveItemsUseCase
import com.market.domain.usecase.store.GetStoresUseCase
import com.market.domain.usecase.trip.CompleteTripUseCase
import com.market.domain.model.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListUiState(
    val itemsByStore: Map<String?, List<ShoppingItem>> = emptyMap(),
    val stores: List<Store> = emptyMap().let { emptyList<Store>() },
    val isLoading: Boolean = true,
    val error: String? = null,
    val isAdmin: Boolean = false
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val observeItemsUseCase: ObserveItemsUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val editItemUseCase: EditItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val completeTripUseCase: CompleteTripUseCase,
    private val authDataSource: AuthDataSource
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _householdId = MutableStateFlow<String?>(null)
    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    private val _stores = MutableStateFlow<List<Store>>(emptyList())
    private val _isAdmin = MutableStateFlow(false)

    val uiState: StateFlow<ShoppingListUiState> = combine(
        _householdId,
        _error,
        _items,
        _stores,
        _isAdmin
    ) { args: Array<Any?> ->
        val hid = args[0] as String?
        val error = args[1] as String?
        val items = args[2] as List<ShoppingItem>
        val stores = args[3] as List<Store>
        val isAdmin = args[4] as Boolean
        ShoppingListUiState(
            itemsByStore = items.groupBy { it.storeId },
            stores = stores,
            isLoading = false,
            error = error,
            isAdmin = isAdmin
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ShoppingListUiState()
    )

    fun setHouseholdId(householdId: String) {
        if (_householdId.value == householdId) return
        _householdId.value = householdId
        observeData(householdId)
    }

    private fun observeData(householdId: String) {
        viewModelScope.launch {
            observeItemsUseCase(householdId).collect { items ->
                _items.value = items
            }
        }
        viewModelScope.launch {
            getStoresUseCase(householdId).collect { stores ->
                _stores.value = stores
            }
        }
    }

    fun addItem(name: String, storeId: String? = null) {
        val hid = _householdId.value ?: return
        viewModelScope.launch {
            addItemUseCase(hid, name, storeId).onFailure {
                _error.value = it.message
            }
        }
    }

    fun editItem(item: ShoppingItem, newName: String) {
        viewModelScope.launch {
            editItemUseCase(item, newName).onFailure {
                _error.value = it.message
            }
        }
    }

    fun deleteItem(itemId: String) {
        val hid = _householdId.value ?: return
        viewModelScope.launch {
            deleteItemUseCase(hid, itemId).onFailure {
                _error.value = it.message
            }
        }
    }

    fun toggleCheck(item: ShoppingItem, reason: String? = null) {
        viewModelScope.launch {
            val firebaseUser = authDataSource.getCurrentFirebaseUser()
            val updated = item.copy(
                isChecked = !item.isChecked,
                checkedBy = if (!item.isChecked) firebaseUser?.uid else null,
                checkedAt = if (!item.isChecked) System.currentTimeMillis() else null,
                checkReason = if (!item.isChecked) reason else null,
                updatedAt = System.currentTimeMillis()
            )
            editItemUseCase(updated, updated.name).onFailure {
                _error.value = it.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun completeTrip(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val hid = _householdId.value ?: return
        val checkedItems = _items.value.filter { it.isChecked }

        if (checkedItems.isEmpty()) {
            onError("No hay items marcados para registrar")
            return
        }

        val storeMap = _stores.value.associateBy { it.id }
        val firebaseUser = authDataSource.getCurrentFirebaseUser()

        val tripItems = checkedItems.map { item ->
            TripItem(
                name = item.name,
                storeId = item.storeId,
                storeName = item.storeId?.let { storeMap[it]?.name },
                price = item.price,
                quantity = 1
            )
        }

        val totalEstimated = tripItems.sumOf { (it.price ?: 0.0) * it.quantity }

        val trip = Trip(
            householdId = hid,
            completedBy = firebaseUser?.uid ?: "",
            completedByName = firebaseUser?.displayName ?: "",
            totalEstimated = totalEstimated,
            items = tripItems
        )

        viewModelScope.launch {
            completeTripUseCase(trip)
                .onSuccess {
                    // Uncheck all items that were part of the trip
                    checkedItems.forEach { item ->
                        val unchecked = item.copy(
                            isChecked = false,
                            checkedBy = null,
                            checkedAt = null,
                            checkReason = null,
                            updatedAt = System.currentTimeMillis()
                        )
                        editItemUseCase(unchecked, unchecked.name)
                    }
                    onSuccess()
                }
                .onFailure { e ->
                    onError(e.message ?: "Error al registrar la compra")
                }
        }
    }
}
