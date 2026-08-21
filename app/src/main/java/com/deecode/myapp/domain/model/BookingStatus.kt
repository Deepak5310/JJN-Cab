package com.deecode.myapp.domain.model

enum class BookingStatus {
    REQUESTED,
    SEARCHING_DRIVER,
    ACCEPTED,
    DRIVER_ARRIVING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED_BY_CUSTOMER,
    CANCELLED_BY_DRIVER,
    NO_DRIVERS_AVAILABLE;

    val isActive: Boolean
        get() = this in setOf(
            REQUESTED,
            SEARCHING_DRIVER,
            ACCEPTED,
            DRIVER_ARRIVING,
            IN_PROGRESS
        )

    val isTerminal: Boolean
        get() = this in setOf(
            COMPLETED,
            CANCELLED_BY_CUSTOMER,
            CANCELLED_BY_DRIVER,
            NO_DRIVERS_AVAILABLE
        )
}
