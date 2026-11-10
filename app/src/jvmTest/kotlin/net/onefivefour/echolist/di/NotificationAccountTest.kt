package net.onefivefour.echolist.di

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.session.domain.StorageKeys

class NotificationAccountTest : FunSpec({
    test("concurrent migration of an existing login keeps one notification account across restart") {
        val firstReads = CyclicBarrier(2)
        val values = ConcurrentHashMap<String, String>().apply {
            put(StorageKeys.ACCESS_TOKEN, "existing-token")
        }
        val storage = object : SecureStorage {
            override fun get(key: String): String? {
                val value = values[key]
                if (key == StorageKeys.NOTIFICATION_ACCOUNT_ID && value == null) {
                    firstReads.await(5, TimeUnit.SECONDS)
                }
                return value
            }
            override fun put(key: String, value: String) { values[key] = value }
            override fun delete(key: String) { values.remove(key) }
        }
        val account = NotificationAccount(storage)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val first = executor.submit<String?> { account.currentAccountId() }
            val second = executor.submit<String?> { account.currentAccountId() }
            val firstId = first.get(10, TimeUnit.SECONDS)
            val secondId = second.get(10, TimeUnit.SECONDS)
            firstId shouldBe secondId
            values[StorageKeys.NOTIFICATION_ACCOUNT_ID] shouldBe firstId
            NotificationAccount(storage).currentAccountId() shouldBe firstId
        } finally {
            executor.shutdownNow()
        }
    }
})
