package com.market.presentation.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.market.presentation.screen.auth.LoginScreen
import com.market.presentation.screen.household.CreateHouseholdScreen
import com.market.presentation.screen.household.JoinHouseholdScreen
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  NAV HOST
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        Route.ShoppingList.route,
        Route.Prices.route,
        Route.History.route,
        Route.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Route.ShoppingList to Icons.Filled.Home,
                        Route.Prices to Icons.Filled.LocalOffer,
                        Route.History to Icons.Filled.History,
                        Route.Settings to Icons.Filled.Settings
                    )
                    val labels = mapOf(
                        Route.ShoppingList.route to "Listas",
                        Route.Prices.route to "Precios",
                        Route.History.route to "Historial",
                        Route.Settings.route to "Ajustes"
                    )
                    items.forEach { (route, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = labels[route.route]) },
                            label = { Text(labels[route.route] ?: "") },
                            selected = currentDestination?.hierarchy?.any { it.route == route.route } == true,
                            onClick = {
                                navController.navigate(route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Login.route) {
                LoginScreen(
                    onSignInSuccess = {
                        navController.navigate(Route.CreateHousehold.route) {
                            popUpTo(Route.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Route.CreateHousehold.route) {
                CreateHouseholdScreen(
                    onHouseholdCreated = {
                        navController.navigate(Route.ShoppingList.route) {
                            popUpTo(Route.CreateHousehold.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToJoin = {
                        navController.navigate(Route.JoinHousehold.createRoute())
                    }
                )
            }

            composable(
                route = Route.JoinHousehold.route,
                arguments = listOf(navArgument("code") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val code = backStackEntry.arguments?.getString("code") ?: ""
                JoinHouseholdScreen(
                    initialCode = code,
                    onHouseholdJoined = {
                        navController.navigate(Route.ShoppingList.route) {
                            popUpTo(Route.JoinHousehold.route) { inclusive = true }
                        }
                    },
                    onNavigateToCreate = { navController.navigate(Route.CreateHousehold.route) }
                )
            }

            // ── MIS LISTAS ──
            composable(Route.ShoppingList.route) {
                ShoppingListsScreen(
                    onOpenList = { listId ->
                        navController.navigate(Route.TripDetail.createRoute(listId))
                    }
                )
            }

            // ── DETALLE DE UNA LISTA ──
            composable(
                route = Route.TripDetail.route,
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("tripId") ?: ""
                ListDetailScreen(listId = listId)
            }

            composable(Route.Prices.route) { PricesDirect() }
            composable(Route.History.route) { HistoryDirect() }
            composable(Route.Settings.route) { SettingsDirect() }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────
@Composable
fun rememberHouseholdId(): String? {
    var hid by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect
        val doc = FirebaseFirestore.getInstance()
            .collection("users").document(user.uid).get().await()
        hid = doc.getString("householdId")
    }
    return hid
}

private fun formatDate(ms: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))

// ─────────────────────────────────────────────────────────────
//  SCREEN: MIS LISTAS  (households/{hid}/lists)
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(onOpenList: (String) -> Unit) {
    val householdId = rememberHouseholdId()
    val lists = remember { mutableStateListOf<Map<String, Any?>>() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    // Real-time listener on lists
    LaunchedEffect(householdId) {
        val hid = householdId ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("households").document(hid).collection("lists")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                lists.clear()
                snap?.documents?.forEach { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    lists.add(data)
                }
            }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis listas") }) },
        floatingActionButton = {
            if (householdId != null) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva lista")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                householdId == null -> Text("No perteneces a ningún hogar")
                lists.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay listas", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toca + para crear tu primera lista", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(lists) { list ->
                        val name = list["name"] as? String ?: "Sin nombre"
                        val createdAt = (list["createdAt"] as? Number)?.toLong() ?: 0L
                        val itemCount = (list["itemCount"] as? Number)?.toInt() ?: 0
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenList(list["id"] as String) }
                                .padding(vertical = 14.dp)
                        ) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (createdAt > 0) "${formatDate(createdAt)} · $itemCount productos"
                                else "$itemCount productos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newListName = "" },
            title = { Text("Nueva lista") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nombre (ej: Lista 27/07)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val hid = householdId ?: return@TextButton
                    val user = FirebaseAuth.getInstance().currentUser ?: return@TextButton
                    val finalName = newListName.ifBlank {
                        val today = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
                        "Lista $today"
                    }
                    FirebaseFirestore.getInstance()
                        .collection("households").document(hid).collection("lists")
                        .add(
                            mapOf(
                                "name" to finalName,
                                "createdBy" to user.uid,
                                "createdAt" to System.currentTimeMillis(),
                                "itemCount" to 0
                            )
                        )
                    newListName = ""
                    showCreateDialog = false
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newListName = "" }) { Text("Cancelar") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN: DETALLE DE UNA LISTA  (households/{hid}/lists/{listId}/items)
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(listId: String) {
    val householdId = rememberHouseholdId()
    val items = remember { mutableStateListOf<Map<String, Any?>>() }
    var listName by remember { mutableStateOf("Lista") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    // Edit dialog state
    var editingItem by remember { mutableStateOf<Map<String, Any?>?>(null) }
    var editName by remember { mutableStateOf("") }

    // Delete confirmation state
    var deletingItem by remember { mutableStateOf<Map<String, Any?>?>(null) }

    // Load list name
    LaunchedEffect(householdId, listId) {
        val hid = householdId ?: return@LaunchedEffect
        val doc = FirebaseFirestore.getInstance()
            .collection("households").document(hid).collection("lists").document(listId).get().await()
        listName = doc.getString("name") ?: "Lista"
    }

    // Real-time items listener
    LaunchedEffect(householdId, listId) {
        val hid = householdId ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("households").document(hid)
            .collection("lists").document(listId)
            .collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                items.clear()
                snap?.documents?.forEach { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    items.add(data)
                }
            }
    }

    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = { TopAppBar(title = { Text(listName) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                householdId == null -> Text("No hay hogar")
                items.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lista vacía", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toca + para agregar productos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    val itemsPath = db.collection("households").document(householdId!!)
                        .collection("lists").document(listId)
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(items) { item ->
                            val itemId = item["id"] as? String ?: return@items
                            val name = item["name"] as? String ?: "?"
                            val checked = item["isChecked"] as? Boolean ?: false

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Check toggle
                                IconButton(onClick = {
                                    itemsPath.collection("items").document(itemId)
                                        .update("isChecked", !checked)
                                }) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = if (checked) "Desmarcar" else "Marcar",
                                        tint = if (checked) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Name
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                // Edit
                                IconButton(onClick = {
                                    editingItem = item
                                    editName = name
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Delete
                                IconButton(onClick = {
                                    deletingItem = item
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add dialog ──
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newItemName = "" },
            title = { Text("Agregar producto") },
            text = {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val hid = householdId ?: return@TextButton
                    val user = FirebaseAuth.getInstance().currentUser ?: return@TextButton
                    if (newItemName.isNotBlank()) {
                        val listRef = db.collection("households").document(hid)
                            .collection("lists").document(listId)
                        listRef.collection("items").add(
                            mapOf(
                                "name" to newItemName.trim(),
                                "isChecked" to false,
                                "createdBy" to user.uid,
                                "createdAt" to System.currentTimeMillis()
                            )
                        )
                        // Update itemCount
                        val newCount = items.size + 1
                        listRef.update("itemCount", newCount)
                        newItemName = ""
                        showAddDialog = false
                    }
                }, enabled = newItemName.isNotBlank()) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newItemName = "" }) { Text("Cancelar") }
            }
        )
    }

    // ── Edit dialog ──
    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null; editName = "" },
            title = { Text("Editar producto") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val hid = householdId ?: return@TextButton
                    val itemId = editingItem?.get("id") as? String ?: return@TextButton
                    if (editName.isNotBlank()) {
                        db.collection("households").document(hid)
                            .collection("lists").document(listId)
                            .collection("items").document(itemId)
                            .update("name", editName.trim())
                    }
                    editingItem = null; editName = ""
                }, enabled = editName.isNotBlank()) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null; editName = "" }) { Text("Cancelar") }
            }
        )
    }

    // ── Delete confirmation dialog ──
    if (deletingItem != null) {
        val itemName = deletingItem?.get("name") as? String ?: ""
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Deseas eliminar \"$itemName\" de la lista?") },
            confirmButton = {
                TextButton(onClick = {
                    val hid = householdId ?: return@TextButton
                    val itemId = deletingItem?.get("id") as? String ?: return@TextButton
                    db.collection("households").document(hid)
                        .collection("lists").document(listId)
                        .collection("items").document(itemId).delete()
                    // Update itemCount
                    val newCount = (items.size - 1).coerceAtLeast(0)
                    db.collection("households").document(hid)
                        .collection("lists").document(listId)
                        .update("itemCount", newCount)
                    deletingItem = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) { Text("Cancelar") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN: PRECIOS
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricesDirect() {
    val householdId = rememberHouseholdId()
    val prices = remember { mutableStateListOf<Map<String, Any?>>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    LaunchedEffect(householdId) {
        val hid = householdId ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("households").document(hid).collection("prices")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().also { snap ->
                prices.clear()
                snap.documents.forEach { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    prices.add(data)
                }
            }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Precios") }) },
        floatingActionButton = {
            if (householdId != null) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar precio")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                householdId == null -> Text("No hay hogar seleccionado")
                prices.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin precios guardados", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toca + para comparar precios", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(prices) { price ->
                        val pName = price["itemName"] as? String ?: "?"
                        val store = price["storeName"] as? String ?: "?"
                        val pValue = (price["price"] as? Number)?.toDouble() ?: 0.0
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            Text(pName, style = MaterialTheme.typography.titleMedium)
                            Text("$store — ₡${String.format("%.0f", pValue)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; itemName = ""; storeName = ""; priceText = "" },
            title = { Text("Agregar precio") },
            text = {
                Column {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Producto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("Tienda") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Precio ₡") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val hid = householdId ?: return@TextButton
                    val user = FirebaseAuth.getInstance().currentUser ?: return@TextButton
                    val price = priceText.toDoubleOrNull()
                    if (itemName.isNotBlank() && storeName.isNotBlank() && price != null) {
                        FirebaseFirestore.getInstance()
                            .collection("households").document(hid).collection("prices")
                            .add(mapOf(
                                "itemName" to itemName.trim(),
                                "storeName" to storeName.trim(),
                                "price" to price,
                                "createdBy" to user.uid,
                                "createdAt" to System.currentTimeMillis()
                            ))
                        itemName = ""; storeName = ""; priceText = ""; showAddDialog = false
                    }
                }, enabled = itemName.isNotBlank() && storeName.isNotBlank() && priceText.toDoubleOrNull() != null) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; itemName = ""; storeName = ""; priceText = "" }) { Text("Cancelar") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN: HISTORIAL
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDirect() {
    val householdId = rememberHouseholdId()
    val trips = remember { mutableStateListOf<Map<String, Any?>>() }

    LaunchedEffect(householdId) {
        val hid = householdId ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("households").document(hid).collection("trips")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .get().await().also { snap ->
                trips.clear()
                snap.documents.forEach { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    trips.add(data)
                }
            }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Historial de compras") }) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                householdId == null -> Text("No hay hogar seleccionado")
                trips.isEmpty() -> Text("Sin compras registradas", style = MaterialTheme.typography.headlineSmall)
                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(trips) { trip ->
                        val name = trip["name"] as? String ?: "Compra"
                        val completedAt = (trip["completedAt"] as? Number)?.toLong() ?: 0L
                        val total = (trip["total"] as? Number)?.toDouble() ?: 0.0
                        val dateStr = if (completedAt > 0) formatDate(completedAt) else "?"
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text("📅 $dateStr — ₡${String.format("%.0f", total)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN: AJUSTES
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDirect() {
    val user = FirebaseAuth.getInstance().currentUser
    val householdId = rememberHouseholdId()
    var householdName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }

    LaunchedEffect(householdId) {
        val hid = householdId ?: return@LaunchedEffect
        val doc = FirebaseFirestore.getInstance().collection("households").document(hid).get().await()
        householdName = doc.getString("name") ?: ""
        inviteCode = doc.getString("inviteCode") ?: ""
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Ajustes") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Tu cuenta", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(user?.displayName ?: "", style = MaterialTheme.typography.bodyLarge)
            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Hogar", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (householdName.isNotEmpty()) Text(householdName, style = MaterialTheme.typography.bodyLarge)
            if (inviteCode.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Código de invitación: $inviteCode", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { FirebaseAuth.getInstance().signOut() }) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
