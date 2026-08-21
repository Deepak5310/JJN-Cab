package com.deecode.myapp.data.datasource.location

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint

interface LocationDataSource {
    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): Resource<LocationPoint>
}
