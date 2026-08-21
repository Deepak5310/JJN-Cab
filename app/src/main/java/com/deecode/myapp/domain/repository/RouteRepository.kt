package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.RouteInfo

interface RouteRepository {
    suspend fun calculateRoute(
        origin: LocationPoint,
        destination: LocationPoint
    ): Resource<RouteInfo>
}
