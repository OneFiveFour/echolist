package net.onefivefour.echolist.core.session.domain

/**
 * Progress of a user-initiated logout. [Failed] means the backend did not confirm the revocation,
 * so the session is still stored and the user can retry.
 */
sealed interface LogoutState {
    data object Idle : LogoutState
    data object InProgress : LogoutState
    data object Failed : LogoutState
}
