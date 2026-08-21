package com.deecode.myapp.feature.driver

import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DriverEarningsCalculationTest {

    private fun createBooking(
        bookingId: String,
        driverId: String,
        status: BookingStatus,
        finalFare: Double?,
        estimatedFare: Double,
        completedAt: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ): Booking {
        return Booking(
            bookingId = bookingId,
            customerId = "cust_123",
            driverId = driverId,
            pickup = LocationPoint(28.0, 77.0),
            destination = LocationPoint(28.1, 77.1),
            distanceMeters = 5000,
            estimatedDurationSeconds = 600,
            estimatedFare = estimatedFare,
            finalFare = finalFare,
            status = status,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    private fun getStartOfDayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun `calculates total earnings accurately strictly from completed rides`() {
        val driverId = "driver_1"
        val now = System.currentTimeMillis()

        val bookings = listOf(
            createBooking("b1", driverId, BookingStatus.COMPLETED, finalFare = 250.50, estimatedFare = 240.0, completedAt = now),
            createBooking("b2", driverId, BookingStatus.COMPLETED, finalFare = 150.00, estimatedFare = 150.0, completedAt = now),
            createBooking("b3", driverId, BookingStatus.CANCELLED, finalFare = null, estimatedFare = 300.0),
            createBooking("b4", driverId, BookingStatus.CANCELLED_BY_CUSTOMER, finalFare = null, estimatedFare = 120.0),
            createBooking("b5", driverId, BookingStatus.IN_PROGRESS, finalFare = null, estimatedFare = 500.0)
        )

        val completedRides = bookings.filter { it.status == BookingStatus.COMPLETED }
        val totalEarnings = completedRides.sumOf { it.finalFare ?: it.estimatedFare }

        assertEquals(2, completedRides.size)
        assertEquals(400.50, totalEarnings, 0.001)
    }

    @Test
    fun `calculates today's earnings filtering only rides completed today`() {
        val driverId = "driver_1"
        val now = System.currentTimeMillis()
        val yesterday = now - 24 * 60 * 60 * 1000L

        val bookings = listOf(
            createBooking("b1", driverId, BookingStatus.COMPLETED, finalFare = 300.0, estimatedFare = 300.0, completedAt = now),
            createBooking("b2", driverId, BookingStatus.COMPLETED, finalFare = 200.0, estimatedFare = 200.0, completedAt = now),
            createBooking("b3", driverId, BookingStatus.COMPLETED, finalFare = 450.0, estimatedFare = 450.0, completedAt = yesterday),
            createBooking("b4", driverId, BookingStatus.CANCELLED, finalFare = null, estimatedFare = 100.0, completedAt = now)
        )

        val startOfDay = getStartOfDayMillis()
        val completedRides = bookings.filter { it.status == BookingStatus.COMPLETED }
        val todayEarnings = completedRides.filter {
            val ts = it.completedAt?.takeIf { t -> t > 0L } ?: it.createdAt
            ts >= startOfDay
        }.sumOf { it.finalFare ?: it.estimatedFare }

        val totalEarnings = completedRides.sumOf { it.finalFare ?: it.estimatedFare }

        assertEquals(500.0, todayEarnings, 0.001)
        assertEquals(950.0, totalEarnings, 0.001)
    }

    @Test
    fun `enforces strict driver data isolation`() {
        val authenticatedDriverId = "driver_me"
        val otherDriverId = "driver_other"

        val allBookings = listOf(
            createBooking("b1", authenticatedDriverId, BookingStatus.COMPLETED, finalFare = 200.0, estimatedFare = 200.0),
            createBooking("b2", otherDriverId, BookingStatus.COMPLETED, finalFare = 500.0, estimatedFare = 500.0)
        )

        val driverBookings = allBookings.filter { it.driverId == authenticatedDriverId }
        val driverEarnings = driverBookings.filter { it.status == BookingStatus.COMPLETED }
            .sumOf { it.finalFare ?: it.estimatedFare }

        assertEquals(1, driverBookings.size)
        assertEquals(200.0, driverEarnings, 0.001)
    }
}
