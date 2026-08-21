package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.DriverLocationDto
import kotlinx.coroutines.flow.Flow

interface DriverTrackingRemoteDataSource {
    suspend fun pushDriverLocation(driverLocationDto: DriverLocationDto): Resource<Unit>
    fun observeDriverLocation(bookingId: String): Flow<DriverLocationDto?>
    suspend fun clearDriverLocation(bookingId: String): Resource<Unit>
}
