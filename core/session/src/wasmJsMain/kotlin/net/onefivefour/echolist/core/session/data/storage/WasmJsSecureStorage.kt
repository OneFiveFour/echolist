package net.onefivefour.echolist.core.session.data.storage

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.onefivefour.echolist.core.session.domain.SecureStorage
import org.w3c.dom.StorageEvent
import org.w3c.dom.events.Event

class WasmJsSecureStorage : SecureStorage {

    override fun get(key: String): String? = localStorage.getItem(key)

    override fun put(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override fun delete(key: String) {
        localStorage.removeItem(key)
    }

    // The browser fires "storage" only in other tabs; a null key means the whole storage was cleared.
    override fun observeDeletions(key: String): Flow<Unit> = callbackFlow {
        val listener: (Event) -> Unit = { event ->
            val storageEvent = event as StorageEvent
            if ((storageEvent.key == key || storageEvent.key == null) && storageEvent.newValue == null) {
                trySend(Unit)
            }
        }
        window.addEventListener("storage", listener)
        awaitClose { window.removeEventListener("storage", listener) }
    }
}
