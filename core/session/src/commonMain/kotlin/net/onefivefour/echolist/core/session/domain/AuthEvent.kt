package net.onefivefour.echolist.core.session.domain

/**
 * Events emitted by the auth layer to signal authentication state changes.
 */
sealed interface AuthEvent {
    data object ReAuthRequired : AuthEvent

    /** The session ended on this device, or in another tab: requests in flight must stop. */
    data object LoggedOut : AuthEvent
}