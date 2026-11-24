package net.onefivefour.echolist.core.session.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface SecureStorage : net.onefivefour.echolist.core.networking.domain.BackendUrlStore {
    override fun getBackendUrl(): String? = get(StorageKeys.BACKEND_URL)
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun delete(key: String)

    /** Emits when [key] is removed by another browser tab; storages without shared tabs never emit. */
    fun observeDeletions(key: String): Flow<Unit> = emptyFlow()
}

object StorageKeys {
    const val ACCESS_TOKEN = "access_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val BACKEND_URL = "backend_url"
    const val NOTIFICATION_ACCOUNT_ID = "notification_account_id"
}