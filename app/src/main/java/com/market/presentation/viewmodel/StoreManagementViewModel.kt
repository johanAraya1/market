package com.market.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.domain.model.Store
import com.market.domain.usecase.store.AddStoreUseCase
import com.market.domain.usecase.store.DeleteStoreUseCase
import com.market.domain.usecase.store.EditStoreUseCase
import com.market.domain.usecase.store.GetStoresUseCase
import com.market.domain.usecase.store.ReorderStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoreManagementUiState(
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class StoreManagementViewModel @Inject constructor(
    private val getStoresUseCase: GetStoresUseCase,
    private val addStoreUseCase: AddStoreUseCase,
    private val editStoreUseCase: EditStoreUseCase,
    private val deleteStoreUseCase: DeleteStoreUseCase,
    private val reorderStoresUseCase: ReorderStoresUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreManagementUiState())
    val uiState: StateFlow<StoreManagementUiState> = _uiState

    private val _householdId = MutableStateFlow<String?>(null)

    fun setHouseholdId(householdId: String) {
        if (_householdId.value == householdId) return
        _householdId.value = householdId
        observeStores(householdId)
    }

    private fun observeStores(householdId: String) {
        viewModelScope.launch {
            getStoresUseCase(householdId).collect { stores ->
                _uiState.value = StoreManagementUiState(
                    stores = stores,
                    isLoading = false
                )
            }
        }
    }

    fun addStore(name: String) {
        val hid = _householdId.value ?: return
        viewModelScope.launch {
            addStoreUseCase(hid, name).onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }

    fun editStore(store: Store, newName: String) {
        viewModelScope.launch {
            editStoreUseCase(store, newName).onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }

    fun deleteStore(storeId: String) {
        val hid = _householdId.value ?: return
        viewModelScope.launch {
            deleteStoreUseCase(hid, storeId).onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }

    fun reorderStores(orderedIds: List<String>) {
        val hid = _householdId.value ?: return
        viewModelScope.launch {
            reorderStoresUseCase(hid, orderedIds).onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
