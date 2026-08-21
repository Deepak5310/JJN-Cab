package com.deecode.myapp.core.di

import com.deecode.myapp.data.datasource.remote.AuthRemoteDataSource
import com.deecode.myapp.data.datasource.remote.FirebaseAuthRemoteDataSourceImpl
import com.deecode.myapp.data.datasource.remote.FirestoreUserRemoteDataSourceImpl
import com.deecode.myapp.data.datasource.remote.UserRemoteDataSource
import com.deecode.myapp.data.repository.AuthRepositoryImpl
import com.deecode.myapp.data.repository.BookingRepositoryImpl
import com.deecode.myapp.data.repository.UserRepositoryImpl
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import com.deecode.myapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        firebaseAuthRemoteDataSourceImpl: FirebaseAuthRemoteDataSourceImpl
    ): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRemoteDataSource(
        firestoreUserRemoteDataSourceImpl: FirestoreUserRemoteDataSourceImpl
    ): UserRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
