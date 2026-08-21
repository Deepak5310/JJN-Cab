package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.DriverDto
import kotlinx.coroutines.flow.Flow

interface DriverRemoteDataSource {
    suspend fun setAvailability(driverId: String, isOnline: Boolean): Resource<Unit>
    fun observeAvailability(driverId: String): Flow<DriverDto?>
    suspend fun getAvailability(driverId: String): Resource<DriverDto>
}
