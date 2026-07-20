package com.market.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.domain.model.Trip
import com.market.domain.usecase.trip.GetTripsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseHistoryUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PurchaseHistoryViewModel @Inject constructor(
    private val getTripsUseCase: GetTripsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseHistoryUiState())
    val uiState: StateFlow<PurchaseHistoryUiState> = _uiState

    private var _householdId: String? = null

    fun setHouseholdId(householdId: String) {
        if (_householdId == householdId) return
        _householdId = householdId
        observeTrips(householdId)
    }

    private fun observeTrips(householdId: String) {
        viewModelScope.launch {
            try {
                getTripsUseCase(householdId).collect { trips ->
                    _uiState.value = PurchaseHistoryUiState(
                        trips = trips,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PurchaseHistoryUiState(
                    isLoading = false,
                    error = "Error al cargar el historial: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
