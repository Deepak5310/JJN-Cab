package com.deecode.myapp.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashNavigationTarget {
    data object Unauthenticated : SplashNavigationTarget
    data class Authenticated(val role: UserRole) : SplashNavigationTarget
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _navigationChannel = Channel<SplashNavigationTarget>(Channel.BUFFERED)
    val navigationEvent = _navigationChannel.receiveAsFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            val authUser = authRepository.currentUser
            if (authUser == null) {
                _navigationChannel.send(SplashNavigationTarget.Unauthenticated)
                return@launch
            }

            when (val profileResult = userRepository.getUserProfile(authUser.uid)) {
                is Resource.Success -> {
                    _navigationChannel.send(SplashNavigationTarget.Authenticated(profileResult.data.role))
                }
                is Resource.Error -> {
                    authRepository.signOut()
                    _navigationChannel.send(SplashNavigationTarget.Unauthenticated)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
