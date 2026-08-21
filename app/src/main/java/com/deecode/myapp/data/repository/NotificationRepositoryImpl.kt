package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.repository.NotificationRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: DispatcherProvider
) : NotificationRepository {

    override suspend fun getFcmToken(): Resource<String> = withContext(dispatchers.io) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNullOrBlank()) {
                Resource.Error("FCM token is empty.")
            } else {
                Resource.Success(token)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to retrieve FCM token", e)
        }
    }

    override suspend fun syncFcmToken(userId: String): Resource<Unit> = withContext(dispatchers.io) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (!token.isNullOrBlank()) {
                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "fcmToken" to token,
                            "fcmUpdatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to sync FCM token", e)
        }
    }

    override suspend fun clearFcmToken(userId: String): Resource<Unit> = withContext(dispatchers.io) {
        try {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "fcmToken" to FieldValue.delete(),
                        "fcmUpdatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to clear FCM token", e)
        }
    }
}
