package com.deecode.myapp.data.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.remote.DriverRemoteDataSource
import com.deecode.myapp.data.model.DriverDto
import com.deecode.myapp.domain.model.DriverAvailability
import com.deecode.myapp.domain.repository.DriverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DriverRepositoryImpl @Inject constructor(
    private val remoteDataSource: DriverRemoteDataSource
) : DriverRepository {

    override suspend fun setAvailability(driverId: String, isOnline: Boolean): Resource<Unit> {
        return remoteDataSource.setAvailability(driverId, isOnline)
    }

    override fun observeAvailability(driverId: String): Flow<Resource<DriverAvailability>> {
        return remoteDataSource.observeAvailability(driverId)
            .map<DriverDto?, Resource<DriverAvailability>> { dto ->
                if (dto != null) {
                    Resource.Success(dto.toDomain())
                } else {
                    Resource.Success(DriverAvailability(driverId = driverId, isOnline = false))
                }
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "Error observing driver availability", e))
            }
    }

    override suspend fun getAvailability(driverId: String): Resource<DriverAvailability> {
        return when (val result = remoteDataSource.getAvailability(driverId)) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message, result.cause)
            is Resource.Loading -> Resource.Loading
        }
    }

    private fun DriverDto.toDomain(): DriverAvailability {
        return DriverAvailability(
            driverId = driverId,
            isOnline = isOnline,
            updatedAt = updatedAt?.toDate()?.time
        )
    }
}
