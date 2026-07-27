package com.market.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.market.presentation.screen.auth.LoginScreen
import com.market.presentation.screen.household.CreateHouseholdScreen
import com.market.presentation.screen.household.JoinHouseholdScreen

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

            composable(Route.ShoppingList.route) {
                ShoppingListRoute()
            }

            composable(Route.Prices.route) {
                PlaceholderScreen("Precios")
            }

            composable(Route.History.route) {
                PlaceholderScreen("Historial")
            }

            composable(
                route = Route.TripDetail.route,
                arguments = listOf(
                    navArgument("tripId") {
                        type = NavType.StringType
                    }
                )
            ) {
                PlaceholderScreen("Detalle de compra")
            }

            composable(Route.Settings.route) {
                PlaceholderScreen("Ajustes")
            }
        }
    }
}

@Composable
fun ShoppingListRoute() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Lista de compras",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
