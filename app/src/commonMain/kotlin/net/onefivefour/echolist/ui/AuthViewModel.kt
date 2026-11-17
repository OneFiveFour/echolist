package net.onefivefour.echolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.session.domain.AuthEvent
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    val authState: StateFlow<AuthState>
        field = MutableStateFlow<AuthState>(AuthState.Loading)

    init {
        // Check storage for existing access token
        authState.value = if (authRepository.isAuthenticated()) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }

        // Collect auth events from the interceptor
        viewModelScope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    AuthEvent.ReAuthRequired -> {
                        authState.value = AuthState.Unauthenticated
                    }
                }
            }
        }
    }

    fun onAuthenticated() {
        authState.value = AuthState.Authenticated
    }
}