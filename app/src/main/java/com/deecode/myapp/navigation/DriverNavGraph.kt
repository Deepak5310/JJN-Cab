package com.deecode.myapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.deecode.myapp.feature.driver.DriverAppShell

fun NavGraphBuilder.driverNavGraph(
    onLogout: () -> Unit
) {
    navigation<Route.DriverGraph>(
        startDestination = Route.DriverHomeRoute
    ) {
        composable<Route.DriverHomeRoute> {
            DriverAppShell(onLogout = onLogout)
        }
    }
}
