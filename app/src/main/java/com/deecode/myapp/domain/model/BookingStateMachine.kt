package com.deecode.myapp.domain.model

import com.deecode.myapp.core.model.UserRole

object BookingStateMachine {

    fun canonicalize(statusStr: String?): BookingStatus {
        if (statusStr.isNullOrBlank()) return BookingStatus.REQUESTED
        return try {
            when (statusStr.uppercase().trim()) {
                "ASSIGNED" -> BookingStatus.ACCEPTED
                "ARRIVING" -> BookingStatus.DRIVER_ARRIVING
                "STARTED" -> BookingStatus.IN_PROGRESS
                else -> BookingStatus.valueOf(statusStr.uppercase().trim())
            }
        } catch (_: Exception) {
            BookingStatus.REQUESTED
        }
    }

    fun isTerminal(status: BookingStatus): Boolean {
        return status in setOf(
            BookingStatus.COMPLETED,
            BookingStatus.CANCELLED,
            BookingStatus.CANCELLED_BY_CUSTOMER,
            BookingStatus.CANCELLED_BY_DRIVER,
            BookingStatus.NO_DRIVERS_AVAILABLE
        )
    }

    fun isActive(status: BookingStatus): Boolean {
        return status in setOf(
            BookingStatus.REQUESTED,
            BookingStatus.SEARCHING_DRIVER,
            BookingStatus.ACCEPTED,
            BookingStatus.DRIVER_ARRIVING,
            BookingStatus.IN_PROGRESS
        )
    }

    fun canDriverAccept(currentStatus: BookingStatus, currentDriverId: String?): Boolean {
        if (!currentDriverId.isNullOrBlank()) return false
        return currentStatus == BookingStatus.REQUESTED || currentStatus == BookingStatus.SEARCHING_DRIVER
    }

    fun canDriverTransition(from: BookingStatus, to: BookingStatus): Boolean {
        return when (to) {
            BookingStatus.DRIVER_ARRIVING -> from == BookingStatus.ACCEPTED
            BookingStatus.IN_PROGRESS -> from == BookingStatus.DRIVER_ARRIVING
            BookingStatus.COMPLETED -> from == BookingStatus.IN_PROGRESS
            BookingStatus.CANCELLED, BookingStatus.CANCELLED_BY_DRIVER ->
                from in setOf(BookingStatus.ACCEPTED, BookingStatus.DRIVER_ARRIVING, BookingStatus.IN_PROGRESS)
            else -> false
        }
    }

    fun canComplete(currentStatus: BookingStatus): Boolean {
        return currentStatus == BookingStatus.IN_PROGRESS
    }

    fun canCancel(currentStatus: BookingStatus, byRole: UserRole): Boolean {
        if (isTerminal(currentStatus)) return false

        return when (byRole) {
            UserRole.CUSTOMER -> {
                // Customer can cancel during REQUESTED, SEARCHING_DRIVER, ACCEPTED, or DRIVER_ARRIVING
                currentStatus in setOf(
                    BookingStatus.REQUESTED,
                    BookingStatus.SEARCHING_DRIVER,
                    BookingStatus.ACCEPTED,
                    BookingStatus.DRIVER_ARRIVING
                )
            }
            UserRole.DRIVER -> {
                // Driver can cancel during ACCEPTED, DRIVER_ARRIVING, or emergency IN_PROGRESS
                currentStatus in setOf(
                    BookingStatus.ACCEPTED,
                    BookingStatus.DRIVER_ARRIVING,
                    BookingStatus.IN_PROGRESS
                )
            }
            UserRole.ADMIN -> {
                // Admin can cancel any active booking before completion
                isActive(currentStatus)
            }
        }
    }

    fun isValidTransition(from: BookingStatus, to: BookingStatus, byRole: UserRole): Boolean {
        if (from == to) return false
        if (isTerminal(from)) return false

        return when (byRole) {
            UserRole.DRIVER -> canDriverTransition(from, to)
            UserRole.CUSTOMER -> {
                to in setOf(BookingStatus.CANCELLED, BookingStatus.CANCELLED_BY_CUSTOMER) && canCancel(from, UserRole.CUSTOMER)
            }
            UserRole.ADMIN -> {
                to in setOf(BookingStatus.CANCELLED, BookingStatus.CANCELLED_BY_CUSTOMER, BookingStatus.CANCELLED_BY_DRIVER) && canCancel(from, UserRole.ADMIN)
            }
        }
    }
}
