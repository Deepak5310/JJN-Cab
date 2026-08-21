package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.DriverAvailability
import com.deecode.myapp.domain.model.LocationPoint
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class DriverMappingTest {

    @Test
    fun `driverDto defaults have isOnline false and null timestamp`() {
        val dto = DriverDto(driverId = "driver_123")
        assertEquals("driver_123", dto.driverId)
        assertFalse(dto.isOnline)
        assertNull(dto.updatedAt)
    }

    @Test
    fun `driverDto with online status and timestamp retains values`() {
        val now = Date()
        val timestamp = Timestamp(now)
        val dto = DriverDto(
            driverId = "driver_456",
            isOnline = true,
            updatedAt = timestamp
        )

        assertEquals("driver_456", dto.driverId)
        assertTrue(dto.isOnline)
        assertEquals(now.time, dto.updatedAt?.toDate()?.time)
    }

    @Test
    fun `driverAvailability domain model holds expected values`() {
        val availability = DriverAvailability(
            driverId = "driver_789",
            isOnline = true,
            updatedAt = 1700000000000L
        )

        assertEquals("driver_789", availability.driverId)
        assertTrue(availability.isOnline)
        assertEquals(1700000000000L, availability.updatedAt)
    }

    @Test
    fun `driverLocationDto correctly maps to and from LocationPoint and Domain`() {
        val point = LocationPoint(28.6139, 77.2090)
        val dto = DriverLocationDto.fromDomain(
            bookingId = "booking_100",
            driverId = "driver_200",
            customerId = "customer_300",
            point = point,
            bearing = 45f,
            speed = 12.5f
        )

        assertEquals("booking_100", dto.bookingId)
        assertEquals("driver_200", dto.driverId)
        assertEquals("customer_300", dto.customerId)
        assertEquals(28.6139, dto.latitude, 0.0001)
        assertEquals(77.2090, dto.longitude, 0.0001)
        assertEquals(45f, dto.bearing, 0.01f)
        assertEquals(12.5f, dto.speed, 0.01f)

        val domain = dto.toDomain()
        assertEquals("booking_100", domain.bookingId)
        assertEquals("driver_200", domain.driverId)
        assertEquals(28.6139, domain.latitude, 0.0001)

        val locPoint = dto.toLocationPoint()
        assertEquals(28.6139, locPoint.latitude, 0.0001)
        assertEquals(77.2090, locPoint.longitude, 0.0001)
    }

    @Test
    fun `location tracking active lifecycle condition is only met for ARRIVING and STARTED`() {
        fun isTrackingActive(status: BookingStatus): Boolean {
            return status == BookingStatus.DRIVER_ARRIVING || status == BookingStatus.IN_PROGRESS
        }

        assertTrue(isTrackingActive(BookingStatus.DRIVER_ARRIVING))
        assertTrue(isTrackingActive(BookingStatus.IN_PROGRESS))

        assertFalse(isTrackingActive(BookingStatus.REQUESTED))
        assertFalse(isTrackingActive(BookingStatus.ACCEPTED))
        assertFalse(isTrackingActive(BookingStatus.COMPLETED))
        assertFalse(isTrackingActive(BookingStatus.CANCELLED_BY_CUSTOMER))
        assertFalse(isTrackingActive(BookingStatus.CANCELLED_BY_DRIVER))
    }
}
