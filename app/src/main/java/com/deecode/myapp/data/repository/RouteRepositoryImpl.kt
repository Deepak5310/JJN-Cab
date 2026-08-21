package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.route.RouteDataSource
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.RouteInfo
import com.deecode.myapp.domain.repository.RouteRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routeDataSource: RouteDataSource,
    private val dispatchers: DispatcherProvider
) : RouteRepository {

    override suspend fun calculateRoute(
        origin: LocationPoint,
        destination: LocationPoint
    ): Resource<RouteInfo> = withContext(dispatchers.io) {
        routeDataSource.calculateRoute(origin, destination)
    }
}
