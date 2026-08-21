package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint

interface LocationRepository {
    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): Resource<LocationPoint>
}
