package com.deecode.myapp.domain.model

data class Rating(
    val ratingId: String,
    val bookingId: String,
    val fromUserId: String,
    val toUserId: String,
    val role: String,
    val rating: Int,
    val review: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
