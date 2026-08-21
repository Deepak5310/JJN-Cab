package com.deecode.myapp.feature.admin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun onEvent(event: AdminUiEvent) {
        when (event) {
            is AdminUiEvent.RefreshMetrics -> {
                // Future event handling
            }
        }
    }
}
