package com.deecode.myapp.data.datasource.route

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.RouteInfo

interface RouteDataSource {
    suspend fun calculateRoute(
        origin: LocationPoint,
        destination: LocationPoint
    ): Resource<RouteInfo>
}
