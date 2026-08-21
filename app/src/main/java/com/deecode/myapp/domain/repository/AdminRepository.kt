package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.AdminDashboardStats
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.DriverManagementItem
import com.deecode.myapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observeDashboardStats(): Flow<Resource<AdminDashboardStats>>
    fun observeRecentBookings(limit: Int = 10): Flow<Resource<List<Booking>>>
    fun observeUsers(): Flow<Resource<List<User>>>
    suspend fun setUserActiveStatus(targetUid: String, isActive: Boolean, adminUid: String): Resource<Unit>
    suspend fun setDriverVerification(targetUid: String, isVerified: Boolean, adminUid: String): Resource<Unit>
    suspend fun setUserRole(targetUid: String, newRole: UserRole, adminUid: String): Resource<Unit>

    fun observeDriverManagementList(): Flow<Resource<List<DriverManagementItem>>>
    suspend fun setDriverActiveStatus(driverId: String, isActive: Boolean, adminUid: String): Resource<Unit>
    suspend fun setDriverApprovalStatus(driverId: String, isApproved: Boolean, adminUid: String): Resource<Unit>

    fun observeAllBookings(): Flow<Resource<List<Booking>>>
    suspend fun cancelBookingAsAdmin(bookingId: String, reason: String, adminUid: String): Resource<Unit>
}
