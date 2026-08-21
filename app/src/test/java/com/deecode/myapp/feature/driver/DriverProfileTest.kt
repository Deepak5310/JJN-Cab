package com.deecode.myapp.feature.driver

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.domain.model.Vehicle
import com.deecode.myapp.feature.driver.profile.DriverProfileUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverProfileTest {

    private val phoneRegex = Regex("^[+]?[0-9]{10,13}$")

    @Test
    fun `validates driver phone number formats`() {
        assertTrue(phoneRegex.matches("+919876543210"))
        assertTrue(phoneRegex.matches("9876543210"))
        assertTrue(phoneRegex.matches("01123456789"))

        assertFalse(phoneRegex.matches("12345"))
        assertFalse(phoneRegex.matches("invalid_phone"))
    }

    @Test
    fun `extracts driver initials accurately`() {
        val driverState = DriverProfileUiState(
            user = User(uid = "d1", name = "Vikram Malhotra", email = "vikram@test.com", phone = "+919876543210", role = UserRole.DRIVER)
        )
        assertEquals("VM", driverState.initials)

        val emptyDriverState = DriverProfileUiState(
            user = User(uid = "d2", name = "", email = "test@test.com", phone = "+919876543210", role = UserRole.DRIVER)
        )
        assertEquals("D", emptyDriverState.initials)
    }

    @Test
    fun `validates DriverProfileUiState form validity`() {
        val validState = DriverProfileUiState(
            editName = "Sunil Verma",
            editPhone = "+919876543210"
        )
        assertTrue(validState.isValid)

        val shortNameState = DriverProfileUiState(
            editName = "S",
            editPhone = "+919876543210"
        )
        assertFalse(shortNameState.isValid)

        val blankPhoneState = DriverProfileUiState(
            editName = "Sunil Verma",
            editPhone = ""
        )
        assertFalse(blankPhoneState.isValid)
    }

    @Test
    fun `validates vehicle attachment in DriverProfileUiState`() {
        val vehicle = Vehicle(
            driverId = "d1",
            vehicleType = "SEDAN",
            makeModel = "Maruti Suzuki Dzire",
            registrationNumber = "DL 01 AB 1234",
            color = "White"
        )

        val state = DriverProfileUiState(
            user = User(uid = "d1", name = "Vikram Malhotra", email = "vikram@test.com", role = UserRole.DRIVER),
            vehicle = vehicle,
            completedRides = 25
        )

        assertEquals("DL 01 AB 1234", state.vehicle?.formattedPlate)
        assertEquals(25, state.completedRides)
    }
}
