package com.deecode.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.AuthGraph
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        authNavGraph(
            navController = navController,
            onNavigateToCustomer = {
                navController.navigate(Route.CustomerGraph) {
                    popUpTo(Route.AuthGraph) { inclusive = true }
                }
            },
            onNavigateToDriver = {
                navController.navigate(Route.DriverGraph) {
                    popUpTo(Route.AuthGraph) { inclusive = true }
                }
            },
            onNavigateToAdmin = {
                navController.navigate(Route.AdminGraph) {
                    popUpTo(Route.AuthGraph) { inclusive = true }
                }
            }
        )

        customerNavGraph(
            navController = navController,
            onLogout = {
                navController.navigate(Route.AuthGraph) {
                    popUpTo(Route.CustomerGraph) { inclusive = true }
                }
            }
        )

        driverNavGraph(
            onLogout = {
                navController.navigate(Route.AuthGraph) {
                    popUpTo(Route.DriverGraph) { inclusive = true }
                }
            }
        )

        adminNavGraph(
            onLogout = {
                navController.navigate(Route.AuthGraph) {
                    popUpTo(Route.AdminGraph) { inclusive = true }
                }
            }
        )
    }
}
