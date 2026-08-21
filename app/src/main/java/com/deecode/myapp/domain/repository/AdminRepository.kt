package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.AdminDashboardStats
import com.deecode.myapp.domain.model.Booking
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observeDashboardStats(): Flow<Resource<AdminDashboardStats>>
    fun observeRecentBookings(limit: Int = 10): Flow<Resource<List<Booking>>>
}
