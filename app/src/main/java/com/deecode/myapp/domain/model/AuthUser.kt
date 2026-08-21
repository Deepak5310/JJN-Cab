package com.deecode.myapp.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val isEmailVerified: Boolean = false
)
