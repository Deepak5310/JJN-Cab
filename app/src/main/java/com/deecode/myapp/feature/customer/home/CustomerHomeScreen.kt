package com.deecode.myapp.feature.customer.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.deecode.myapp.domain.model.FareBreakdown
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.PlaceSuggestion
import com.deecode.myapp.domain.model.RideTier
import com.deecode.myapp.domain.model.RouteInfo
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.feature.customer.LocationTarget
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.components.map.JJNMap
import com.deecode.myapp.ui.theme.spacing
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CustomerHomeScreen(
    user: User?,
    currentLocation: LocationPoint?,
    pickupLocation: LocationPoint?,
    destinationLocation: LocationPoint?,
    routeInfo: RouteInfo?,
    isCalculatingRoute: Boolean,
    routeError: String?,
    selectedRideTier: RideTier,
    fareEstimates: Map<RideTier, FareBreakdown>,
    hasLocationPermission: Boolean,
    isLocating: Boolean,
    locationError: String?,
    isPermissionPermanentlyDenied: Boolean,
    isSearchBottomSheetVisible: Boolean,
    activeLocationTarget: LocationTarget,
    searchQuery: String,
    isSearchingPlaces: Boolean,
    placeSuggestions: List<PlaceSuggestion>,
    isSelectingOnMap: Boolean,
    isReverseGeocoding: Boolean,
    onRequestLocation: () -> Unit,
    onPermissionDenied: (Boolean) -> Unit,
    onClearLocationError: () -> Unit,
    onOpenPlaceSearch: (LocationTarget) -> Unit,
    onClosePlaceSearch: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectPlaceSuggestion: (PlaceSuggestion) -> Unit,
    onClearSelectedLocation: (LocationTarget) -> Unit,
    onStartMapSelection: (LocationTarget) -> Unit,
    onConfirmMapSelection: (Double, Double) -> Unit,
    onCancelMapSelection: () -> Unit,
    onSelectRideTier: (RideTier) -> Unit,
    onNavigateToBookings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val displayName = user?.name?.split(" ")?.firstOrNull() ?: "Rider"
    val cameraPositionState = rememberCameraPositionState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isFineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val isCoarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isFineGranted || isCoarseGranted) {
            onRequestLocation()
        } else {
            val activity = context as? Activity
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } ?: true

            onPermissionDenied(!shouldShowRationale)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium
            )
    ) {
        // Top Greeting Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, $displayName 👋",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = if (isSelectingOnMap) "Move map to choose exact point" else "Where are you going today?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Google Maps Interactive Container
        JJNCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSelectingOnMap) 320.dp else 230.dp),
            contentPadding = 0.dp,
            elevation = 3.dp
        ) {
            JJNMap(
                modifier = Modifier.fillMaxSize(),
                currentLocation = currentLocation,
                pickupLocation = pickupLocation,
                destinationLocation = destinationLocation,
                routePoints = routeInfo?.points ?: emptyList(),
                hasLocationPermission = hasLocationPermission,
                isSelectingOnMap = isSelectingOnMap,
                cameraPositionState = cameraPositionState
            )
        }

        // Map Selection Confirm / Cancel Buttons
        if (isSelectingOnMap) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                JJNOutlinedButton(
                    text = "Cancel",
                    onClick = onCancelMapSelection,
                    modifier = Modifier.weight(1f)
                )
                JJNPrimaryButton(
                    text = if (isReverseGeocoding) "Locating..." else "Confirm Spot",
                    onClick = {
                        val targetLatLng = cameraPositionState.position.target
                        onConfirmMapSelection(targetLatLng.latitude, targetLatLng.longitude)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isReverseGeocoding
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Route Summary Card (Distance & ETA)
        if (isCalculatingRoute) {
            JJNCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                    Text(
                        text = "Calculating fastest driving route & ETA...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        } else if (routeInfo != null) {
            JJNCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛣️", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                        Column {
                            Text(
                                text = "Route Distance",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                            Text(
                                text = routeInfo.formattedDistance,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⏱️", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                        Column {
                            Text(
                                text = "Estimated Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                            Text(
                                text = routeInfo.formattedDuration,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        } else if (!routeError.isNullOrBlank()) {
            JJNCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text(
                    text = routeError,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Location Error / Settings Banner
        if (!locationError.isNullOrBlank()) {
            JJNCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text(
                    text = locationError,
                    style = MaterialTheme.typography.bodySmall
                )

                if (isPermissionPermanentlyDenied) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    JJNOutlinedButton(
                        text = "Open App Settings",
                        onClick = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        // Pickup / Destination Card
        JJNCard(
            elevation = 3.dp,
            contentPadding = MaterialTheme.spacing.medium
        ) {
            // Pickup location row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        if (hasLocationPermission) {
                            onOpenPlaceSearch(LocationTarget.PICKUP)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                    .padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pickup Location",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    if (isLocating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Detecting current location...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    } else if (pickupLocation != null) {
                        Text(
                            text = pickupLocation.address ?: "Lat: ${String.format("%.4f", pickupLocation.latitude)}, Lng: ${String.format("%.4f", pickupLocation.longitude)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "Tap to set pickup point",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                if (pickupLocation != null) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onClearSelectedLocation(LocationTarget.PICKUP) }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Destination row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onOpenPlaceSearch(LocationTarget.DESTINATION) }
                    .padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Destination",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    if (destinationLocation != null) {
                        Text(
                            text = destinationLocation.address ?: "Lat: ${String.format("%.4f", destinationLocation.latitude)}, Lng: ${String.format("%.4f", destinationLocation.longitude)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "Where to? (Search destination)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                if (destinationLocation != null) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onClearSelectedLocation(LocationTarget.DESTINATION) }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Ride Options Header
        Text(
            text = "Ride Options",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Ride Categories Grid with Live Fare Estimation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            RideTier.entries.forEach { tier ->
                RideCategoryCard(
                    tier = tier,
                    fare = fareEstimates[tier],
                    isSelected = selectedRideTier == tier,
                    onClick = { onSelectRideTier(tier) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Fare Breakdown Card (when route is computed)
        val selectedFare = fareEstimates[selectedRideTier]
        if (selectedFare != null) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            JJNOutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fare Breakdown (${selectedRideTier.displayName})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = selectedFare.formattedTotalFare,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Base Fare",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = selectedFare.formattedBaseFare,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Distance (${String.format("%.1f", selectedFare.distanceKm)} km)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = selectedFare.formattedDistanceCharge,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Time (${selectedFare.durationMinutes} min)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = selectedFare.formattedTimeCharge,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Primary Call to Action
        val canRequestRide = pickupLocation != null && destinationLocation != null && routeInfo != null
        val ctaText = if (selectedFare != null) {
            "Request ${selectedRideTier.displayName} • ${selectedFare.formattedTotalFare}"
        } else if (canRequestRide) {
            "Request ${selectedRideTier.displayName}"
        } else {
            "Select Pickup & Destination"
        }

        JJNPrimaryButton(
            text = ctaText,
            onClick = { /* Ready for future booking flow */ },
            enabled = canRequestRide
        )
    }

    // Place Search Modal Bottom Sheet
    if (isSearchBottomSheetVisible) {
        PlaceSearchBottomSheet(
            target = activeLocationTarget,
            searchQuery = searchQuery,
            isSearching = isSearchingPlaces,
            suggestions = placeSuggestions,
            onQueryChange = onUpdateSearchQuery,
            onSelectSuggestion = onSelectPlaceSuggestion,
            onSelectOnMap = { onStartMapSelection(activeLocationTarget) },
            onDismiss = onClosePlaceSearch
        )
    }
}

@Composable
private fun RideCategoryCard(
    tier: RideTier,
    fare: FareBreakdown?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    JJNCard(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        contentPadding = MaterialTheme.spacing.small,
        elevation = if (isSelected) 3.dp else 1.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = tier.icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tier.displayName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = fare?.formattedTotalFare ?: "Est. fare",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (fare != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
