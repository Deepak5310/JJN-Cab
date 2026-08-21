package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRemoteDataSource {
    suspend fun createUserProfile(user: User): Resource<User>
    suspend fun getUserProfile(uid: String): Resource<User>
    fun observeUserProfile(uid: String): Flow<Resource<User>>
    suspend fun updateProfile(uid: String, name: String, phone: String): Resource<Unit>
}
