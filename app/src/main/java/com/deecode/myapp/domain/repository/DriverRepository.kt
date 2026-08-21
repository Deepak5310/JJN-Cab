package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.DriverAvailability
import kotlinx.coroutines.flow.Flow

interface DriverRepository {
    suspend fun setAvailability(driverId: String, isOnline: Boolean): Resource<Unit>
    fun observeAvailability(driverId: String): Flow<Resource<DriverAvailability>>
    suspend fun getAvailability(driverId: String): Resource<DriverAvailability>
}
