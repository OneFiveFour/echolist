package net.onefivefour.echolist.core.session.domain

/**
 * Observable authentication state used by navigation gating.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
}
