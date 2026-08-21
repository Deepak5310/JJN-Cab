package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.DriverAvailability
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
}
