package com.market.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.domain.model.ShoppingItem
import com.market.domain.model.Store
import com.market.domain.usecase.item.ObserveItemsUseCase
import com.market.domain.usecase.price.GetPriceSummaryUseCase
import com.market.domain.usecase.price.PriceSummary
import com.market.domain.usecase.price.RecordPriceUseCase
import com.market.domain.usecase.store.GetStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemPriceRow(
    val item: ShoppingItem,
    val summary: PriceSummary?,
    val cheapestStoreName: String?
)

data class PriceComparisonUiState(
    val items: List<ItemPriceRow> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PriceComparisonViewModel @Inject constructor(
    private val observeItemsUseCase: ObserveItemsUseCase,
    private val getStoresUseCase: GetStoresUseCase,
    private val getPriceSummaryUseCase: GetPriceSummaryUseCase,
    private val recordPriceUseCase: RecordPriceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceComparisonUiState())
    val uiState: StateFlow<PriceComparisonUiState> = _uiState

    private val _householdId = MutableStateFlow<String?>(null)

    private var _items: List<ShoppingItem> = emptyList()
    private var _stores: List<Store> = emptyList()
    private var _summaries: Map<String, PriceSummary?> = emptyMap()

    fun setHouseholdId(householdId: String) {
        if (_householdId.value == householdId) return
        _householdId.value = householdId
        observeData(householdId)
    }

    private fun observeData(householdId: String) {
        viewModelScope.launch {
            observeItemsUseCase(householdId).collect { items ->
                _items = items
                emitState()
            }
        }
        viewModelScope.launch {
            getStoresUseCase(householdId).collect { stores ->
                _stores = stores
                emitState()
            }
        }
        viewModelScope.launch {
            // Observe summaries for each item as prices change
            _items.forEach { item ->
                getPriceSummaryUseCase(householdId, item.id).collect { summary ->
                    _summaries = _summaries + (item.id to summary)
                    emitState()
                }
            }
        }
    }

    private fun emitState() {
        val storeMap = _stores.associateBy { it.id }
        val rows = _items.map { item ->
            val summary = _summaries[item.id]
            val cheapestStoreName = summary?.cheapestStoreId?.let { storeMap[it]?.name }
            ItemPriceRow(
                item = item,
                summary = summary,
                cheapestStoreName = cheapestStoreName
            )
        }
        _uiState.value = PriceComparisonUiState(
            items = rows,
            isLoading = false
        )
    }

    fun recordPrice(itemId: String, storeId: String, amount: Double) {
        val hid = _householdId.value ?: return
        viewModelScope.launch {
            recordPriceUseCase(hid, itemId, storeId, amount).onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
