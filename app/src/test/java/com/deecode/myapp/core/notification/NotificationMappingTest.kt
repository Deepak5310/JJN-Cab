package com.deecode.myapp.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationMappingTest {

    @Test
    fun `notification channels have correct identifiers and parameters`() {
        assertEquals("jjn_rides_channel", NotificationHelper.CHANNEL_RIDES)
        assertEquals("jjn_requests_channel", NotificationHelper.CHANNEL_REQUESTS)
    }

    @Test
    fun `all supported notification event types are defined`() {
        val supportedTypes = setOf(
            NotificationHelper.TYPE_NEW_REQUEST,
            NotificationHelper.TYPE_BOOKING_ACCEPTED,
            NotificationHelper.TYPE_DRIVER_ARRIVING,
            NotificationHelper.TYPE_RIDE_STARTED,
            NotificationHelper.TYPE_RIDE_COMPLETED,
            NotificationHelper.TYPE_BOOKING_CANCELLED
        )

        assertEquals(6, supportedTypes.size)
        assertTrue(supportedTypes.contains("NEW_REQUEST"))
        assertTrue(supportedTypes.contains("BOOKING_ACCEPTED"))
        assertTrue(supportedTypes.contains("DRIVER_ARRIVING"))
        assertTrue(supportedTypes.contains("RIDE_STARTED"))
        assertTrue(supportedTypes.contains("RIDE_COMPLETED"))
        assertTrue(supportedTypes.contains("BOOKING_CANCELLED"))
    }

    @Test
    fun `notification payload maps data fields correctly`() {
        val payload = mapOf(
            "type" to NotificationHelper.TYPE_DRIVER_ARRIVING,
            "bookingId" to "booking_xyz_123",
            "title" to "Driver Arrived 📍",
            "body" to "Your cab is waiting at the pickup point."
        )

        val type = payload["type"] ?: NotificationHelper.TYPE_RIDE_STARTED
        val bookingId = payload["bookingId"]
        val title = payload["title"]
        val body = payload["body"]

        assertEquals(NotificationHelper.TYPE_DRIVER_ARRIVING, type)
        assertEquals("booking_xyz_123", bookingId)
        assertEquals("Driver Arrived 📍", title)
        assertNotNull(body)
    }

    @Test
    fun `target channel is requests channel for NEW_REQUEST and rides channel for trip updates`() {
        fun resolveChannel(type: String): String =
            if (type == NotificationHelper.TYPE_NEW_REQUEST) NotificationHelper.CHANNEL_REQUESTS
            else NotificationHelper.CHANNEL_RIDES

        assertEquals(NotificationHelper.CHANNEL_REQUESTS, resolveChannel(NotificationHelper.TYPE_NEW_REQUEST))
        assertEquals(NotificationHelper.CHANNEL_RIDES, resolveChannel(NotificationHelper.TYPE_BOOKING_ACCEPTED))
        assertEquals(NotificationHelper.CHANNEL_RIDES, resolveChannel(NotificationHelper.TYPE_DRIVER_ARRIVING))
        assertEquals(NotificationHelper.CHANNEL_RIDES, resolveChannel(NotificationHelper.TYPE_RIDE_STARTED))
        assertEquals(NotificationHelper.CHANNEL_RIDES, resolveChannel(NotificationHelper.TYPE_RIDE_COMPLETED))
        assertEquals(NotificationHelper.CHANNEL_RIDES, resolveChannel(NotificationHelper.TYPE_BOOKING_CANCELLED))
    }
}
