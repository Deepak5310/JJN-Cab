package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.remote.BookingRemoteDataSource
import com.deecode.myapp.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val remoteDataSource: BookingRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : BookingRepository {

    override fun observeActiveBookings(): Flow<Resource<List<String>>> {
        return remoteDataSource.getActiveBookingsStream()
            .map<List<String>, Resource<List<String>>> { Resource.Success(it) }
            .catch { emit(Resource.Error(it.localizedMessage ?: "An unexpected error occurred", it)) }
            .flowOn(dispatchers.io)
    }
}
