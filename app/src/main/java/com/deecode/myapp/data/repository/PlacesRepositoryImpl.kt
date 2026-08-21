package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.datasource.places.PlacesDataSource
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.PlaceSuggestion
import com.deecode.myapp.domain.repository.PlacesRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val placesDataSource: PlacesDataSource,
    private val dispatchers: DispatcherProvider
) : PlacesRepository {

    override suspend fun searchPlaces(query: String): Resource<List<PlaceSuggestion>> =
        withContext(dispatchers.io) {
            placesDataSource.searchPlaces(query)
        }

    override suspend fun getPlaceDetails(placeId: String): Resource<LocationPoint> =
        withContext(dispatchers.io) {
            placesDataSource.getPlaceDetails(placeId)
        }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Resource<String> =
        withContext(dispatchers.io) {
            placesDataSource.reverseGeocode(latitude, longitude)
        }
}
