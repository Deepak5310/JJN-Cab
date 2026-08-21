package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class BookingMappingTest {

    @Test
    fun `domain Booking correctly maps to BookingDto`() {
        val domainBooking = Booking(
            bookingId = "booking_123",
            customerId = "user_456",
            pickup = LocationPoint(28.6139, 77.2090, "Connaught Place, Delhi"),
            destination = LocationPoint(28.7041, 77.1025, "Pitampura, Delhi"),
            distanceMeters = 15400,
            estimatedDurationSeconds = 1800,
            estimatedFare = 245.0,
            status = BookingStatus.REQUESTED,
            driverId = null
        )

        val dto = BookingDto.fromDomain(domainBooking)

        assertEquals("booking_123", dto.bookingId)
        assertEquals("user_456", dto.customerId)
        assertEquals(28.6139, dto.pickup.latitude, 0.0001)
        assertEquals(77.2090, dto.pickup.longitude, 0.0001)
        assertEquals("Connaught Place, Delhi", dto.pickup.address)
        assertEquals(28.7041, dto.destination.latitude, 0.0001)
        assertEquals(77.1025, dto.destination.longitude, 0.0001)
        assertEquals("Pitampura, Delhi", dto.destination.address)
        assertEquals(15400, dto.distanceMeters)
        assertEquals(1800L, dto.estimatedDurationSeconds)
        assertEquals(245.0, dto.estimatedFare, 0.001)
        assertEquals("REQUESTED", dto.status)
        assertNull(dto.driverId)
    }

    @Test
    fun `BookingDto correctly maps to domain Booking`() {
        val testDate = Date(1700000000000L)
        val timestamp = Timestamp(testDate)

        val dto = BookingDto(
            bookingId = "booking_999",
            customerId = "user_777",
            pickup = LocationPointDto(19.0760, 72.8777, "Mumbai CST"),
            destination = LocationPointDto(19.0896, 72.8656, "Bandra, Mumbai"),
            distanceMeters = 8200,
            estimatedDurationSeconds = 1200,
            estimatedFare = 160.0,
            status = "ACCEPTED",
            driverId = "driver_333",
            createdAt = timestamp,
            updatedAt = timestamp
        )

        val domain = dto.toDomain()

        assertEquals("booking_999", domain.bookingId)
        assertEquals("user_777", domain.customerId)
        assertEquals(19.0760, domain.pickup.latitude, 0.0001)
        assertEquals("Mumbai CST", domain.pickup.address)
        assertEquals(19.0896, domain.destination.latitude, 0.0001)
        assertEquals("Bandra, Mumbai", domain.destination.address)
        assertEquals(8200, domain.distanceMeters)
        assertEquals(1200L, domain.estimatedDurationSeconds)
        assertEquals(160.0, domain.estimatedFare, 0.001)
        assertEquals(BookingStatus.ACCEPTED, domain.status)
        assertEquals("driver_333", domain.driverId)
        assertEquals(1700000000000L, domain.createdAt)
        assertEquals(1700000000000L, domain.updatedAt)
    }

    @Test
    fun `invalid or unknown status string safely defaults to REQUESTED`() {
        val dto = BookingDto(
            bookingId = "booking_invalid",
            customerId = "user_1",
            status = "SOME_UNKNOWN_STATUS"
        )

        val domain = dto.toDomain()
        assertEquals(BookingStatus.REQUESTED, domain.status)
    }

    @Test
    fun `BookingStatus isActive and isTerminal helpers evaluate accurately`() {
        assertTrue(BookingStatus.REQUESTED.isActive)
        assertTrue(BookingStatus.SEARCHING_DRIVER.isActive)
        assertTrue(BookingStatus.ACCEPTED.isActive)
        assertTrue(BookingStatus.DRIVER_ARRIVING.isActive)
        assertTrue(BookingStatus.IN_PROGRESS.isActive)

        assertFalse(BookingStatus.COMPLETED.isActive)
        assertFalse(BookingStatus.CANCELLED_BY_CUSTOMER.isActive)
        assertFalse(BookingStatus.CANCELLED_BY_DRIVER.isActive)
        assertFalse(BookingStatus.NO_DRIVERS_AVAILABLE.isActive)

        assertTrue(BookingStatus.COMPLETED.isTerminal)
        assertTrue(BookingStatus.CANCELLED_BY_CUSTOMER.isTerminal)
        assertTrue(BookingStatus.CANCELLED_BY_DRIVER.isTerminal)
        assertTrue(BookingStatus.NO_DRIVERS_AVAILABLE.isTerminal)

        assertFalse(BookingStatus.REQUESTED.isTerminal)
        assertFalse(BookingStatus.IN_PROGRESS.isTerminal)
    }

    @Test
    fun `eligible pending bookings filter requires REQUESTED status and null driverId`() {
        val eligibleBooking = BookingDto(bookingId = "1", status = "REQUESTED", driverId = null)
        val claimedBooking = BookingDto(bookingId = "2", status = "REQUESTED", driverId = "driver_1")
        val acceptedBooking = BookingDto(bookingId = "3", status = "ACCEPTED", driverId = "driver_2")
        val completedBooking = BookingDto(bookingId = "4", status = "COMPLETED", driverId = "driver_1")

        val allBookings = listOf(eligibleBooking, claimedBooking, acceptedBooking, completedBooking)
        val filtered = allBookings.filter { it.status == "REQUESTED" && it.driverId == null }

        assertEquals(1, filtered.size)
        assertEquals("1", filtered[0].bookingId)
    }

    @Test
    fun `driver accepting booking sets driverId and status to ACCEPTED`() {
        val initialDto = BookingDto(bookingId = "booking_1", status = "REQUESTED", driverId = null)
        val acceptedDto = initialDto.copy(driverId = "driver_win", status = "ACCEPTED")

        assertEquals("driver_win", acceptedDto.driverId)
        assertEquals("ACCEPTED", acceptedDto.status)
        assertTrue(acceptedDto.toDomain().status.isActive)
    }

    @Test
    fun `second driver attempting to claim already assigned booking is detected`() {
        val alreadyAssignedDto = BookingDto(bookingId = "booking_1", status = "ACCEPTED", driverId = "driver_first")
        val isClaimable = alreadyAssignedDto.driverId == null && alreadyAssignedDto.status == "REQUESTED"

        assertFalse(isClaimable)
    }
}
