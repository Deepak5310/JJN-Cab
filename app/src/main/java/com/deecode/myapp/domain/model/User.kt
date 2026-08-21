package com.deecode.myapp.domain.model

import com.deecode.myapp.core.model.UserRole

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val ratingAverage: Double = 5.0,
    val ratingCount: Int = 0,
    val totalRatingSum: Double = 0.0,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
