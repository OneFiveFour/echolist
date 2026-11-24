package net.onefivefour.echolist.core.session.domain

/**
 * Implemented by modules holding user-specific local data (for example caches).
 * Every binding is invoked after a completed logout.
 */
fun interface SessionDataCleaner {
    suspend fun clear()
}
