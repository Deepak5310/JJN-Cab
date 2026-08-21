package com.deecode.myapp.data.datasource.remote

import kotlinx.coroutines.flow.Flow

interface BookingRemoteDataSource {
    fun getActiveBookingsStream(): Flow<List<String>>
}
