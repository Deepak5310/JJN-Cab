package com.deecode.myapp.core.di

import com.deecode.myapp.domain.calculator.DefaultFareCalculator
import com.deecode.myapp.domain.calculator.FareCalculator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FareModule {

    @Binds
    @Singleton
    abstract fun bindFareCalculator(
        defaultFareCalculator: DefaultFareCalculator
    ): FareCalculator
}
