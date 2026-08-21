package com.deecode.myapp.domain.usecase

import com.deecode.myapp.core.result.Resource
import kotlinx.coroutines.flow.Flow

fun interface FlowUseCase<in P, out R> {
    operator fun invoke(parameters: P): Flow<Resource<R>>
}

fun interface SuspendUseCase<in P, out R> {
    suspend operator fun invoke(parameters: P): Resource<R>
}
