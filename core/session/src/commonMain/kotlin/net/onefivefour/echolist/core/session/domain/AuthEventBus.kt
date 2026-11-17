package net.onefivefour.echolist.core.session.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Concrete DI type for auth events so Koin never confuses this flow with
 * other generic MutableSharedFlow registrations after JVM type erasure.
 */
class AuthEventBus(
    eventFlow: MutableSharedFlow<AuthEvent> = MutableSharedFlow()
) {

    val events: SharedFlow<AuthEvent>
        field = eventFlow

    suspend fun emit(event: AuthEvent) {
        events.emit(event)
    }
}