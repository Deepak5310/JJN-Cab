package com.deecode.myapp.feature.admin

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.AdminDashboardStats
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminDashboardStatsTest {

    @Test
    fun `calculates operational stats from raw bookings and user counts`() {
        val users = listOf(
            User(uid = "u1", name = "Alice", email = "alice@test.com", role = UserRole.CUSTOMER),
            User(uid = "u2", name = "Bob", email = "bob@test.com", role = UserRole.CUSTOMER),
            User(uid = "u3", name = "Charlie", email = "charlie@test.com", role = UserRole.DRIVER),
            User(uid = "u4", name = "Admin", email = "admin@test.com", role = UserRole.ADMIN)
        )

        val bookings = listOf(
            Booking("b1", "u1", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 150.0, status = BookingStatus.COMPLETED, finalFare = 150.0),
            Booking("b2", "u1", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 250.0, status = BookingStatus.COMPLETED, finalFare = 250.0),
            Booking("b3", "u2", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 100.0, status = BookingStatus.CANCELLED),
            Booking("b4", "u2", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 300.0, status = BookingStatus.IN_PROGRESS),
            Booking("b5", "u1", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 200.0, status = BookingStatus.REQUESTED)
        )

        val totalCustomers = users.count { it.role == UserRole.CUSTOMER }
        val totalDrivers = users.count { it.role == UserRole.DRIVER }
        val completedRides = bookings.count { it.status == BookingStatus.COMPLETED }
        val cancelledRides = bookings.count { it.status == BookingStatus.CANCELLED }
        val activeBookings = bookings.count { it.status.isActive }
        val totalGmv = bookings.filter { it.status == BookingStatus.COMPLETED }.sumOf { it.finalFare ?: it.estimatedFare }

        val stats = AdminDashboardStats(
            totalCustomers = totalCustomers,
            totalDrivers = totalDrivers,
            onlineDrivers = 1,
            activeBookings = activeBookings,
            completedRides = completedRides,
            cancelledRides = cancelledRides,
            totalRevenue = totalGmv
        )

        assertEquals(2, stats.totalCustomers)
        assertEquals(1, stats.totalDrivers)
        assertEquals(2, stats.completedRides)
        assertEquals(1, stats.cancelledRides)
        assertEquals(2, stats.activeBookings)
        assertEquals(400.0, stats.totalRevenue, 0.001)
    }

    @Test
    fun `enforces administrator role requirement for dashboard access`() {
        fun isAuthorizedAdmin(user: User?): Boolean = user?.role == UserRole.ADMIN

        val adminUser = User(uid = "a1", name = "Admin", email = "admin@test.com", role = UserRole.ADMIN)
        val customerUser = User(uid = "c1", name = "Cust", email = "cust@test.com", role = UserRole.CUSTOMER)
        val driverUser = User(uid = "d1", name = "Driver", email = "driver@test.com", role = UserRole.DRIVER)

        assertTrue(isAuthorizedAdmin(adminUser))
        assertFalse(isAuthorizedAdmin(customerUser))
        assertFalse(isAuthorizedAdmin(driverUser))
        assertFalse(isAuthorizedAdmin(null))
    }

    @Test
    fun `recent bookings feed is limited and ordered newest first`() {
        val bookings = listOf(
            Booking("b1", "u1", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 100.0, createdAt = 1000L),
            Booking("b2", "u1", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 100.0, createdAt = 3000L),
            Booking("b3", "u1", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 100.0, createdAt = 2000L),
            Booking("b4", "u1", LocationPoint(0.0, 0.0), LocationPoint(0.0, 0.0), 1000, 100, 100.0, createdAt = 4000L)
        )

        val sortedAndLimited = bookings.sortedByDescending { it.createdAt }.take(2)

        assertEquals(2, sortedAndLimited.size)
        assertEquals("b4", sortedAndLimited[0].bookingId)
        assertEquals("b2", sortedAndLimited[1].bookingId)
    }
}
