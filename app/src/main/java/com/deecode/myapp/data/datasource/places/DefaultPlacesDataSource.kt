package com.deecode.myapp.data.datasource.places

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.PlaceSuggestion
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class DefaultPlacesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val placesClient: PlacesClient
) : PlacesDataSource {

    private val geocoder by lazy { Geocoder(context, Locale.getDefault()) }

    override suspend fun searchPlaces(query: String): Resource<List<PlaceSuggestion>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 2) {
            return Resource.Success(emptyList())
        }

        return try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(trimmedQuery)
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()
            val suggestions = response.autocompletePredictions.map { prediction ->
                PlaceSuggestion(
                    placeId = prediction.placeId,
                    primaryText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null).toString(),
                    fullText = prediction.getFullText(null).toString()
                )
            }
            Resource.Success(suggestions)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to search places", e)
        }
    }

    override suspend fun getPlaceDetails(placeId: String): Resource<LocationPoint> {
        return try {
            val placeFields = listOf(Place.Field.LOCATION, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS)
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            val location = place.location

            if (location != null) {
                Resource.Success(
                    LocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = place.formattedAddress ?: place.displayName ?: "${location.latitude}, ${location.longitude}"
                    )
                )
            } else {
                Resource.Error("Could not retrieve coordinates for the selected place.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch place details", e)
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Resource<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val address = addresses.firstOrNull()?.getAddressLine(0)
                                ?: String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
                            continuation.resume(Resource.Success(address))
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(
                                Resource.Success(
                                    String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
                                )
                            )
                        }
                    })
                }
            } else {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()?.getAddressLine(0)
                    ?: String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
                Resource.Success(address)
            }
        } catch (e: Exception) {
            Resource.Success(String.format(Locale.US, "%.4f, %.4f", latitude, longitude))
        }
    }
}
