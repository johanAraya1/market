package com.market.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object CreateHousehold : Route("create_household")
    data object JoinHousehold : Route("join_household?code={code}") {
        fun createRoute(code: String = "") = "join_household?code=$code"
    }
    data object ShoppingList : Route("shopping_list")
    data object Stores : Route("stores")
    data object Prices : Route("prices")
    data object History : Route("history")
    data object TripDetail : Route("trip_detail/{tripId}") {
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
    data object Settings : Route("settings")
    data object PriceComparison : Route("price_comparison")
}

data class BottomNavItem(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Route.ShoppingList, "Lista", Icons.Filled.Home),
    BottomNavItem(Route.Prices, "Precios", Icons.Filled.LocalOffer),
    BottomNavItem(Route.History, "Historial", Icons.Filled.History),
    BottomNavItem(Route.Settings, "Ajustes", Icons.Filled.Settings)
)
