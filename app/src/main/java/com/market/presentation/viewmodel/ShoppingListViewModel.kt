package com.market.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.data.remote.AuthDataSource
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListUiState(
    val itemsByStore: Map<String?, List<ShoppingItem>> = emptyMap(),
    val stores: List<Store> = emptyList(),
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

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private var householdId: String? = null

    fun setHouseholdId(hid: String) {
        if (householdId == hid) return
        householdId = hid
        observeData(hid)
    }

    private fun observeData(hid: String) {
        viewModelScope.launch {
            observeItemsUseCase(hid).collect { items ->
                _uiState.update { state ->
                    state.copy(
                        itemsByStore = items.groupBy { it.storeId },
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            getStoresUseCase(hid).collect { stores ->
                _uiState.update { state ->
                    state.copy(stores = stores)
                }
            }
        }
    }

    fun addItem(name: String, storeId: String? = null) {
        val hid = householdId ?: return
        viewModelScope.launch {
            addItemUseCase(hid, name, storeId).onFailure {
                _uiState.update { s -> s.copy(error = it.message) }
            }
        }
    }

    fun editItem(item: ShoppingItem, newName: String) {
        viewModelScope.launch {
            editItemUseCase(item, newName).onFailure {
                _uiState.update { s -> s.copy(error = it.message) }
            }
        }
    }

    fun deleteItem(itemId: String) {
        val hid = householdId ?: return
        viewModelScope.launch {
            deleteItemUseCase(hid, itemId).onFailure {
                _uiState.update { s -> s.copy(error = it.message) }
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
                _uiState.update { s -> s.copy(error = it.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { s -> s.copy(error = null) }
    }

    fun completeTrip(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val hid = householdId ?: return
        val state = _uiState.value
        val allItems = state.itemsByStore.values.flatten()
        val checkedItems = allItems.filter { it.isChecked }

        if (checkedItems.isEmpty()) {
            onError("No hay items marcados para registrar")
            return
        }

        val storeMap = state.stores.associateBy { it.id }
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
