package com.deecode.myapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.deecode.myapp.feature.auth.AuthPlaceholderScreen

fun NavGraphBuilder.authNavGraph(
    onNavigateToCustomer: () -> Unit,
    onNavigateToDriver: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    navigation<Route.AuthGraph>(
        startDestination = Route.LoginRoute
    ) {
        composable<Route.LoginRoute> {
            AuthPlaceholderScreen()
        }
    }
}
