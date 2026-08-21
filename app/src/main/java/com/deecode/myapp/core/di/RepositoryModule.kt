package com.deecode.myapp.core.di

import com.deecode.myapp.data.datasource.remote.AuthRemoteDataSource
import com.deecode.myapp.data.datasource.remote.BookingRemoteDataSource
import com.deecode.myapp.data.datasource.remote.DefaultBookingRemoteDataSource
import com.deecode.myapp.data.datasource.remote.DefaultDriverRemoteDataSource
import com.deecode.myapp.data.datasource.remote.DefaultDriverTrackingRemoteDataSource
import com.deecode.myapp.data.datasource.remote.DriverRemoteDataSource
import com.deecode.myapp.data.datasource.remote.DriverTrackingRemoteDataSource
import com.deecode.myapp.data.datasource.remote.FirebaseAuthRemoteDataSourceImpl
import com.deecode.myapp.data.datasource.remote.FirestoreUserRemoteDataSourceImpl
import com.deecode.myapp.data.datasource.remote.UserRemoteDataSource
import com.deecode.myapp.data.repository.AuthRepositoryImpl
import com.deecode.myapp.data.repository.BookingRepositoryImpl
import com.deecode.myapp.data.repository.DriverRepositoryImpl
import com.deecode.myapp.data.repository.DriverTrackingRepositoryImpl
import com.deecode.myapp.data.repository.NotificationRepositoryImpl
import com.deecode.myapp.data.repository.UserRepositoryImpl
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import com.deecode.myapp.domain.repository.DriverRepository
import com.deecode.myapp.domain.repository.DriverTrackingRepository
import com.deecode.myapp.domain.repository.NotificationRepository
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
    abstract fun bindBookingRemoteDataSource(
        defaultBookingRemoteDataSource: DefaultBookingRemoteDataSource
    ): BookingRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindDriverRemoteDataSource(
        defaultDriverRemoteDataSource: DefaultDriverRemoteDataSource
    ): DriverRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindDriverRepository(
        driverRepositoryImpl: DriverRepositoryImpl
    ): DriverRepository

    @Binds
    @Singleton
    abstract fun bindDriverTrackingRemoteDataSource(
        defaultDriverTrackingRemoteDataSource: DefaultDriverTrackingRemoteDataSource
    ): DriverTrackingRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindDriverTrackingRepository(
        driverTrackingRepositoryImpl: DriverTrackingRepositoryImpl
    ): DriverTrackingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

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
