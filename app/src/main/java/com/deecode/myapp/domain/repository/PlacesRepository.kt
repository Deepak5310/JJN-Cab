package com.deecode.myapp.domain.repository

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.PlaceSuggestion

interface PlacesRepository {
    suspend fun searchPlaces(query: String): Resource<List<PlaceSuggestion>>
    suspend fun getPlaceDetails(placeId: String): Resource<LocationPoint>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Resource<String>
}
