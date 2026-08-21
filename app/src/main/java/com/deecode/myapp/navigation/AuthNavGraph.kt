package com.deecode.myapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.deecode.myapp.feature.auth.login.LoginScreen
import com.deecode.myapp.feature.auth.register.RegisterScreen
import com.deecode.myapp.feature.splash.SplashScreen

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onNavigateToCustomer: () -> Unit,
    onNavigateToDriver: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    navigation<Route.AuthGraph>(
        startDestination = Route.SplashRoute
    ) {
        composable<Route.SplashRoute> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.LoginRoute) {
                        popUpTo(Route.SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToCustomer = onNavigateToCustomer,
                onNavigateToDriver = onNavigateToDriver,
                onNavigateToAdmin = onNavigateToAdmin
            )
        }

        composable<Route.LoginRoute> {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Route.RegisterRoute)
                },
                onNavigateToCustomer = onNavigateToCustomer,
                onNavigateToDriver = onNavigateToDriver,
                onNavigateToAdmin = onNavigateToAdmin
            )
        }

        composable<Route.RegisterRoute> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToCustomer = onNavigateToCustomer
            )
        }
    }
}
