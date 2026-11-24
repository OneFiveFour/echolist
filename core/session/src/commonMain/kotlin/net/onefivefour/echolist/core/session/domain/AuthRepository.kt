package net.onefivefour.echolist.core.session.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Repository abstraction for authentication operations.
 * Handles login, token refresh, and credential management via [SecureStorage].
 */
interface AuthRepository {
    suspend fun login(baseUrl: String, username: String, password: String): Result<Unit>
    suspend fun refreshToken(): Result<String>
    /**
     * Revokes this device's session on the backend using the latest stored refresh token.
     * Tokens are cleared only when the backend confirms (success or `unauthenticated`); any other
     * failure keeps them so the call can be retried.
     */
    suspend fun logout(): Result<Unit>

    /** Emits when the stored session was removed by another tab or window. */
    fun observeExternalLogout(): Flow<Unit> = emptyFlow()
    fun isAuthenticated(): Boolean
    fun clearAuth()
    fun getAccessToken(): String?
    fun getBaseUrl(): String?
}