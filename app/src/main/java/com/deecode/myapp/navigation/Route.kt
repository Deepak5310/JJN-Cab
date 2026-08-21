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

    // Destinations
    @Serializable
    data object LoginRoute : Route

    @Serializable
    data object CustomerHomeRoute : Route

    @Serializable
    data object DriverHomeRoute : Route

    @Serializable
    data object AdminDashboardRoute : Route
}
