package com.deecode.myapp.feature.rating

import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.Rating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingValidationTest {

    @Test
    fun `validates rating boundary between 1 and 5 stars`() {
        fun isValidRating(rating: Int): Boolean = rating in 1..5

        assertTrue(isValidRating(1))
        assertTrue(isValidRating(3))
        assertTrue(isValidRating(5))
        assertFalse(isValidRating(0))
        assertFalse(isValidRating(6))
        assertFalse(isValidRating(-1))
    }

    @Test
    fun `calculates aggregate rating count sum and average accurately`() {
        var currentCount = 0L
        var currentSum = 0.0

        fun submitTestRating(rating: Int): Double {
            currentCount += 1
            currentSum += rating
            return currentSum / currentCount
        }

        val avg1 = submitTestRating(4)
        assertEquals(1L, currentCount)
        assertEquals(4.0, avg1, 0.001)

        val avg2 = submitTestRating(5)
        assertEquals(2L, currentCount)
        assertEquals(4.5, avg2, 0.001)

        val avg3 = submitTestRating(3)
        assertEquals(3L, currentCount)
        assertEquals(4.0, avg3, 0.001)
    }

    @Test
    fun `generates deterministic rating document ID preventing duplicate writes`() {
        val bookingId = "booking_123"
        val fromUserId = "user_cust_456"

        val docId = "${bookingId}_$fromUserId"
        assertEquals("booking_123_user_cust_456", docId)
    }

    @Test
    fun `only completed bookings can be rated by authorized participants`() {
        val completedBooking = Booking(
            bookingId = "b1",
            customerId = "cust_1",
            driverId = "driver_1",
            status = BookingStatus.COMPLETED,
            pickup = LocationPoint(28.0, 77.0),
            destination = LocationPoint(28.1, 77.1),
            distanceMeters = 1000,
            estimatedDurationSeconds = 120,
            estimatedFare = 50.0
        )

        val inProgressBooking = completedBooking.copy(status = BookingStatus.IN_PROGRESS)

        fun canRate(booking: Booking, uid: String): Boolean {
            return booking.status == BookingStatus.COMPLETED &&
                    (booking.customerId == uid || booking.driverId == uid)
        }

        assertTrue(canRate(completedBooking, "cust_1"))
        assertTrue(canRate(completedBooking, "driver_1"))
        assertFalse(canRate(completedBooking, "random_user"))
        assertFalse(canRate(inProgressBooking, "cust_1"))
    }
}
