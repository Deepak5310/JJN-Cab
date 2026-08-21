package com.deecode.myapp.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deecode.myapp.domain.model.LocationPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
    pickupLocation: LocationPoint? = null,
    destinationLocation: LocationPoint? = null,
    hasLocationPermission: Boolean = false,
    isSelectingOnMap: Boolean = false,
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            pickupLocation?.let { LatLng(it.latitude, it.longitude) }
                ?: currentLocation?.let { LatLng(it.latitude, it.longitude) }
                ?: DefaultLocation,
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

    // Auto animate camera when pickup / destination locations change
    LaunchedEffect(pickupLocation, destinationLocation, currentLocation) {
        if (isSelectingOnMap) return@LaunchedEffect

        if (pickupLocation != null && destinationLocation != null) {
            val bounds = LatLngBounds.builder()
                .include(LatLng(pickupLocation.latitude, pickupLocation.longitude))
                .include(LatLng(destinationLocation.latitude, destinationLocation.longitude))
                .build()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 100),
                durationMs = 800
            )
        } else if (pickupLocation != null) {
            val target = LatLng(pickupLocation.latitude, pickupLocation.longitude)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, zoom),
                durationMs = 800
            )
        } else if (destinationLocation != null) {
            val target = LatLng(destinationLocation.latitude, destinationLocation.longitude)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, zoom),
                durationMs = 800
            )
        } else if (currentLocation != null) {
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
            // Pickup Marker
            if (pickupLocation != null) {
                Marker(
                    state = MarkerState(
                        position = LatLng(pickupLocation.latitude, pickupLocation.longitude)
                    ),
                    title = "Pickup Location",
                    snippet = pickupLocation.address ?: "Selected Pickup",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Destination Marker
            if (destinationLocation != null) {
                Marker(
                    state = MarkerState(
                        position = LatLng(destinationLocation.latitude, destinationLocation.longitude)
                    ),
                    title = "Destination",
                    snippet = destinationLocation.address ?: "Selected Destination",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            content()
        }

        // Center Pin when manually selecting location on Map
        if (isSelectingOnMap) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .shadow(6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📍 Drag Map to Set Location",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}
