package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.location.LocationDataSource
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.repository.LocationRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource,
    private val dispatchers: DispatcherProvider
) : LocationRepository {

    override fun hasLocationPermission(): Boolean {
        return locationDataSource.hasLocationPermission()
    }

    override suspend fun getCurrentLocation(): Resource<LocationPoint> =
        withContext(dispatchers.io) {
            locationDataSource.getCurrentLocation()
        }

    override fun observeLocationUpdates(): kotlinx.coroutines.flow.Flow<LocationPoint> {
        return locationDataSource.observeLocationUpdates()
    }
}
