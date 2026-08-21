package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.remote.AuthRemoteDataSource
import com.deecode.myapp.domain.model.AuthUser
import com.deecode.myapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val dispatchers: DispatcherProvider
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = authRemoteDataSource.currentUser

    override fun isUserAuthenticated(): Boolean {
        return authRemoteDataSource.isUserAuthenticated()
    }

    override fun observeAuthState(): Flow<AuthUser?> {
        return authRemoteDataSource.observeAuthState()
            .flowOn(dispatchers.io)
    }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Resource<AuthUser> = withContext(dispatchers.io) {
        authRemoteDataSource.signInWithEmailPassword(email, password)
    }

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Resource<AuthUser> = withContext(dispatchers.io) {
        authRemoteDataSource.signUpWithEmailPassword(email, password)
    }

    override suspend fun signOut(): Resource<Unit> = withContext(dispatchers.io) {
        authRemoteDataSource.signOut()
    }
}
