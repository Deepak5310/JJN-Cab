package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.RatingDto
import com.deecode.myapp.domain.model.Rating
import com.deecode.myapp.domain.repository.RatingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val dispatchers: DispatcherProvider
) : RatingRepository {

    private val ratingsCollection = firestore.collection("ratings")
    private val bookingsCollection = firestore.collection("bookings")
    private val usersCollection = firestore.collection("users")

    override suspend fun submitRating(
        bookingId: String,
        fromUserId: String,
        toUserId: String,
        role: String,
        rating: Int,
        review: String?
    ): Resource<Unit> = withContext(dispatchers.io) {
        val currentUid = auth.currentUser?.uid
            ?: return@withContext Resource.Error("User must be authenticated to submit a rating.")

        if (currentUid != fromUserId) {
            return@withContext Resource.Error("Unauthorized rating submission.")
        }

        if (rating !in 1..5) {
            return@withContext Resource.Error("Rating must be between 1 and 5 stars.")
        }

        val ratingDocId = "${bookingId}_$fromUserId"
        val ratingDocRef = ratingsCollection.document(ratingDocId)
        val bookingDocRef = bookingsCollection.document(bookingId)
        val targetUserDocRef = usersCollection.document(toUserId)

        try {
            firestore.runTransaction { transaction ->
                // 1. Check duplicate rating
                val existingRating = transaction.get(ratingDocRef)
                if (existingRating.exists()) {
                    throw IllegalStateException("DUPLICATE_RATING")
                }

                // 2. Verify booking is completed
                val bookingSnapshot = transaction.get(bookingDocRef)
                if (!bookingSnapshot.exists()) {
                    throw IllegalArgumentException("Booking not found.")
                }

                val status = bookingSnapshot.getString("status")
                if (status != "COMPLETED") {
                    throw IllegalStateException("CANNOT_RATE_INCOMPLETE_RIDE")
                }

                val customerId = bookingSnapshot.getString("customerId")
                val driverId = bookingSnapshot.getString("driverId")
                if (fromUserId != customerId && fromUserId != driverId) {
                    throw SecurityException("User is not a participant of this booking.")
                }

                // 3. Read target user's current aggregate ratings
                val targetUserSnapshot = transaction.get(targetUserDocRef)
                val currentCount = targetUserSnapshot.getLong("ratingCount") ?: 0L
                val currentSum = targetUserSnapshot.getDouble("totalRatingSum") ?: (currentCount.toDouble() * 5.0)

                val newCount = currentCount + 1
                val newSum = currentSum + rating
                val newAverage = if (newCount > 0) newSum / newCount else 5.0

                // 4. Create Rating document
                val ratingData = mapOf(
                    "ratingId" to ratingDocId,
                    "bookingId" to bookingId,
                    "fromUserId" to fromUserId,
                    "toUserId" to toUserId,
                    "role" to role,
                    "rating" to rating,
                    "review" to review?.trim()?.ifBlank { null },
                    "createdAt" to FieldValue.serverTimestamp()
                )
                transaction.set(ratingDocRef, ratingData)

                // 5. Update target user aggregate metrics
                transaction.update(
                    targetUserDocRef,
                    mapOf(
                        "ratingCount" to newCount,
                        "totalRatingSum" to newSum,
                        "ratingAverage" to newAverage,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                // 6. Update booking doc with rating indicator
                val bookingUpdateMap = if (fromUserId == customerId) {
                    mapOf(
                        "customerRating" to rating,
                        "customerReview" to review?.trim()?.ifBlank { null },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                } else {
                    mapOf(
                        "driverRating" to rating,
                        "driverReview" to review?.trim()?.ifBlank { null },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
                transaction.update(bookingDocRef, bookingUpdateMap)
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("DUPLICATE_RATING") == true || e.cause?.message?.contains("DUPLICATE_RATING") == true ->
                    "You have already submitted a rating for this ride."
                e.message?.contains("CANNOT_RATE_INCOMPLETE_RIDE") == true || e.cause?.message?.contains("CANNOT_RATE_INCOMPLETE_RIDE") == true ->
                    "Ratings can only be submitted for completed rides."
                else -> e.localizedMessage ?: "Failed to submit rating."
            }
            Resource.Error(message, e)
        }
    }

    override suspend fun getRating(bookingId: String, fromUserId: String): Resource<Rating?> = withContext(dispatchers.io) {
        try {
            val ratingDocId = "${bookingId}_$fromUserId"
            val snapshot = ratingsCollection.document(ratingDocId).get().await()
            if (snapshot.exists()) {
                val dto = snapshot.toObject(RatingDto::class.java)
                Resource.Success(dto?.toDomain())
            } else {
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get rating", e)
        }
    }

    override fun observeUserRatings(userId: String): Flow<Resource<List<Rating>>> = callbackFlow {
        val listener = ratingsCollection
            .whereEqualTo("toUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dtos = snapshot?.toObjects(RatingDto::class.java) ?: emptyList()
                val domainList = dtos.map { it.toDomain() }
                val sorted = domainList.sortedByDescending { it.createdAt }
                trySend(Resource.Success(sorted) as Resource<List<Rating>>)
            }

        awaitClose { listener.remove() }
    }.catch {
        emit(Resource.Error(it.localizedMessage ?: "Failed to observe ratings", it))
    }.flowOn(dispatchers.io)
}
