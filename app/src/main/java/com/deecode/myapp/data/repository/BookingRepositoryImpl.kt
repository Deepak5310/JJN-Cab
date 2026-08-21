package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.remote.BookingRemoteDataSource
import com.deecode.myapp.data.model.BookingDto
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val remoteDataSource: BookingRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : BookingRepository {

    override suspend fun createBooking(booking: Booking): Resource<String> =
        withContext(dispatchers.io) {
            val dto = BookingDto.fromDomain(booking)
            remoteDataSource.createBooking(dto)
        }

    override suspend fun getBooking(bookingId: String): Resource<Booking> =
        withContext(dispatchers.io) {
            when (val result = remoteDataSource.getBooking(bookingId)) {
                is Resource.Success -> Resource.Success(result.data.toDomain())
                is Resource.Error -> Resource.Error(result.message, result.cause)
                is Resource.Loading -> Resource.Loading
            }
        }

    override fun observeBooking(bookingId: String): Flow<Resource<Booking>> {
        return remoteDataSource.observeBooking(bookingId)
            .map { dto ->
                if (dto != null) {
                    Resource.Success(dto.toDomain())
                } else {
                    Resource.Error("Booking not found")
                }
            }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Failed to observe booking", it)) }
            .flowOn(dispatchers.io)
    }

    override fun observeCustomerBookings(customerId: String): Flow<Resource<List<Booking>>> {
        return remoteDataSource.observeCustomerBookings(customerId)
            .map { dtoList ->
                val domainList = dtoList.map { it.toDomain() }
                Resource.Success(domainList) as Resource<List<Booking>>
            }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Failed to observe customer bookings", it)) }
            .flowOn(dispatchers.io)
    }

    override fun observeActiveCustomerBooking(customerId: String): Flow<Resource<Booking?>> {
        return remoteDataSource.observeCustomerBookings(customerId)
            .map { dtoList ->
                val activeBooking = dtoList
                    .map { it.toDomain() }
                    .firstOrNull { it.status.isActive }
                Resource.Success(activeBooking) as Resource<Booking?>
            }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Failed to observe active booking", it)) }
            .flowOn(dispatchers.io)
    }

    override fun observePendingBookings(): Flow<Resource<List<Booking>>> {
        return remoteDataSource.observePendingBookings()
            .map { dtoList ->
                val domainList = dtoList.map { it.toDomain() }
                Resource.Success(domainList) as Resource<List<Booking>>
            }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Failed to observe pending bookings", it)) }
            .flowOn(dispatchers.io)
    }

    override suspend fun acceptBooking(bookingId: String, driverId: String): Resource<Unit> =
        withContext(dispatchers.io) {
            remoteDataSource.acceptBooking(bookingId, driverId)
        }

    override fun observeActiveDriverBooking(driverId: String): Flow<Resource<Booking?>> {
        return remoteDataSource.observeDriverBookings(driverId)
            .map { dtoList ->
                val activeBooking = dtoList
                    .map { it.toDomain() }
                    .firstOrNull { it.status.isActive }
                Resource.Success(activeBooking) as Resource<Booking?>
            }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Failed to observe active driver booking", it)) }
            .flowOn(dispatchers.io)
    }

    override fun observeDriverBookings(driverId: String): Flow<Resource<List<Booking>>> {
        return remoteDataSource.observeDriverBookings(driverId)
            .map { dtoList ->
                val domainList = dtoList.map { it.toDomain() }
                Resource.Success(domainList) as Resource<List<Booking>>
            }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Failed to observe driver bookings", it)) }
            .flowOn(dispatchers.io)
    }

    override suspend fun updateBookingStatus(
        bookingId: String,
        driverId: String,
        newStatus: BookingStatus
    ): Resource<Unit> = withContext(dispatchers.io) {
        remoteDataSource.updateBookingStatus(bookingId, driverId, newStatus.name)
    }

    override suspend fun completeBooking(
        bookingId: String,
        driverId: String,
        finalFare: Double?,
        finalDistanceMeters: Int?,
        finalDurationSeconds: Long?
    ): Resource<Unit> = withContext(dispatchers.io) {
        remoteDataSource.completeBooking(
            bookingId = bookingId,
            driverId = driverId,
            finalFare = finalFare,
            finalDistanceMeters = finalDistanceMeters,
            finalDurationSeconds = finalDurationSeconds
        )
    }

    override suspend fun cancelBooking(
        bookingId: String,
        reason: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        remoteDataSource.cancelBooking(bookingId, reason)
    }
}
