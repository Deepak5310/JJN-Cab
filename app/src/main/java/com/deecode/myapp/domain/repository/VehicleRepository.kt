package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeDriverVehicle(driverId: String): Flow<Resource<Vehicle?>>
    suspend fun getDriverVehicle(driverId: String): Resource<Vehicle?>
    suspend fun saveVehicle(vehicle: Vehicle): Resource<Unit>
}
