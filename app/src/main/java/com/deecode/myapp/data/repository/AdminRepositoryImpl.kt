package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.BookingDto
import com.deecode.myapp.data.model.DriverDto
import com.deecode.myapp.data.model.UserDto
import com.deecode.myapp.domain.model.AdminDashboardStats
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.domain.repository.AdminRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: DispatcherProvider
) : AdminRepository {

    private val usersCollection = firestore.collection("users")
    private val driversCollection = firestore.collection("drivers")
    private val bookingsCollection = firestore.collection("bookings")

    private fun observeRawUsers(): Flow<List<UserDto>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.toObjects(UserDto::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    private fun observeRawDrivers(): Flow<List<DriverDto>> = callbackFlow {
        val listener = driversCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.toObjects(DriverDto::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    private fun observeRawBookings(): Flow<List<BookingDto>> = callbackFlow {
        val listener = bookingsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.toObjects(BookingDto::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun observeDashboardStats(): Flow<Resource<AdminDashboardStats>> {
        return combine(
            observeRawUsers(),
            observeRawDrivers(),
            observeRawBookings()
        ) { users, drivers, bookings ->
            val customersCount = users.count { it.role.equals("CUSTOMER", ignoreCase = true) }
            val driversCount = users.count { it.role.equals("DRIVER", ignoreCase = true) }.let {
                if (it > 0) it else drivers.size
            }
            val onlineDriversCount = drivers.count { it.isOnline }

            var activeCount = 0
            var completedCount = 0
            var cancelledCount = 0
            var totalGmv = 0.0

            for (b in bookings) {
                when (b.status) {
                    "COMPLETED" -> {
                        completedCount++
                        totalGmv += (b.finalFare ?: b.estimatedFare)
                    }
                    "CANCELLED", "CANCELLED_BY_CUSTOMER", "CANCELLED_BY_DRIVER" -> {
                        cancelledCount++
                    }
                    "REQUESTED", "SEARCHING_DRIVER", "ACCEPTED", "ASSIGNED", "DRIVER_ARRIVING", "ARRIVING", "IN_PROGRESS", "STARTED" -> {
                        activeCount++
                    }
                }
            }

            val stats = AdminDashboardStats(
                totalCustomers = customersCount,
                totalDrivers = driversCount,
                onlineDrivers = onlineDriversCount,
                activeBookings = activeCount,
                completedRides = completedCount,
                cancelledRides = cancelledCount,
                totalRevenue = totalGmv
            )
            Resource.Success(stats) as Resource<AdminDashboardStats>
        }.catch {
            emit(Resource.Error(it.localizedMessage ?: "Failed to load dashboard metrics", it))
        }.flowOn(dispatchers.io)
    }

    override fun observeRecentBookings(limit: Int): Flow<Resource<List<Booking>>> = callbackFlow {
        val listener = bookingsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val dtos = snapshot?.toObjects(BookingDto::class.java) ?: emptyList()
            val domainList = dtos.map { it.toDomain() }
                .sortedByDescending { it.createdAt }
                .take(limit)
            trySend(Resource.Success(domainList) as Resource<List<Booking>>)
        }
        awaitClose { listener.remove() }
    }.catch {
        emit(Resource.Error(it.localizedMessage ?: "Failed to observe recent bookings", it))
    }.flowOn(dispatchers.io)

    override fun observeUsers(): Flow<Resource<List<User>>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val dtos = snapshot?.toObjects(UserDto::class.java) ?: emptyList()
            val domainUsers = dtos.map { it.toDomain() }
                .sortedByDescending { it.createdAt ?: 0L }
            trySend(Resource.Success(domainUsers) as Resource<List<User>>)
        }
        awaitClose { listener.remove() }
    }.catch {
        emit(Resource.Error(it.localizedMessage ?: "Failed to observe users", it))
    }.flowOn(dispatchers.io)

    override suspend fun setUserActiveStatus(
        targetUid: String,
        isActive: Boolean,
        adminUid: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            val updates = UserDto.updateStatusMap(isActive, adminUid)
            usersCollection.document(targetUid).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update user status", e)
        }
    }

    override suspend fun setDriverVerification(
        targetUid: String,
        isVerified: Boolean,
        adminUid: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            val updates = UserDto.updateDriverVerificationMap(isVerified, adminUid)
            usersCollection.document(targetUid).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update driver verification", e)
        }
    }

    override suspend fun setUserRole(
        targetUid: String,
        newRole: UserRole,
        adminUid: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        try {
            // Strict guard: Never allow granting ADMIN privileges
            if (newRole == UserRole.ADMIN) {
                return@withContext Resource.Error("Unauthorized: Cannot grant Administrator role.")
            }

            val updates = UserDto.updateRoleMap(newRole, adminUid)
            usersCollection.document(targetUid).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update user role", e)
        }
    }
}
