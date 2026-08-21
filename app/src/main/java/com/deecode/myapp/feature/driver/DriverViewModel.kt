package com.deecode.myapp.feature.driver

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    fun onEvent(event: DriverUiEvent) {
        when (event) {
            is DriverUiEvent.ToggleOnlineStatus -> {
                _uiState.value = _uiState.value.copy(isOnline = event.isOnline)
            }
        }
    }
}
