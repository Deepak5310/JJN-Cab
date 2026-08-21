package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun createBooking(booking: Booking): Resource<String>
    suspend fun getBooking(bookingId: String): Resource<Booking>
    fun observeBooking(bookingId: String): Flow<Resource<Booking>>
    fun observeCustomerBookings(customerId: String): Flow<Resource<List<Booking>>>
    fun observeActiveCustomerBooking(customerId: String): Flow<Resource<Booking?>>
    fun observePendingBookings(): Flow<Resource<List<Booking>>>
    suspend fun acceptBooking(bookingId: String, driverId: String): Resource<Unit>
    fun observeActiveDriverBooking(driverId: String): Flow<Resource<Booking?>>
    suspend fun updateBookingStatus(bookingId: String, driverId: String, newStatus: BookingStatus): Resource<Unit>
    suspend fun completeBooking(
        bookingId: String,
        driverId: String,
        finalFare: Double? = null,
        finalDistanceMeters: Int? = null,
        finalDurationSeconds: Long? = null
    ): Resource<Unit>
}
