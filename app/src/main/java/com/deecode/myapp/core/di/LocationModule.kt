package com.deecode.myapp.core.di

import android.content.Context
import com.deecode.myapp.data.datasource.location.DefaultLocationDataSource
import com.deecode.myapp.data.datasource.location.LocationDataSource
import com.deecode.myapp.data.repository.LocationRepositoryImpl
import com.deecode.myapp.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationProvidersModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationBindingsModule {

    @Binds
    @Singleton
    abstract fun bindLocationDataSource(
        defaultLocationDataSource: DefaultLocationDataSource
    ): LocationDataSource

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository
}
