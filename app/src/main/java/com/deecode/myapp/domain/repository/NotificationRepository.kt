package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource

interface NotificationRepository {
    suspend fun getFcmToken(): Resource<String>
    suspend fun syncFcmToken(userId: String): Resource<Unit>
    suspend fun clearFcmToken(userId: String): Resource<Unit>
}
