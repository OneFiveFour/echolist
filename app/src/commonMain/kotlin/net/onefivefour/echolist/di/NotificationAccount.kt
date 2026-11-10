package net.onefivefour.echolist.di

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.session.domain.StorageKeys

/** Existing logins receive a stable device-local scope; new logins identify backend and username. */
@OptIn(ExperimentalUuidApi::class)
internal class NotificationAccount(private val storage: SecureStorage) : NotificationAccountProvider {
    // All concurrent first reads must persist the same migration ID for an existing login.
    private val migrationAccountId = Uuid.random().toString()

    override fun currentAccountId(): String? {
        if (storage.get(StorageKeys.ACCESS_TOKEN) == null) return null
        return storage.get(StorageKeys.NOTIFICATION_ACCOUNT_ID) ?: migrationAccountId.also {
            storage.put(StorageKeys.NOTIFICATION_ACCOUNT_ID, it)
        }
    }
}
