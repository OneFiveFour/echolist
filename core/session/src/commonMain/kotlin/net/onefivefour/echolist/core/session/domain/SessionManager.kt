package net.onefivefour.echolist.core.session.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for the authentication state of the app. Any screen can start a logout;
 * once the backend confirmed it, [authState] becomes [AuthState.Unauthenticated] and the app shows
 * the login, which drops the whole authenticated back stack.
 */
class SessionManager(
    private val authRepository: AuthRepository,
    private val authEventBus: AuthEventBus,
    private val scope: CoroutineScope,
    private val sessionDataCleaners: List<SessionDataCleaner> = emptyList()
) {

    val authState: StateFlow<AuthState>
        field = MutableStateFlow<AuthState>(
            if (authRepository.isAuthenticated()) AuthState.Authenticated else AuthState.Unauthenticated
        )

    val logoutState: StateFlow<LogoutState>
        field = MutableStateFlow<LogoutState>(LogoutState.Idle)

    init {
        // Collect auth events from the interceptor
        scope.launch {
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
        scope.launch {
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

        scope.launch {
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
