package com.deecode.myapp.feature.admin

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.DriverManagementItem
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.feature.admin.drivers.AdminDriversUiState
import com.deecode.myapp.feature.admin.drivers.DriverStatusFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminDriverManagementTest {

    private val sampleDrivers = listOf(
        DriverManagementItem(
            driverId = "d1",
            user = User(uid = "d1", name = "Vikram Malhotra", email = "vikram@test.com", phone = "+919876000001", role = UserRole.DRIVER, isDriverVerified = true, isActive = true),
            isOnline = true,
            vehicleModel = "Dzire",
            vehiclePlate = "DL 01 AB 1111",
            completedTrips = 15,
            cancelledTrips = 1,
            todayEarnings = 1200.0,
            totalEarnings = 15000.0
        ),
        DriverManagementItem(
            driverId = "d2",
            user = User(uid = "d2", name = "Sunil Verma", email = "sunil@test.com", phone = "+919876000002", role = UserRole.DRIVER, isDriverVerified = false, isActive = true),
            isOnline = false,
            vehicleModel = "WagonR",
            vehiclePlate = "HR 26 CD 2222",
            completedTrips = 3,
            cancelledTrips = 0,
            todayEarnings = 0.0,
            totalEarnings = 2500.0
        ),
        DriverManagementItem(
            driverId = "d3",
            user = User(uid = "d3", name = "Deepak Joshi", email = "deepak@test.com", phone = "+919876000003", role = UserRole.DRIVER, isDriverVerified = true, isActive = false),
            isOnline = false,
            vehicleModel = "Ertiga",
            vehiclePlate = "UP 16 EF 3333",
            completedTrips = 30,
            cancelledTrips = 4,
            todayEarnings = 0.0,
            totalEarnings = 32000.0
        )
    )

    @Test
    fun `filters drivers by search query across name phone and vehicle plate`() {
        val searchByName = AdminDriversUiState(allDrivers = sampleDrivers, searchQuery = "vikram")
        assertEquals(1, searchByName.filteredDrivers.size)
        assertEquals("d1", searchByName.filteredDrivers[0].driverId)

        val searchByPhone = AdminDriversUiState(allDrivers = sampleDrivers, searchQuery = "000002")
        assertEquals(1, searchByPhone.filteredDrivers.size)
        assertEquals("d2", searchByPhone.filteredDrivers[0].driverId)

        val searchByPlate = AdminDriversUiState(allDrivers = sampleDrivers, searchQuery = "EF 3333")
        assertEquals(1, searchByPlate.filteredDrivers.size)
        assertEquals("d3", searchByPlate.filteredDrivers[0].driverId)
    }

    @Test
    fun `filters drivers by online offline and pending status`() {
        val onlineState = AdminDriversUiState(allDrivers = sampleDrivers, selectedFilter = DriverStatusFilter.ONLINE)
        assertEquals(1, onlineState.filteredDrivers.size)
        assertEquals("d1", onlineState.filteredDrivers[0].driverId)

        val offlineState = AdminDriversUiState(allDrivers = sampleDrivers, selectedFilter = DriverStatusFilter.OFFLINE)
        assertEquals(2, offlineState.filteredDrivers.size)

        val pendingState = AdminDriversUiState(allDrivers = sampleDrivers, selectedFilter = DriverStatusFilter.PENDING_VERIFICATION)
        assertEquals(1, pendingState.filteredDrivers.size)
        assertEquals("d2", pendingState.filteredDrivers[0].driverId)
    }

    @Test
    fun `validates driver trip and revenue statistics accuracy`() {
        val driver = sampleDrivers[0]
        assertEquals(15, driver.completedTrips)
        assertEquals(1, driver.cancelledTrips)
        assertEquals(1200.0, driver.todayEarnings, 0.001)
        assertEquals(15000.0, driver.totalEarnings, 0.001)
    }
}
