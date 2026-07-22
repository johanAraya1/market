package com.market.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.market.presentation.screen.auth.LoginScreen
import com.market.presentation.screen.history.PurchaseHistoryScreen
import com.market.presentation.screen.history.TripDetailScreen
import com.market.presentation.screen.household.CreateHouseholdScreen
import com.market.presentation.screen.household.JoinHouseholdScreen
import com.market.presentation.screen.list.ShoppingListScreen
import com.market.presentation.screen.price.PriceComparisonScreen
import com.market.presentation.viewmodel.PriceComparisonViewModel
import com.market.presentation.viewmodel.PurchaseHistoryViewModel
import com.market.presentation.viewmodel.ShoppingListViewModel
import com.market.presentation.viewmodel.TripDetailViewModel

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
                        // Navigate to main screen - householdId will be resolved from auth state
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
                val viewModel: ShoppingListViewModel = hiltViewModel()
                // TODO: Replace with real householdId from auth/household state
                ShoppingListScreen(
                    viewModel = viewModel,
                    householdId = ""
                )
            }

            composable(Route.Prices.route) {
                val viewModel: PriceComparisonViewModel = hiltViewModel()
                PriceComparisonScreen(
                    viewModel = viewModel,
                    householdId = ""
                )
            }

            composable(Route.History.route) {
                val viewModel: PurchaseHistoryViewModel = hiltViewModel()
                PurchaseHistoryScreen(
                    viewModel = viewModel,
                    householdId = "",
                    onTripClick = { tripId ->
                        navController.navigate(Route.TripDetail.createRoute(tripId))
                    }
                )
            }

            composable(
                route = Route.TripDetail.route,
                arguments = listOf(
                    navArgument("tripId") {
                        type = NavType.StringType
                    }
                )
            ) {
                val viewModel: TripDetailViewModel = hiltViewModel()
                TripDetailScreen(
                    viewModel = viewModel,
                    householdId = "",
                    isAdmin = true,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Route.Settings.route) {
                com.market.presentation.component.EmptyState(
                    title = "Ajustes",
                    subtitle = "Configuración del hogar"
                )
            }
        }
    }
}
