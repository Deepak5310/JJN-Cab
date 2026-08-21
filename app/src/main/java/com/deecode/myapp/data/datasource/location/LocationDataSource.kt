package com.deecode.myapp.data.datasource.location

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): Resource<LocationPoint>
    fun observeLocationUpdates(intervalMs: Long = 4000L, minDistanceMeters: Float = 5f): Flow<LocationPoint>
}
