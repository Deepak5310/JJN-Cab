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

    // Driver & Admin Destinations
    @Serializable
    data object DriverHomeRoute : Route

    @Serializable
    data object AdminDashboardRoute : Route
}
