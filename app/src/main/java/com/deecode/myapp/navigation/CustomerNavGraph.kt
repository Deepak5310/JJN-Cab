package com.deecode.myapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.deecode.myapp.feature.customer.CustomerPlaceholderScreen

fun NavGraphBuilder.customerNavGraph(
    onLogout: () -> Unit
) {
    navigation<Route.CustomerGraph>(
        startDestination = Route.CustomerHomeRoute
    ) {
        composable<Route.CustomerHomeRoute> {
            CustomerPlaceholderScreen(onLogout = onLogout)
        }
    }
}
