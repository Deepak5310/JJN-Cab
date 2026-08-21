package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.DriverLocation
import com.deecode.myapp.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface DriverTrackingRepository {
    suspend fun pushDriverLocation(
        bookingId: String,
        driverId: String,
        customerId: String,
        location: LocationPoint,
        bearing: Float = 0f,
        speed: Float = 0f
    ): Resource<Unit>

    fun observeDriverLocation(bookingId: String): Flow<Resource<DriverLocation?>>
    suspend fun clearDriverLocation(bookingId: String): Resource<Unit>
}
