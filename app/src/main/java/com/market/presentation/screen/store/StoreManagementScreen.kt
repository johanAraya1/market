package com.market.presentation.screen.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.market.domain.model.Store
import com.market.presentation.component.EmptyState
import com.market.presentation.component.StoreHeader
import com.market.presentation.viewmodel.StoreManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreManagementScreen(
    viewModel: StoreManagementViewModel,
    householdId: String
) {
    LaunchedEffect(householdId) {
        viewModel.setHouseholdId(householdId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Store?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Store?>(null) }
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
                    Text("Gestionar tiendas", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar tienda")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.stores.isEmpty()) {
            EmptyState(
                title = "Sin tiendas",
                subtitle = "Agrega una tienda para organizar tu lista",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = uiState.stores,
                    key = { it.id }
                ) { store ->
                    StoreHeader(
                        storeName = store.name,
                        isAdmin = true,
                        onEdit = { showEditDialog = store },
                        onDelete = { showDeleteDialog = store }
                    )
                }
            }
        }
    }

    // Add store dialog
    if (showAddDialog) {
        StoreNameDialog(
            title = "Agregar tienda",
            initialName = "",
            confirmText = "Agregar",
            onConfirm = { name ->
                viewModel.addStore(name)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit store dialog
    showEditDialog?.let { store ->
        StoreNameDialog(
            title = "Editar tienda",
            initialName = store.name,
            confirmText = "Guardar",
            onConfirm = { newName ->
                viewModel.editStore(store, newName)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    // Delete store dialog
    showDeleteDialog?.let { store ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("¿Eliminar tienda?") },
            text = { Text("¿Estás seguro de que deseas eliminar \"${store.name}\"? Los items no se eliminarán.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStore(store.id)
                    showDeleteDialog = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun StoreNameDialog(
    title: String,
    initialName: String,
    confirmText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    hasError = false
                },
                label = { Text("Nombre de la tienda") },
                placeholder = { Text("Ej: Automercado") },
                singleLine = true,
                isError = hasError,
                supportingText = if (hasError) {
                    { Text("El nombre debe tener entre 1 y 50 caracteres") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty() || trimmed.length > 50) {
                        hasError = true
                    } else {
                        onConfirm(trimmed)
                    }
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
