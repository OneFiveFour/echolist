package net.onefivefour.echolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.session.domain.AuthEvent
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.SessionDataCleaner

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val authEventBus: AuthEventBus,
    private val sessionDataCleaners: List<SessionDataCleaner> = emptyList()
) : ViewModel() {

    val authState: StateFlow<AuthState>
        field = MutableStateFlow<AuthState>(AuthState.Loading)

    val logoutState: StateFlow<LogoutState>
        field = MutableStateFlow<LogoutState>(LogoutState.Idle)

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

                    AuthEvent.LoggedOut -> {
                        clearSessionData()
                        logoutState.value = LogoutState.Idle
                        authState.value = AuthState.Unauthenticated
                    }
                }
            }
        }

        // Another browser tab or window ended the session.
        viewModelScope.launch {
            authRepository.observeExternalLogout().collect {
                if (authState.value == AuthState.Authenticated) {
                    authEventBus.emit(AuthEvent.LoggedOut)
                }
            }
        }
    }

    fun onAuthenticated() {
        authState.value = AuthState.Authenticated
    }

    fun logout() {
        if (logoutState.value == LogoutState.InProgress) return
        logoutState.value = LogoutState.InProgress

        viewModelScope.launch {
            authRepository.logout().fold(
                // Tokens are already cleared; this stops requests in flight and resets local data.
                onSuccess = { authEventBus.emit(AuthEvent.LoggedOut) },
                onFailure = { failure ->
                    if (failure is CancellationException) throw failure
                    logoutState.value = LogoutState.Failed
                }
            )
        }
    }

    fun dismissLogoutError() {
        if (logoutState.value == LogoutState.Failed) {
            logoutState.value = LogoutState.Idle
        }
    }

    private suspend fun clearSessionData() {
        sessionDataCleaners.forEach { cleaner ->
            try {
                cleaner.clear()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // One failing cache must not keep the user signed in or block the other cleaners.
            }
        }
    }
}