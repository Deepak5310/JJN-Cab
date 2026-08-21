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

    @Test
    fun `status aliases map correctly to domain BookingStatus`() {
        val assignedDto = BookingDto(status = "ASSIGNED")
        val arrivingDto = BookingDto(status = "ARRIVING")
        val startedDto = BookingDto(status = "STARTED")

        assertEquals(BookingStatus.ACCEPTED, assignedDto.toDomain().status)
        assertEquals(BookingStatus.DRIVER_ARRIVING, arrivingDto.toDomain().status)
        assertEquals(BookingStatus.IN_PROGRESS, startedDto.toDomain().status)
    }

    @Test
    fun `active ride progression transitions evaluate correctly`() {
        fun isValidTransition(current: String, next: String): Boolean = when (next) {
            "DRIVER_ARRIVING", "ARRIVING" -> current in setOf("ACCEPTED", "ASSIGNED")
            "IN_PROGRESS", "STARTED" -> current in setOf("DRIVER_ARRIVING", "ARRIVING")
            else -> false
        }

        assertTrue(isValidTransition("ACCEPTED", "DRIVER_ARRIVING"))
        assertTrue(isValidTransition("ASSIGNED", "ARRIVING"))
        assertTrue(isValidTransition("DRIVER_ARRIVING", "IN_PROGRESS"))
        assertTrue(isValidTransition("ARRIVING", "STARTED"))

        assertFalse(isValidTransition("REQUESTED", "IN_PROGRESS"))
        assertFalse(isValidTransition("ACCEPTED", "IN_PROGRESS"))
        assertFalse(isValidTransition("IN_PROGRESS", "ACCEPTED"))
    }

    @Test
    fun `completed booking correctly maps finalFare, finalDistance, and finalDuration`() {
        val completedTimestamp = Timestamp(Date(1700005000000L))
        val dto = BookingDto(
            bookingId = "booking_comp_1",
            customerId = "user_100",
            status = "COMPLETED",
            driverId = "driver_200",
            estimatedFare = 250.0,
            distanceMeters = 12000,
            estimatedDurationSeconds = 1500L,
            completedAt = completedTimestamp,
            finalFare = 265.0,
            finalDistanceMeters = 12400,
            finalDurationSeconds = 1620L
        )

        val domain = dto.toDomain()

        assertEquals("booking_comp_1", domain.bookingId)
        assertEquals(BookingStatus.COMPLETED, domain.status)
        assertFalse(domain.status.isActive)
        assertTrue(domain.status.isTerminal)
        assertEquals(1700005000000L, domain.completedAt)
        assertEquals(265.0, domain.finalFare ?: 0.0, 0.01)
        assertEquals(12400, domain.finalDistanceMeters)
        assertEquals(1620L, domain.finalDurationSeconds)
    }

    @Test
    fun `completion transition requires IN_PROGRESS or STARTED status`() {
        fun canCompleteRide(status: String): Boolean =
            status == "IN_PROGRESS" || status == "STARTED"

        assertTrue(canCompleteRide("IN_PROGRESS"))
        assertTrue(canCompleteRide("STARTED"))

        assertFalse(canCompleteRide("REQUESTED"))
        assertFalse(canCompleteRide("ACCEPTED"))
        assertFalse(canCompleteRide("DRIVER_ARRIVING"))
        assertFalse(canCompleteRide("COMPLETED"))
    }

    @Test
    fun `cancelled booking correctly maps cancelledAt, cancelledBy, and cancellationReason`() {
        val cancelledTimestamp = Timestamp(Date(1700008000000L))
        val dto = BookingDto(
            bookingId = "booking_cancel_1",
            customerId = "user_100",
            status = "CANCELLED_BY_CUSTOMER",
            driverId = "driver_200",
            cancelledAt = cancelledTimestamp,
            cancelledBy = "user_100",
            cancellationReason = "Change of plans"
        )

        val domain = dto.toDomain()

        assertEquals("booking_cancel_1", domain.bookingId)
        assertEquals(BookingStatus.CANCELLED_BY_CUSTOMER, domain.status)
        assertFalse(domain.status.isActive)
        assertTrue(domain.status.isTerminal)
        assertEquals(1700008000000L, domain.cancelledAt)
        assertEquals("user_100", domain.cancelledBy)
        assertEquals("Change of plans", domain.cancellationReason)
    }

    @Test
    fun `cancellation is allowed from all active states but prohibited once COMPLETED or already cancelled`() {
        fun canCancelBooking(status: String): Boolean {
            if (status == "COMPLETED") return false
            if (status in setOf("CANCELLED", "CANCELLED_BY_CUSTOMER", "CANCELLED_BY_DRIVER", "NO_DRIVERS_AVAILABLE")) return false
            return true
        }

        assertTrue(canCancelBooking("REQUESTED"))
        assertTrue(canCancelBooking("SEARCHING_DRIVER"))
        assertTrue(canCancelBooking("ACCEPTED"))
        assertTrue(canCancelBooking("ASSIGNED"))
        assertTrue(canCancelBooking("DRIVER_ARRIVING"))
        assertTrue(canCancelBooking("ARRIVING"))
        assertTrue(canCancelBooking("IN_PROGRESS"))
        assertTrue(canCancelBooking("STARTED"))

        assertFalse(canCancelBooking("COMPLETED"))
        assertFalse(canCancelBooking("CANCELLED"))
        assertFalse(canCancelBooking("CANCELLED_BY_CUSTOMER"))
        assertFalse(canCancelBooking("CANCELLED_BY_DRIVER"))
        assertFalse(canCancelBooking("NO_DRIVERS_AVAILABLE"))
    }
}
