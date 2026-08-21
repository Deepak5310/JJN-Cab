package com.deecode.myapp.core.di

import android.content.Context
import android.content.pm.PackageManager
import com.deecode.myapp.data.datasource.places.DefaultPlacesDataSource
import com.deecode.myapp.data.datasource.places.PlacesDataSource
import com.deecode.myapp.data.repository.PlacesRepositoryImpl
import com.deecode.myapp.domain.repository.PlacesRepository
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesProvidersModule {

    @Provides
    @Singleton
    fun providePlacesClient(
        @ApplicationContext context: Context
    ): PlacesClient {
        if (!Places.isInitialized()) {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
            Places.initializeWithNewPlacesApiEnabled(context, apiKey)
        }
        return Places.createClient(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlacesBindingsModule {

    @Binds
    @Singleton
    abstract fun bindPlacesDataSource(
        defaultPlacesDataSource: DefaultPlacesDataSource
    ): PlacesDataSource

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(
        placesRepositoryImpl: PlacesRepositoryImpl
    ): PlacesRepository
}
