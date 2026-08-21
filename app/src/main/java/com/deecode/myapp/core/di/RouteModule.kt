package com.deecode.myapp.core.di

import com.deecode.myapp.data.datasource.route.DefaultRouteDataSource
import com.deecode.myapp.data.datasource.route.RouteDataSource
import com.deecode.myapp.data.repository.RouteRepositoryImpl
import com.deecode.myapp.domain.repository.RouteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RouteProvidersModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RouteBindingsModule {

    @Binds
    @Singleton
    abstract fun bindRouteDataSource(
        defaultRouteDataSource: DefaultRouteDataSource
    ): RouteDataSource

    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        routeRepositoryImpl: RouteRepositoryImpl
    ): RouteRepository
}
