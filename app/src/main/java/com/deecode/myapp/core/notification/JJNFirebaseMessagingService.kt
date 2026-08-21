package com.deecode.myapp.core.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class JJNFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: return

        serviceScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUid)
                    .update(
                        mapOf(
                            "fcmToken" to token,
                            "fcmUpdatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                // Non-fatal token sync failure
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val type = data["type"] ?: NotificationHelper.TYPE_RIDE_STARTED
        val bookingId = data["bookingId"]
        val title = data["title"]
            ?: remoteMessage.notification?.title
            ?: getDefaultTitleForType(type)
        val body = data["body"]
            ?: remoteMessage.notification?.body
            ?: "You have a new update on your ride."

        NotificationHelper.showNotification(
            context = applicationContext,
            type = type,
            bookingId = bookingId,
            title = title,
            body = body
        )
    }

    private fun getDefaultTitleForType(type: String): String = when (type) {
        NotificationHelper.TYPE_NEW_REQUEST -> "New Ride Request 🚕"
        NotificationHelper.TYPE_BOOKING_ACCEPTED -> "Ride Accepted! 🚘"
        NotificationHelper.TYPE_DRIVER_ARRIVING -> "Driver Arrived 📍"
        NotificationHelper.TYPE_RIDE_STARTED -> "Trip in Progress 🏎️"
        NotificationHelper.TYPE_RIDE_COMPLETED -> "Ride Completed 🏁"
        NotificationHelper.TYPE_BOOKING_CANCELLED -> "Ride Cancelled ❌"
        else -> "JJN Cab Update"
    }
}
