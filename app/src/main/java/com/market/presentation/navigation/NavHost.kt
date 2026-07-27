package com.market.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.market.presentation.screen.auth.LoginScreen
import com.market.presentation.screen.household.CreateHouseholdScreen
import com.market.presentation.screen.household.JoinHouseholdScreen
import kotlinx.coroutines.tasks.await

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
                        Route.ShoppingList.route to "Lista",
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
                arguments = listOf(
                    navArgument("code") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val code = backStackEntry.arguments?.getString("code") ?: ""
                JoinHouseholdScreen(
                    initialCode = code,
                    onHouseholdJoined = {
                        navController.navigate(Route.ShoppingList.route) {
                            popUpTo(Route.JoinHousehold.route) { inclusive = true }
                        }
                    },
                    onNavigateToCreate = {
                        navController.navigate(Route.CreateHousehold.route)
                    }
                )
            }

            // Shopping list — direct Firestore, no Hilt ViewModel
            composable(Route.ShoppingList.route) {
                ShoppingListDirect()
            }

            composable(Route.Prices.route) {
                PlaceholderScreen("Precios")
            }

            composable(Route.History.route) {
                PlaceholderScreen("Historial")
            }

            composable(
                route = Route.TripDetail.route,
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) {
                PlaceholderScreen("Detalle de compra")
            }

            composable(Route.Settings.route) {
                PlaceholderScreen("Ajustes")
            }
        }
    }
}

/**
 * Shopping list that talks directly to Firestore — no Hilt, no ViewModel.
 * We'll refactor to proper architecture once the core flow works.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDirect() {
    val items = remember { mutableStateListOf<Map<String, Any?>>() }
    var householdId by remember { mutableStateOf<String?>(null) }
    var householdName by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Find the user's household
    LaunchedEffect(Unit) {
        try {
            val user = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect
            val db = FirebaseFirestore.getInstance()

            // Check user doc for householdId
            val userDoc = db.collection("users").document(user.uid).get().await()
            val hid = userDoc.getString("householdId")
            if (hid != null) {
                householdId = hid
                val hhDoc = db.collection("households").document(hid).get().await()
                householdName = hhDoc.getString("name") ?: ""
            }

            isLoading = false
        } catch (e: Throwable) {
            isLoading = false
        }
    }

    // Listen for items once we have a householdId
    LaunchedEffect(householdId) {
        val hid = householdId ?: return@LaunchedEffect
        val listener = FirebaseFirestore.getInstance()
            .collection("households").document(hid).collection("items")
            .addSnapshotListener { snapshot, _ ->
                items.clear()
                snapshot?.documents?.forEach { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    items.add(data)
                }
            }
        // Note: we don't remove listener here for simplicity
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (householdName.isNotEmpty()) "Lista: $householdName" else "Mi Lista") })
        },
        floatingActionButton = {
            if (householdId != null) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    Text("Cargando...", style = MaterialTheme.typography.bodyLarge)
                }
                householdId == null -> {
                    Text("No perteneces a ningún hogar", style = MaterialTheme.typography.bodyLarge)
                }
                items.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tu lista está vacía", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca + para agregar productos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(items) { item ->
                            val name = item["name"] as? String ?: "Sin nombre"
                            val checked = item["isChecked"] as? Boolean ?: false
                            Text(
                                text = if (checked) "✓ $name" else name,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

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
                TextButton(
                    onClick = {
                        val hid = householdId ?: return@TextButton
                        val user = FirebaseAuth.getInstance().currentUser ?: return@TextButton
                        if (newItemName.isNotBlank()) {
                            FirebaseFirestore.getInstance()
                                .collection("households").document(hid).collection("items")
                                .add(
                                    mapOf(
                                        "name" to newItemName.trim(),
                                        "isChecked" to false,
                                        "createdBy" to user.uid,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                )
                            newItemName = ""
                            showAddDialog = false
                        }
                    },
                    enabled = newItemName.isNotBlank()
                ) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newItemName = "" }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}
