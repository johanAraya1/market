package com.market.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.domain.model.Trip
import com.market.domain.usecase.trip.DeleteTripUseCase
import com.market.domain.usecase.trip.GetTripDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripDetailUiState(
    val trip: Trip? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false
)

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTripDetailUseCase: GetTripDetailUseCase,
    private val deleteTripUseCase: DeleteTripUseCase
) : ViewModel() {

    private val tripId: String = savedStateHandle["tripId"] ?: ""
    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState

    private var _householdId: String? = null

    fun setHouseholdId(householdId: String) {
        if (_householdId == householdId) return
        _householdId = householdId
        observeTripDetail(householdId, tripId)
    }

    private fun observeTripDetail(householdId: String, tripId: String) {
        viewModelScope.launch {
            getTripDetailUseCase(householdId, tripId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar el viaje: ${e.message}"
                    )
                }
                .collect { trip ->
                    _uiState.value = _uiState.value.copy(
                        trip = trip,
                        isLoading = false
                    )
                }
        }
    }

    fun deleteTrip(onSuccess: () -> Unit) {
        val hid = _householdId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            deleteTripUseCase(hid, tripId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleted = true
                    )
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = "Error al eliminar: ${e.message}"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
