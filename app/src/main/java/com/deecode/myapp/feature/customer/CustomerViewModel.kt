package com.deecode.myapp.feature.customer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CustomerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    fun onEvent(event: CustomerUiEvent) {
        when (event) {
            is CustomerUiEvent.Refresh -> {
                // Future event handling
            }
        }
    }
}
