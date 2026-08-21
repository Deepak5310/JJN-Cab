package com.deecode.myapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.deecode.myapp.feature.driver.DriverPlaceholderScreen

fun NavGraphBuilder.driverNavGraph(
    onLogout: () -> Unit
) {
    navigation<Route.DriverGraph>(
        startDestination = Route.DriverHomeRoute
    ) {
        composable<Route.DriverHomeRoute> {
            DriverPlaceholderScreen(onLogout = onLogout)
        }
    }
}
