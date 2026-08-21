package com.deecode.myapp.data.datasource.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DefaultLocationDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : LocationDataSource {

    override fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    override suspend fun getCurrentLocation(): Resource<LocationPoint> {
        if (!hasLocationPermission()) {
            return Resource.Error("Location permission is required to fetch your current position.")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        if (!isGpsEnabled && !isNetworkEnabled) {
            return Resource.Error("Location services (GPS) are turned off. Please enable GPS in device settings.")
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await() ?: fusedLocationProviderClient.lastLocation.await()

            if (location != null) {
                Resource.Success(
                    LocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
            } else {
                Resource.Error("Could not retrieve current location. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get current location", e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeLocationUpdates(
        intervalMs: Long,
        minDistanceMeters: Float
    ): Flow<LocationPoint> = callbackFlow {
        if (!hasLocationPermission()) {
            close(IllegalStateException("Location permission not granted."))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(minDistanceMeters)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val lastLocation = result.lastLocation ?: return
                trySend(
                    LocationPoint(
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude
                    )
                )
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
}
