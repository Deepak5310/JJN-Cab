package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.remote.DriverTrackingRemoteDataSource
import com.deecode.myapp.data.model.DriverLocationDto
import com.deecode.myapp.domain.model.DriverLocation
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.repository.DriverTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DriverTrackingRepositoryImpl @Inject constructor(
    private val remoteDataSource: DriverTrackingRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : DriverTrackingRepository {

    override suspend fun pushDriverLocation(
        bookingId: String,
        driverId: String,
        customerId: String,
        location: LocationPoint,
        bearing: Float,
        speed: Float
    ): Resource<Unit> = withContext(dispatchers.io) {
        val dto = DriverLocationDto.fromDomain(
            bookingId = bookingId,
            driverId = driverId,
            customerId = customerId,
            point = location,
            bearing = bearing,
            speed = speed
        )
        remoteDataSource.pushDriverLocation(dto)
    }

    override fun observeDriverLocation(bookingId: String): Flow<Resource<DriverLocation?>> {
        return remoteDataSource.observeDriverLocation(bookingId)
            .map { dto ->
                Resource.Success(dto?.toDomain()) as Resource<DriverLocation?>
            }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Failed to observe driver location", it)) }
            .flowOn(dispatchers.io)
    }

    override suspend fun clearDriverLocation(bookingId: String): Resource<Unit> =
        withContext(dispatchers.io) {
            remoteDataSource.clearDriverLocation(bookingId)
        }
}
