package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Rating
import kotlinx.coroutines.flow.Flow

interface RatingRepository {
    suspend fun submitRating(
        bookingId: String,
        fromUserId: String,
        toUserId: String,
        role: String,
        rating: Int,
        review: String? = null
    ): Resource<Unit>

    suspend fun getRating(bookingId: String, fromUserId: String): Resource<Rating?>

    fun observeUserRatings(userId: String): Flow<Resource<List<Rating>>>
}
