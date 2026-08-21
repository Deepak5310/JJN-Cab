package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUserProfile(user: User): Resource<User>
    suspend fun getUserProfile(uid: String): Resource<User>
    fun observeUserProfile(uid: String): Flow<Resource<User>>
    suspend fun updateProfile(uid: String, name: String, phone: String): Resource<Unit>
}
