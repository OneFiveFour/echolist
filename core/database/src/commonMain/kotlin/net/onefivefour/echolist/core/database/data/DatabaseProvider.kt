package net.onefivefour.echolist.core.database.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.onefivefour.echolist.cache.EchoListDatabase

class DatabaseProvider(private val initialize: suspend () -> EchoListDatabase) {
    private val mutex = Mutex()
    private var database: EchoListDatabase? = null

    suspend fun get(): EchoListDatabase = mutex.withLock {
        database ?: initialize().also { database = it }
    }
}