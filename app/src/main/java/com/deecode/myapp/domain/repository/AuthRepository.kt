package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: AuthUser?
    fun isUserAuthenticated(): Boolean
    fun observeAuthState(): Flow<AuthUser?>
    suspend fun signInWithEmailPassword(email: String, password: String): Resource<AuthUser>
    suspend fun signUpWithEmailPassword(email: String, password: String): Resource<AuthUser>
    suspend fun signOut(): Resource<Unit>
}
