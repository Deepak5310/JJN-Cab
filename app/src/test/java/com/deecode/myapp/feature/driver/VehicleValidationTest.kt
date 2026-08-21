package com.deecode.myapp.feature.driver

import com.deecode.myapp.data.model.VehicleDto
import com.deecode.myapp.domain.model.Vehicle
import com.deecode.myapp.feature.driver.vehicle.DriverVehicleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleValidationTest {

    private val plateRegex = Regex("^[A-Z]{2}[ -]?[0-9]{1,2}[ -]?[A-Z]{1,3}[ -]?[0-9]{4}$", RegexOption.IGNORE_CASE)

    @Test
    fun `validates Indian vehicle registration plate formats`() {
        // Valid formats
        assertTrue(plateRegex.matches("DL 01 AB 1234"))
        assertTrue(plateRegex.matches("MH12DE1234"))
        assertTrue(plateRegex.matches("KA-05-MJ-5678"))
        assertTrue(plateRegex.matches("UP 16 A 9999"))
        assertTrue(plateRegex.matches("HR 26 CD 0001"))

        // Invalid formats
        assertFalse(plateRegex.matches("12345"))
        assertFalse(plateRegex.matches("INVALID_PLATE"))
        assertFalse(plateRegex.matches("DL 01 1234")) // Missing series letters
        assertFalse(plateRegex.matches("D 01 AB 1234")) // State code must be 2 letters
    }

    @Test
    fun `validates DriverVehicleUiState form completion`() {
        val invalidState = DriverVehicleUiState(
            makeModel = "",
            registrationNumber = "DL 01 AB 1234",
            color = "White"
        )
        assertFalse(invalidState.isValid)

        val validState = DriverVehicleUiState(
            vehicleType = "SEDAN",
            makeModel = "Maruti Suzuki Dzire",
            registrationNumber = "DL 01 AB 1234",
            color = "White"
        )
        assertTrue(validState.isValid)
    }

    @Test
    fun `verifies domain Vehicle to VehicleDto mapping`() {
        val domainVehicle = Vehicle(
            driverId = "driver_123",
            vehicleType = "SUV",
            makeModel = "Hyundai Creta",
            registrationNumber = "DL 01 AB 9999",
            color = "Polar White"
        )

        val dtoMap = VehicleDto.fromDomain(domainVehicle)
        assertEquals("driver_123", dtoMap["driverId"])
        assertEquals("SUV", dtoMap["vehicleType"])
        assertEquals("Hyundai Creta", dtoMap["makeModel"])
        assertEquals("DL 01 AB 9999", dtoMap["registrationNumber"])
        assertEquals("Polar White", dtoMap["color"])
        assertNotNull(dtoMap["updatedAt"])
    }
}
