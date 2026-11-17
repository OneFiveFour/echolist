package net.onefivefour.echolist.core.session.data.storage

import net.onefivefour.echolist.core.session.domain.SecureStorage

/**
 * In-memory [SecureStorage] implementation for use in common tests.
 */
internal class FakeSecureStorage : SecureStorage {

    private val store = mutableMapOf<String, String>()

    override fun get(key: String): String? = store[key]

    override fun put(key: String, value: String) {
        store[key] = value
    }

    override fun delete(key: String) {
        store.remove(key)
    }
}