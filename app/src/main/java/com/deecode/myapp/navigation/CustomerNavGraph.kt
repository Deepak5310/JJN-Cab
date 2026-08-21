package com.deecode.myapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.deecode.myapp.feature.customer.CustomerAppShell
import com.deecode.myapp.feature.customer.active.CustomerActiveBookingScreen
import com.deecode.myapp.feature.customer.details.CustomerRideDetailsScreen

fun NavGraphBuilder.customerNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation<Route.CustomerGraph>(
        startDestination = Route.CustomerHomeRoute
    ) {
        composable<Route.CustomerHomeRoute> {
            CustomerAppShell(
                onLogout = onLogout,
                onNavigateToActiveBooking = {
                    navController.navigate(Route.CustomerActiveBookingRoute)
                },
                onNavigateToRideDetails = { bookingId ->
                    navController.navigate(Route.CustomerRideDetailsRoute(bookingId))
                }
            )
        }

        composable<Route.CustomerActiveBookingRoute> {
            CustomerActiveBookingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.CustomerRideDetailsRoute> {
            CustomerRideDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
