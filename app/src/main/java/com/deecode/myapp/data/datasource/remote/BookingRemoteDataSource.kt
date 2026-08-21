package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.BookingDto
import kotlinx.coroutines.flow.Flow

interface BookingRemoteDataSource {
    suspend fun createBooking(bookingDto: BookingDto): Resource<String>
    suspend fun getBooking(bookingId: String): Resource<BookingDto>
    fun observeBooking(bookingId: String): Flow<BookingDto?>
    fun observeCustomerBookings(customerId: String): Flow<List<BookingDto>>
}
