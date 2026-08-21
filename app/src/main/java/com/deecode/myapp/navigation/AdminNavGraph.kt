package com.deecode.myapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.deecode.myapp.feature.admin.AdminPlaceholderScreen

fun NavGraphBuilder.adminNavGraph(
    onLogout: () -> Unit
) {
    navigation<Route.AdminGraph>(
        startDestination = Route.AdminDashboardRoute
    ) {
        composable<Route.AdminDashboardRoute> {
            AdminPlaceholderScreen()
        }
    }
}
