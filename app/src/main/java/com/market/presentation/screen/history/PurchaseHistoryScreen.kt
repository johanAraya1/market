package com.market.presentation.screen.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.market.presentation.component.EmptyState
import com.market.presentation.component.ErrorState
import com.market.presentation.component.LoadingIndicator
import com.market.presentation.component.TripCard
import com.market.presentation.viewmodel.PurchaseHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(
    viewModel: PurchaseHistoryViewModel,
    householdId: String,
    onTripClick: (String) -> Unit
) {
    LaunchedEffect(householdId) {
        viewModel.setHouseholdId(householdId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Historial de compras", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(padding))
            }
            uiState.error != null && uiState.trips.isEmpty() -> {
                ErrorState(
                    title = "Error al cargar",
                    message = uiState.error ?: "Error desconocido",
                    onRetry = { viewModel.setHouseholdId(householdId) },
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.trips.isEmpty() -> {
                EmptyState(
                    title = "Aún no hay compras registradas",
                    subtitle = "Finaliza una compra desde la lista para ver el historial",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.trips,
                            key = { it.id }
                        ) { trip ->
                            TripCard(
                                trip = trip,
                                onClick = { onTripClick(trip.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
