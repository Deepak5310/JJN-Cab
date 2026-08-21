package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.BookingDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

            val finalDto = bookingDto.copy(bookingId = docRef.id)
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
}
