package com.deecode.myapp.data.datasource.route

import android.content.Context
import android.content.pm.PackageManager
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.util.PolylineDecoder
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.RouteInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class DefaultRouteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) : RouteDataSource {

    private val apiKey: String by lazy {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun calculateRoute(
        origin: LocationPoint,
        destination: LocationPoint
    ): Resource<RouteInfo> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Resource.Error("Google Maps/Routes API key is missing.")
        }

        try {
            val jsonBody = JSONObject().apply {
                put("origin", JSONObject().apply {
                    put("location", JSONObject().apply {
                        put("latLng", JSONObject().apply {
                            put("latitude", origin.latitude)
                            put("longitude", origin.longitude)
                        })
                    })
                })
                put("destination", JSONObject().apply {
                    put("location", JSONObject().apply {
                        put("latLng", JSONObject().apply {
                            put("latitude", destination.latitude)
                            put("longitude", destination.longitude)
                        })
                    })
                })
                put("travelMode", "DRIVE")
                put("routingPreference", "TRAFFIC_AWARE")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://routes.googleapis.com/directions/v2:computeRoutes")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", apiKey)
                .addHeader(
                    "X-Goog-FieldMask",
                    "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline"
                )
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string()

            if (!response.isSuccessful || responseBodyString.isNullOrBlank()) {
                val errorMsg = if (!responseBodyString.isNullOrBlank()) {
                    try {
                        val errorJson = JSONObject(responseBodyString)
                        errorJson.optJSONObject("error")?.optString("message")
                            ?: "HTTP error ${response.code}"
                    } catch (e: Exception) {
                        "HTTP error ${response.code}"
                    }
                } else {
                    "Failed to fetch route. HTTP ${response.code}"
                }
                return@withContext Resource.Error(errorMsg)
            }

            val responseJson = JSONObject(responseBodyString)
            val routesArray = responseJson.optJSONArray("routes")

            if (routesArray == null || routesArray.length() == 0) {
                return@withContext Resource.Error("No driving route found between selected points.")
            }

            val routeObj = routesArray.getJSONObject(0)
            val distanceMeters = routeObj.optInt("distanceMeters", 0)
            val rawDuration = routeObj.optString("duration", "0s")
            val durationSeconds = rawDuration.removeSuffix("s").toLongOrNull() ?: 0L

            val polylineObj = routeObj.optJSONObject("polyline")
            val encodedPolyline = polylineObj?.optString("encodedPolyline") ?: ""
            val decodedPoints = if (encodedPolyline.isNotBlank()) {
                PolylineDecoder.decode(encodedPolyline)
            } else {
                listOf(origin, destination)
            }

            Resource.Success(
                RouteInfo(
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    encodedPolyline = encodedPolyline,
                    points = decodedPoints
                )
            )
        } catch (e: IOException) {
            Resource.Error("Network error calculating route. Check your internet connection.", e)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unexpected error calculating route", e)
        }
    }
}
