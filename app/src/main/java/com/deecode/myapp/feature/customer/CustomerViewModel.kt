package com.deecode.myapp.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.calculator.FareCalculator
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.PlaceSuggestion
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.LocationRepository
import com.deecode.myapp.domain.repository.PlacesRepository
import com.deecode.myapp.domain.repository.RouteRepository
import com.deecode.myapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
    private val placesRepository: PlacesRepository,
    private val routeRepository: RouteRepository,
    private val fareCalculator: FareCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState(isLoading = true))
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    private val _searchQueryFlow = MutableStateFlow("")
    private var lastCalculatedPair: Pair<LocationPoint, LocationPoint>? = null

    init {
        observeUserProfile()
        setupDebouncedSearch()
    }

    private fun observeUserProfile() {
        val authUser = authRepository.currentUser
        if (authUser == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User session expired.") }
            return
        }

        viewModelScope.launch {
            userRepository.observeUserProfile(authUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                user = resource.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun setupDebouncedSearch() {
        _searchQueryFlow
            .debounce(400)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.trim().length >= 2) {
                    searchPlaces(query)
                } else {
                    _uiState.update {
                        it.copy(
                            isSearchingPlaces = false,
                            placeSuggestions = emptyList()
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun searchPlaces(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingPlaces = true) }
            when (val result = placesRepository.searchPlaces(query)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSearchingPlaces = false,
                            placeSuggestions = result.data
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSearchingPlaces = false,
                            placeSuggestions = emptyList()
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onEvent(event: CustomerUiEvent) {
        when (event) {
            is CustomerUiEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is CustomerUiEvent.Refresh -> {
                observeUserProfile()
            }
            is CustomerUiEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is CustomerUiEvent.RequestLocation -> {
                fetchCurrentLocation()
            }
            is CustomerUiEvent.OnLocationPermissionDenied -> {
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        isPermissionPermanentlyDenied = event.permanentlyDenied,
                        locationError = if (event.permanentlyDenied) {
                            "Location permission permanently denied. Please enable it in App Settings."
                        } else {
                            "Location permission was denied. Location is needed to determine your pickup point."
                        }
                    )
                }
            }
            is CustomerUiEvent.ClearLocationError -> {
                _uiState.update { it.copy(locationError = null) }
            }
            is CustomerUiEvent.OpenPlaceSearch -> {
                _searchQueryFlow.value = ""
                _uiState.update {
                    it.copy(
                        isSearchBottomSheetVisible = true,
                        activeLocationTarget = event.target,
                        searchQuery = "",
                        placeSuggestions = emptyList()
                    )
                }
            }
            is CustomerUiEvent.ClosePlaceSearch -> {
                _uiState.update {
                    it.copy(
                        isSearchBottomSheetVisible = false,
                        searchQuery = "",
                        placeSuggestions = emptyList()
                    )
                }
            }
            is CustomerUiEvent.UpdateSearchQuery -> {
                _searchQueryFlow.value = event.query
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is CustomerUiEvent.SelectPlaceSuggestion -> {
                selectPlace(event.suggestion)
            }
            is CustomerUiEvent.ClearSelectedLocation -> {
                when (event.target) {
                    LocationTarget.PICKUP -> _uiState.update {
                        it.copy(
                            pickupLocation = null,
                            routeInfo = null,
                            routeError = null,
                            fareEstimates = emptyMap()
                        )
                    }
                    LocationTarget.DESTINATION -> _uiState.update {
                        it.copy(
                            destinationLocation = null,
                            routeInfo = null,
                            routeError = null,
                            fareEstimates = emptyMap()
                        )
                    }
                }
                lastCalculatedPair = null
            }
            is CustomerUiEvent.StartMapSelection -> {
                _uiState.update {
                    it.copy(
                        isSelectingOnMap = true,
                        activeLocationTarget = event.target,
                        isSearchBottomSheetVisible = false
                    )
                }
            }
            is CustomerUiEvent.ConfirmMapSelection -> {
                confirmMapSelection(event.latitude, event.longitude)
            }
            is CustomerUiEvent.CancelMapSelection -> {
                _uiState.update { it.copy(isSelectingOnMap = false) }
            }
            is CustomerUiEvent.CalculateRoute -> {
                checkAndCalculateRoute()
            }
            is CustomerUiEvent.ClearRoute -> {
                _uiState.update {
                    it.copy(
                        routeInfo = null,
                        routeError = null,
                        fareEstimates = emptyMap()
                    )
                }
                lastCalculatedPair = null
            }
            is CustomerUiEvent.SelectRideTier -> {
                _uiState.update { it.copy(selectedRideTier = event.tier) }
            }
        }
    }

    private fun selectPlace(suggestion: PlaceSuggestion) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingPlaces = true) }
            when (val result = placesRepository.getPlaceDetails(suggestion.placeId)) {
                is Resource.Success -> {
                    val location = result.data
                    _uiState.update { state ->
                        when (state.activeLocationTarget) {
                            LocationTarget.PICKUP -> state.copy(
                                pickupLocation = location,
                                isSearchingPlaces = false,
                                isSearchBottomSheetVisible = false
                            )
                            LocationTarget.DESTINATION -> state.copy(
                                destinationLocation = location,
                                isSearchingPlaces = false,
                                isSearchBottomSheetVisible = false
                            )
                        }
                    }
                    checkAndCalculateRoute()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSearchingPlaces = false,
                            locationError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun confirmMapSelection(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReverseGeocoding = true) }
            val address = when (val res = placesRepository.reverseGeocode(latitude, longitude)) {
                is Resource.Success -> res.data
                else -> String.format(java.util.Locale.US, "%.4f, %.4f", latitude, longitude)
            }

            val locationPoint = LocationPoint(
                latitude = latitude,
                longitude = longitude,
                address = address
            )

            _uiState.update { state ->
                when (state.activeLocationTarget) {
                    LocationTarget.PICKUP -> state.copy(
                        pickupLocation = locationPoint,
                        isSelectingOnMap = false,
                        isReverseGeocoding = false
                    )
                    LocationTarget.DESTINATION -> state.copy(
                        destinationLocation = locationPoint,
                        isSelectingOnMap = false,
                        isReverseGeocoding = false
                    )
                }
            }
            checkAndCalculateRoute()
        }
    }

    private fun checkAndCalculateRoute() {
        val currentState = _uiState.value
        val pickup = currentState.pickupLocation
        val destination = currentState.destinationLocation

        if (pickup != null && destination != null) {
            val pair = Pair(pickup, destination)
            if (pair != lastCalculatedPair) {
                lastCalculatedPair = pair
                calculateRoute(pickup, destination)
            }
        }
    }

    private fun calculateRoute(origin: LocationPoint, destination: LocationPoint) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingRoute = true, routeError = null) }

            when (val result = routeRepository.calculateRoute(origin, destination)) {
                is Resource.Success -> {
                    val route = result.data
                    val fares = fareCalculator.calculateAllTiers(
                        distanceMeters = route.distanceMeters,
                        durationSeconds = route.durationSeconds
                    )

                    _uiState.update {
                        it.copy(
                            routeInfo = route,
                            fareEstimates = fares,
                            isCalculatingRoute = false,
                            routeError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            routeInfo = null,
                            fareEstimates = emptyMap(),
                            isCalculatingRoute = false,
                            routeError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        return locationRepository.hasLocationPermission()
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, locationError = null) }

            when (val result = locationRepository.getCurrentLocation()) {
                is Resource.Success -> {
                    val location = result.data
                    val addressResult = placesRepository.reverseGeocode(location.latitude, location.longitude)
                    val resolvedAddress = if (addressResult is Resource.Success) addressResult.data else "Current GPS Location"
                    val resolvedLocation = location.copy(address = resolvedAddress)

                    _uiState.update {
                        it.copy(
                            currentLocation = resolvedLocation,
                            pickupLocation = it.pickupLocation ?: resolvedLocation,
                            isLocating = false,
                            locationError = null
                        )
                    }
                    checkAndCalculateRoute()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLocating = false,
                            locationError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }
}
