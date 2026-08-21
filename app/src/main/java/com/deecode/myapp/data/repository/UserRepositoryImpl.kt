package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.remote.UserRemoteDataSource
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : UserRepository {

    override suspend fun createUserProfile(user: User): Resource<User> =
        withContext(dispatchers.io) {
            userRemoteDataSource.createUserProfile(user)
        }

    override suspend fun getUserProfile(uid: String): Resource<User> =
        withContext(dispatchers.io) {
            userRemoteDataSource.getUserProfile(uid)
        }

    override fun observeUserProfile(uid: String): Flow<Resource<User>> {
        return userRemoteDataSource.observeUserProfile(uid)
            .flowOn(dispatchers.io)
    }

    override suspend fun updateProfile(
        uid: String,
        name: String,
        phone: String
    ): Resource<Unit> = withContext(dispatchers.io) {
        userRemoteDataSource.updateProfile(uid, name, phone)
    }
}
