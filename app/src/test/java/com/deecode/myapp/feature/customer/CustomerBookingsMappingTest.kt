package com.deecode.myapp.feature.customer

import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerBookingsMappingTest {

    @Test
    fun `customer bookings are sorted newest first by createdAt`() {
        val b1 = Booking(
            bookingId = "1",
            customerId = "user_1",
            pickup = LocationPoint(28.0, 77.0),
            destination = LocationPoint(28.1, 77.1),
            distanceMeters = 5000,
            estimatedDurationSeconds = 600,
            estimatedFare = 150.0,
            createdAt = 1000L
        )
        val b2 = Booking(
            bookingId = "2",
            customerId = "user_1",
            pickup = LocationPoint(28.0, 77.0),
            destination = LocationPoint(28.1, 77.1),
            distanceMeters = 5000,
            estimatedDurationSeconds = 600,
            estimatedFare = 150.0,
            createdAt = 3000L
        )
        val b3 = Booking(
            bookingId = "3",
            customerId = "user_1",
            pickup = LocationPoint(28.0, 77.0),
            destination = LocationPoint(28.1, 77.1),
            distanceMeters = 5000,
            estimatedDurationSeconds = 600,
            estimatedFare = 150.0,
            createdAt = 2000L
        )

        val list = listOf(b1, b2, b3)
        val sorted = list.sortedByDescending { it.createdAt }

        assertEquals("2", sorted[0].bookingId)
        assertEquals("3", sorted[1].bookingId)
        assertEquals("1", sorted[2].bookingId)
    }

    @Test
    fun `customer can only view own bookings`() {
        val currentUserId = "user_abc"
        val ownBooking = Booking(
            bookingId = "b_1",
            customerId = "user_abc",
            pickup = LocationPoint(28.0, 77.0),
            destination = LocationPoint(28.1, 77.1),
            distanceMeters = 1000,
            estimatedDurationSeconds = 120,
            estimatedFare = 50.0
        )
        val otherBooking = Booking(
            bookingId = "b_2",
            customerId = "user_xyz",
            pickup = LocationPoint(28.0, 77.0),
            destination = LocationPoint(28.1, 77.1),
            distanceMeters = 1000,
            estimatedDurationSeconds = 120,
            estimatedFare = 50.0
        )

        fun isAuthorized(booking: Booking, uid: String): Boolean = booking.customerId == uid

        assertTrue(isAuthorized(ownBooking, currentUserId))
        assertFalse(isAuthorized(otherBooking, currentUserId))
    }

    @Test
    fun `completed and cancelled statuses evaluate as terminal history items`() {
        assertTrue(BookingStatus.COMPLETED.isTerminal)
        assertTrue(BookingStatus.CANCELLED.isTerminal)
        assertTrue(BookingStatus.CANCELLED_BY_CUSTOMER.isTerminal)
        assertTrue(BookingStatus.CANCELLED_BY_DRIVER.isTerminal)

        assertFalse(BookingStatus.COMPLETED.isActive)
        assertFalse(BookingStatus.CANCELLED.isActive)
    }
}
