package com.market.presentation.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.market.domain.model.ShoppingItem
import com.market.presentation.component.CheckOffDialog
import com.market.presentation.component.EmptyState
import com.market.presentation.component.ItemCard
import com.market.presentation.component.StoreHeader
import com.market.presentation.viewmodel.ShoppingListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    householdId: String
) {
    LaunchedEffect(householdId) {
        viewModel.setHouseholdId(householdId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ShoppingItem?>(null) }
    var showCheckDialog by remember { mutableStateOf<ShoppingItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                    Text("Lista de compras", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Complete trip FAB - only shown when there are checked items
                val checkedItems = uiState.itemsByStore.values.flatten().filter { it.isChecked }
                if (checkedItems.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.completeTrip(
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Compra registrada exitosamente")
                                    }
                                },
                                onError = { errorMsg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(errorMsg)
                                    }
                                }
                            )
                        },
                        icon = {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        },
                        text = { Text("Finalizar compra") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar item")
                }
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
        } else {
            val grouped = uiState.itemsByStore
            val storeMap = uiState.stores.associateBy { it.id }
            val storesWithItems = uiState.stores.filter { store ->
                grouped.containsKey(store.id) && grouped[store.id]?.isNotEmpty() == true
            }
            val unassignedItems = grouped[null].orEmpty()

            if (uiState.stores.isEmpty() && unassignedItems.isEmpty()) {
                EmptyState(
                    title = "Tu lista está vacía",
                    subtitle = "Toca + para agregar tu primer item",
                    modifier = Modifier.padding(padding)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                        // Items grouped by store
                        storesWithItems.forEach { store ->
                            item(key = "store_${store.id}") {
                                StoreHeader(
                                    storeName = store.name,
                                    isAdmin = uiState.isAdmin,
                                    onEdit = { /* Phase 4 */ },
                                    onDelete = { /* Phase 4 */ }
                                )
                            }
                            items(
                                items = grouped[store.id].orEmpty(),
                                key = { it.id }
                            ) { item ->
                                ItemCard(
                                    item = item,
                                    storeName = null,
                                    isAdmin = uiState.isAdmin,
                                    onToggleCheck = {
                                        if (!item.isChecked) {
                                            showCheckDialog = item
                                        } else {
                                            viewModel.toggleCheck(item)
                                        }
                                    },
                                    onEdit = { showEditDialog = item },
                                    onDelete = { viewModel.deleteItem(item.id) }
                                )
                            }
                        }

                        // Unassigned items section
                        if (unassignedItems.isNotEmpty()) {
                            item(key = "unassigned_header") {
                                StoreHeader(
                                    storeName = "Sin asignar",
                                    isAdmin = false,
                                    onEdit = {},
                                    onDelete = {}
                                )
                            }
                            items(
                                items = unassignedItems,
                                key = { it.id }
                            ) { item ->
                                ItemCard(
                                    item = item,
                                    storeName = null,
                                    isAdmin = uiState.isAdmin,
                                    onToggleCheck = {
                                        if (!item.isChecked) {
                                            showCheckDialog = item
                                        } else {
                                            viewModel.toggleCheck(item)
                                        }
                                    },
                                    onEdit = { showEditDialog = item },
                                    onDelete = { viewModel.deleteItem(item.id) }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
            }
        }
    }

    // Add item dialog
    if (showAddDialog) {
        AddItemDialog(
            onConfirm = { name ->
                viewModel.addItem(name)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit item dialog
    showEditDialog?.let { item ->
        EditItemDialog(
            currentName = item.name,
            onConfirm = { newName ->
                viewModel.editItem(item, newName)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    // Check-off dialog
    showCheckDialog?.let { item ->
        CheckOffDialog(
            itemName = item.name,
            isAdmin = uiState.isAdmin,
            onConfirm = { reason ->
                viewModel.toggleCheck(item, reason.ifBlank { null })
                showCheckDialog = null
            },
            onDismiss = { showCheckDialog = null }
        )
    }
}

@Composable
private fun AddItemDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar item") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    hasError = false
                },
                label = { Text("Nombre del item") },
                placeholder = { Text("Ej: Leche") },
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
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EditItemDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar item") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    hasError = false
                },
                label = { Text("Nombre del item") },
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
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
