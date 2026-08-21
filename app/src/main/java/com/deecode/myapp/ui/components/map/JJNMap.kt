package com.deecode.myapp.ui.components.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.deecode.myapp.domain.model.LocationPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val DefaultLocation = LatLng(28.6139, 77.2090) // Default fallback coordinates

@Composable
fun JJNMap(
    modifier: Modifier = Modifier,
    currentLocation: LocationPoint? = null,
    hasLocationPermission: Boolean = false,
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: DefaultLocation,
            15f
        )
    },
    zoom: Float = 16f,
    content: @Composable () -> Unit = {}
) {
    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = true
        )
    }

    LaunchedEffect(currentLocation) {
        if (currentLocation != null) {
            val target = LatLng(currentLocation.latitude, currentLocation.longitude)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, zoom),
                durationMs = 800
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            if (currentLocation != null) {
                Marker(
                    state = MarkerState(
                        position = LatLng(currentLocation.latitude, currentLocation.longitude)
                    ),
                    title = "Your Location"
                )
            }
            content()
        }
    }
}
