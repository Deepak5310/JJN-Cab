package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun observeActiveBookings(): Flow<Resource<List<String>>>
}
