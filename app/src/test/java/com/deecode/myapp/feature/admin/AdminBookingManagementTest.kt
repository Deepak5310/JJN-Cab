package com.deecode.myapp.feature.admin

import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.feature.admin.bookings.AdminBookingsUiState
import com.deecode.myapp.feature.admin.bookings.BookingDateFilter
import com.deecode.myapp.feature.admin.bookings.BookingStatusFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AdminBookingManagementTest {

    private val now = System.currentTimeMillis()
    private val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val sampleBookings = listOf(
        Booking(
            bookingId = "bk_001",
            customerId = "cust_alpha",
            driverId = "driver_101",
            status = BookingStatus.IN_PROGRESS,
            pickup = LocationPoint(28.6139, 77.2090, address = "Connaught Place, New Delhi"),
            destination = LocationPoint(28.5355, 77.3910, address = "Sector 62, Noida"),
            distanceMeters = 20000,
            estimatedDurationSeconds = 1800,
            estimatedFare = 350.0,
            createdAt = startOfToday + 3600000L
        ),
        Booking(
            bookingId = "bk_002",
            customerId = "cust_beta",
            driverId = "driver_102",
            status = BookingStatus.COMPLETED,
            pickup = LocationPoint(28.4595, 77.0266, address = "Cyber Hub, Gurgaon"),
            destination = LocationPoint(28.6139, 77.2090, address = "Connaught Place, New Delhi"),
            distanceMeters = 25000,
            estimatedDurationSeconds = 2400,
            estimatedFare = 450.0,
            finalFare = 450.0,
            createdAt = startOfToday - 86400000L * 2 // 2 days ago
        ),
        Booking(
            bookingId = "bk_003",
            customerId = "cust_gamma",
            driverId = null,
            status = BookingStatus.CANCELLED,
            pickup = LocationPoint(28.7041, 77.1025, address = "Rohini Sector 10"),
            destination = LocationPoint(28.6139, 77.2090, address = "New Delhi Railway Station"),
            distanceMeters = 15000,
            estimatedDurationSeconds = 1500,
            estimatedFare = 280.0,
            createdAt = startOfToday - 86400000L * 10 // 10 days ago
        )
    )

    @Test
    fun `filters bookings by status correctly`() {
        val activeState = AdminBookingsUiState(allBookings = sampleBookings, statusFilter = BookingStatusFilter.ACTIVE)
        assertEquals(1, activeState.filteredBookings.size)
        assertEquals("bk_001", activeState.filteredBookings[0].bookingId)

        val completedState = AdminBookingsUiState(allBookings = sampleBookings, statusFilter = BookingStatusFilter.COMPLETED)
        assertEquals(1, completedState.filteredBookings.size)
        assertEquals("bk_002", completedState.filteredBookings[0].bookingId)

        val cancelledState = AdminBookingsUiState(allBookings = sampleBookings, statusFilter = BookingStatusFilter.CANCELLED)
        assertEquals(1, cancelledState.filteredBookings.size)
        assertEquals("bk_003", cancelledState.filteredBookings[0].bookingId)
    }

    @Test
    fun `filters bookings by date range`() {
        val todayState = AdminBookingsUiState(allBookings = sampleBookings, dateFilter = BookingDateFilter.TODAY)
        assertEquals(1, todayState.filteredBookings.size)
        assertEquals("bk_001", todayState.filteredBookings[0].bookingId)

        val allTimeState = AdminBookingsUiState(allBookings = sampleBookings, dateFilter = BookingDateFilter.ALL)
        assertEquals(3, allTimeState.filteredBookings.size)
    }

    @Test
    fun `searches bookings by address or driver id`() {
        val searchByAddress = AdminBookingsUiState(allBookings = sampleBookings, searchQuery = "Cyber Hub")
        assertEquals(1, searchByAddress.filteredBookings.size)
        assertEquals("bk_002", searchByAddress.filteredBookings[0].bookingId)

        val searchByDriver = AdminBookingsUiState(allBookings = sampleBookings, searchQuery = "driver_101")
        assertEquals(1, searchByDriver.filteredBookings.size)
        assertEquals("bk_001", searchByDriver.filteredBookings[0].bookingId)
    }

    @Test
    fun `validates cancellation permissions on active bookings`() {
        fun canCancel(status: BookingStatus): Boolean {
            return status.isActive && status != BookingStatus.COMPLETED
        }

        assertTrue(canCancel(BookingStatus.REQUESTED))
        assertTrue(canCancel(BookingStatus.ACCEPTED))
        assertTrue(canCancel(BookingStatus.IN_PROGRESS))
        assertFalse(canCancel(BookingStatus.COMPLETED))
        assertFalse(canCancel(BookingStatus.CANCELLED))
    }
}
