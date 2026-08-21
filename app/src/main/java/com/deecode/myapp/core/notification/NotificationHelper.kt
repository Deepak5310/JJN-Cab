package com.deecode.myapp.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.deecode.myapp.MainActivity
import com.deecode.myapp.R

object NotificationHelper {

    const val CHANNEL_RIDES = "jjn_rides_channel"
    const val CHANNEL_REQUESTS = "jjn_requests_channel"

    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
    const val EXTRA_BOOKING_ID = "extra_booking_id"

    const val TYPE_NEW_REQUEST = "NEW_REQUEST"
    const val TYPE_BOOKING_ACCEPTED = "BOOKING_ACCEPTED"
    const val TYPE_DRIVER_ARRIVING = "DRIVER_ARRIVING"
    const val TYPE_RIDE_STARTED = "RIDE_STARTED"
    const val TYPE_RIDE_COMPLETED = "RIDE_COMPLETED"
    const val TYPE_BOOKING_CANCELLED = "BOOKING_CANCELLED"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ridesChannel = NotificationChannel(
                CHANNEL_RIDES,
                "Ride Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time updates about your ride status, driver arrival, and completions."
                enableVibration(true)
                setShowBadge(true)
            }

            val requestsChannel = NotificationChannel(
                CHANNEL_REQUESTS,
                "Ride Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming ride requests for drivers."
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(ridesChannel, requestsChannel))
        }
    }

    fun showNotification(
        context: Context,
        type: String,
        bookingId: String?,
        title: String,
        body: String
    ) {
        val channelId = if (type == TYPE_NEW_REQUEST) CHANNEL_REQUESTS else CHANNEL_RIDES

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            if (!bookingId.isNullOrBlank()) {
                putExtra(EXTRA_BOOKING_ID, bookingId)
            }
        }

        val requestCode = (bookingId.hashCode() + type.hashCode()).let { if (it < 0) -it else it }
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = requestCode % 100000 + 1000
            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS permission not granted
        }
    }
}
