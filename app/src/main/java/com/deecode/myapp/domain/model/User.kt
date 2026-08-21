package com.deecode.myapp.domain.model

import com.deecode.myapp.core.model.UserRole

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
