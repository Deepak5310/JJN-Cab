package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.Rating
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName

data class RatingDto(
    @get:PropertyName("ratingId") @set:PropertyName("ratingId") var ratingId: String = "",
    @get:PropertyName("bookingId") @set:PropertyName("bookingId") var bookingId: String = "",
    @get:PropertyName("fromUserId") @set:PropertyName("fromUserId") var fromUserId: String = "",
    @get:PropertyName("toUserId") @set:PropertyName("toUserId") var toUserId: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "",
    @get:PropertyName("rating") @set:PropertyName("rating") var rating: Int = 5,
    @get:PropertyName("review") @set:PropertyName("review") var review: String? = null,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null
) {
    fun toDomain(): Rating {
        return Rating(
            ratingId = ratingId,
            bookingId = bookingId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            role = role,
            rating = rating,
            review = review,
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun toMap(rating: Rating): Map<String, Any?> {
            return mapOf(
                "ratingId" to rating.ratingId,
                "bookingId" to rating.bookingId,
                "fromUserId" to rating.fromUserId,
                "toUserId" to rating.toUserId,
                "role" to rating.role,
                "rating" to rating.rating,
                "review" to rating.review,
                "createdAt" to FieldValue.serverTimestamp()
            )
        }
    }
}
