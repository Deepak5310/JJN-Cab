package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.BookingDto
import com.deecode.myapp.domain.model.BookingStateMachine
import com.deecode.myapp.domain.model.BookingStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DefaultBookingRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BookingRemoteDataSource {

    private val bookingsCollection = firestore.collection("bookings")

    override suspend fun createBooking(bookingDto: BookingDto): Resource<String> {
        val currentUid = auth.currentUser?.uid
            ?: return Resource.Error("User must be authenticated to create a booking.")

        if (currentUid != bookingDto.customerId) {
            return Resource.Error("Unauthorized booking creation. Customer ID does not match authenticated user.")
        }

        return try {
            val docRef = if (bookingDto.bookingId.isNotBlank()) {
                bookingsCollection.document(bookingDto.bookingId)
            } else {
                bookingsCollection.document()
            }

            val finalDto = bookingDto.copy(
                bookingId = docRef.id,
                status = BookingStatus.REQUESTED.name
            )
            docRef.set(finalDto).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create booking", e)
        }
    }

    override suspend fun getBooking(bookingId: String): Resource<BookingDto> {
        return try {
            val doc = bookingsCollection.document(bookingId).get().await()
            val dto = doc.toObject(BookingDto::class.java)
            if (dto != null) {
                Resource.Success(dto)
            } else {
                Resource.Error("Booking not found with ID: $bookingId")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch booking", e)
        }
    }

    override fun observeBooking(bookingId: String): Flow<BookingDto?> = callbackFlow {
        val listener = bookingsCollection.document(bookingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dto = snapshot?.toObject(BookingDto::class.java)
                trySend(dto)
            }

        awaitClose { listener.remove() }
    }

    override fun observeCustomerBookings(customerId: String): Flow<List<BookingDto>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("customerId", customerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dtos = snapshot?.toObjects(BookingDto::class.java) ?: emptyList()
                val sorted = dtos.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                trySend(sorted)
            }

        awaitClose { listener.remove() }
    }

    override fun observePendingBookings(): Flow<List<BookingDto>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("status", BookingStatus.REQUESTED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dtos = snapshot?.toObjects(BookingDto::class.java) ?: emptyList()
                val eligible = dtos.filter { it.driverId == null }
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                trySend(eligible)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun acceptBooking(bookingId: String, driverId: String): Resource<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Resource.Error("Driver must be authenticated.")

        if (currentUid != driverId) {
            return Resource.Error("Unauthorized driver assignment.")
        }

        return try {
            val docRef = bookingsCollection.document(bookingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Booking not found.")
                }

                val currentDriverId = snapshot.getString("driverId")
                val currentStatus = BookingStateMachine.canonicalize(snapshot.getString("status"))

                if (!BookingStateMachine.canDriverAccept(currentStatus, currentDriverId)) {
                    throw IllegalStateException("ALREADY_TAKEN")
                }

                transaction.update(
                    docRef,
                    mapOf(
                        "driverId" to driverId,
                        "status" to BookingStatus.ACCEPTED.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val isAlreadyTaken = e.message?.contains("ALREADY_TAKEN") == true ||
                    e.cause?.message?.contains("ALREADY_TAKEN") == true
            val message = if (isAlreadyTaken) {
                "This ride was already accepted by another driver."
            } else {
                e.localizedMessage ?: "Failed to accept booking"
            }
            Resource.Error(message, e)
        }
    }

    override fun observeDriverBookings(driverId: String): Flow<List<BookingDto>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("driverId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dtos = snapshot?.toObjects(BookingDto::class.java) ?: emptyList()
                val sorted = dtos.sortedByDescending { it.updatedAt?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L }
                trySend(sorted)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateBookingStatus(
        bookingId: String,
        driverId: String,
        newStatus: String
    ): Resource<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Resource.Error("Driver must be authenticated.")

        if (currentUid != driverId) {
            return Resource.Error("Unauthorized driver status update.")
        }

        return try {
            val docRef = bookingsCollection.document(bookingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Booking not found.")
                }

                val currentDriver = snapshot.getString("driverId")
                val currentStatus = BookingStateMachine.canonicalize(snapshot.getString("status"))
                val targetStatus = BookingStateMachine.canonicalize(newStatus)

                if (currentDriver != driverId) {
                    throw IllegalStateException("You are not the assigned driver for this ride.")
                }

                if (!BookingStateMachine.canDriverTransition(currentStatus, targetStatus)) {
                    throw IllegalStateException("Invalid status transition from ${currentStatus.name} to ${targetStatus.name}.")
                }

                transaction.update(
                    docRef,
                    mapOf(
                        "status" to targetStatus.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update ride status", e)
        }
    }

    override suspend fun completeBooking(
        bookingId: String,
        driverId: String,
        finalFare: Double?,
        finalDistanceMeters: Int?,
        finalDurationSeconds: Long?
    ): Resource<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Resource.Error("Driver must be authenticated.")

        if (currentUid != driverId) {
            return Resource.Error("Unauthorized driver operation.")
        }

        return try {
            val docRef = bookingsCollection.document(bookingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Booking not found.")
                }

                val currentDriver = snapshot.getString("driverId")
                val currentStatus = BookingStateMachine.canonicalize(snapshot.getString("status"))

                if (currentDriver != driverId) {
                    throw IllegalStateException("You are not the assigned driver for this ride.")
                }

                if (!BookingStateMachine.canComplete(currentStatus)) {
                    throw IllegalStateException("Ride can only be completed when IN_PROGRESS. Current status: ${currentStatus.name}")
                }

                val estimatedFare = snapshot.getDouble("estimatedFare") ?: 0.0
                val estimatedDistance = snapshot.getLong("distanceMeters")?.toInt() ?: 0
                val estimatedDuration = snapshot.getLong("estimatedDurationSeconds") ?: 0L

                val updates = mutableMapOf<String, Any>(
                    "status" to BookingStatus.COMPLETED.name,
                    "completedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "finalFare" to (finalFare ?: estimatedFare),
                    "finalDistanceMeters" to (finalDistanceMeters ?: estimatedDistance),
                    "finalDurationSeconds" to (finalDurationSeconds ?: estimatedDuration)
                )

                transaction.update(docRef, updates)
            }.await()

            try {
                firestore.collection("driver_locations").document(bookingId).delete().await()
            } catch (_: Exception) {
                // Ignore cleanup error
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to complete ride", e)
        }
    }

    override suspend fun cancelBooking(
        bookingId: String,
        reason: String
    ): Resource<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Resource.Error("User must be authenticated to cancel a booking.")

        return try {
            val docRef = bookingsCollection.document(bookingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Booking not found.")
                }

                val customerId = snapshot.getString("customerId")
                val driverId = snapshot.getString("driverId")
                val currentStatus = BookingStateMachine.canonicalize(snapshot.getString("status"))

                if (currentUid != customerId && currentUid != driverId) {
                    throw IllegalStateException("Unauthorized: Only the assigned customer or driver can cancel this booking.")
                }

                val role = if (currentUid == customerId) UserRole.CUSTOMER else UserRole.DRIVER
                if (!BookingStateMachine.canCancel(currentStatus, role)) {
                    if (BookingStateMachine.isTerminal(currentStatus)) {
                        throw IllegalStateException("This booking is already closed or completed.")
                    } else {
                        throw IllegalStateException("Cannot cancel booking at current status: ${currentStatus.name}.")
                    }
                }

                val newStatus = if (currentUid == customerId) BookingStatus.CANCELLED_BY_CUSTOMER else BookingStatus.CANCELLED_BY_DRIVER

                val updates = mapOf(
                    "status" to newStatus.name,
                    "cancelledBy" to currentUid,
                    "cancellationReason" to reason,
                    "cancelledAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                transaction.update(docRef, updates)
            }.await()

            try {
                firestore.collection("driver_locations").document(bookingId).delete().await()
            } catch (_: Exception) {
                // Ignore cleanup error
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to cancel booking", e)
        }
    }
}
