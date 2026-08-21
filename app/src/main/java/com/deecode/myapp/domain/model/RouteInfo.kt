package com.deecode.myapp.domain.model

import java.util.Locale

data class RouteInfo(
    val distanceMeters: Int,
    val durationSeconds: Long,
    val encodedPolyline: String,
    val points: List<LocationPoint> = emptyList()
) {
    val formattedDistance: String
        get() = if (distanceMeters < 1000) {
            "$distanceMeters m"
        } else {
            String.format(Locale.US, "%.1f km", distanceMeters / 1000.0)
        }

    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val hours = minutes / 60
            val remainingMins = minutes % 60
            return if (hours > 0) {
                "${hours} hr ${remainingMins} min"
            } else {
                "${minutes} min"
            }
        }
}
