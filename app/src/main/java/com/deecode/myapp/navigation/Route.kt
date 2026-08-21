package com.deecode.myapp.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    // Top-Level Subgraphs
    @Serializable
    data object AuthGraph : Route

    @Serializable
    data object CustomerGraph : Route

    @Serializable
    data object DriverGraph : Route

    @Serializable
    data object AdminGraph : Route

    // Auth Destinations
    @Serializable
    data object SplashRoute : Route

    @Serializable
    data object LoginRoute : Route

    @Serializable
    data object RegisterRoute : Route

    // Customer Destinations
    @Serializable
    data object CustomerHomeRoute : Route

    @Serializable
    data object CustomerBookingsRoute : Route

    @Serializable
    data object CustomerProfileRoute : Route

    @Serializable
    data object CustomerActiveBookingRoute : Route

    // Driver Destinations
    @Serializable
    data object DriverHomeRoute : Route

    @Serializable
    data object DriverRequestsRoute : Route

    @Serializable
    data object DriverActiveRideRoute : Route

    @Serializable
    data object DriverProfileRoute : Route

    // Admin Destinations
    @Serializable
    data object AdminDashboardRoute : Route

    @Serializable
    data object AdminBookingsRoute : Route

    @Serializable
    data object AdminDriversRoute : Route

    @Serializable
    data object AdminUsersRoute : Route

    @Serializable
    data object AdminProfileRoute : Route
}
