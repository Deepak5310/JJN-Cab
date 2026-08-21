package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Booking
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun createBooking(booking: Booking): Resource<String>
    suspend fun getBooking(bookingId: String): Resource<Booking>
    fun observeBooking(bookingId: String): Flow<Resource<Booking>>
    fun observeCustomerBookings(customerId: String): Flow<Resource<List<Booking>>>
    fun observeActiveCustomerBooking(customerId: String): Flow<Resource<Booking?>>
}
