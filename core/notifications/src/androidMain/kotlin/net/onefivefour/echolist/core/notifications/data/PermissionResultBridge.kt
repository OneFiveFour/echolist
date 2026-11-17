package net.onefivefour.echolist.core.notifications.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Bridge between ViewModel-layer coroutines and Activity-lifecycle permission handling.
 *
 * The ViewModel (via [AndroidNotificationPermissionRequester]) calls [awaitPermission]
 * which suspends. The Activity observes [requests], handles the full permission flow
 * (rationale → system dialog → settings redirect if needed), then delivers the result.
 */
class PermissionResultBridge {
    private var pendingResult: CompletableDeferred<Boolean>? = null
    val requests: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Called by the requester. Suspends until the Activity handles the permission flow.
     *
     * @return true if permission was ultimately granted, false otherwise.
     */
    suspend fun awaitPermission(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingResult = deferred
        requests.emit(Unit)
        return deferred.await()
    }

    /**
     * Called by the Activity after the permission result is known.
     */
    fun deliverResult(granted: Boolean) {
        pendingResult?.complete(granted)
        pendingResult = null
    }

    /**
     * Cancel a pending request (e.g., Activity destroyed).
     */
    fun cancel() {
        pendingResult?.complete(false)
        pendingResult = null
    }
}