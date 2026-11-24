package net.onefivefour.echolist.core.session.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.session.domain.StorageKeys
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.networking.data.config.NetworkConfigProvider
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.AuthFailure

internal class AuthRepositoryImpl(
    private val secureStorage: SecureStorage,
    private val client: ConnectRpcClient,
    private val networkConfigProvider: NetworkConfigProvider
) : AuthRepository {

    // Serializes refresh and logout so logout always sends the most recently rotated refresh token.
    private val refreshTokenMutex = Mutex()

    override suspend fun login(
        baseUrl: String,
        username: String,
        password: String
    ): Result<Unit> {
        networkConfigProvider.updateBaseUrl(baseUrl)

        val request = auth.v1.LoginRequest(
            username = username,
            password = password,
            device_name = deviceSessionName()
        )

        return client.call(
            path = "/auth.v1.AuthService/Login",
            request = request,
            requestSerializer = { auth.v1.LoginRequest.ADAPTER.encode(it) },
            responseDeserializer = { auth.v1.LoginResponse.ADAPTER.decode(it) }
        ).map { response ->
            secureStorage.put(StorageKeys.REFRESH_TOKEN, response.refresh_token)
            secureStorage.put(StorageKeys.BACKEND_URL, baseUrl)
            secureStorage.put(StorageKeys.NOTIFICATION_ACCOUNT_ID, "${baseUrl.trimEnd('/')}\n$username")
            // Publish authentication only after its backend/account scope is available to background workers.
            secureStorage.put(StorageKeys.ACCESS_TOKEN, response.access_token)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(AuthFailure(AuthErrorMapper.fromNetworkException(it))) }
        )
    }

    override suspend fun refreshToken(): Result<String> = refreshTokenMutex.withLock {
        val baseUrl = secureStorage.get(StorageKeys.BACKEND_URL)
            ?: return@withLock Result.failure(IllegalStateException("No backend URL stored"))
        val refreshToken = secureStorage.get(StorageKeys.REFRESH_TOKEN)
            ?: return@withLock Result.failure(IllegalStateException("No refresh token stored"))

        networkConfigProvider.updateBaseUrl(baseUrl)

        val request = auth.v1.RefreshTokenRequest(refresh_token = refreshToken)

        client.call(
            path = "/auth.v1.AuthService/RefreshToken",
            request = request,
            requestSerializer = { auth.v1.RefreshTokenRequest.ADAPTER.encode(it) },
            responseDeserializer = { auth.v1.RefreshTokenResponse.ADAPTER.decode(it) }
        ).map { response ->
            // Persist the rotated refresh token first, before publishing access.
            // Empty is accepted for compatibility with older backend deployments.
            if (response.refresh_token.isNotEmpty()) {
                secureStorage.put(StorageKeys.REFRESH_TOKEN, response.refresh_token)
            }
            secureStorage.put(StorageKeys.ACCESS_TOKEN, response.access_token)
            response.access_token
        }
    }

    override suspend fun logout(): Result<Unit> = refreshTokenMutex.withLock {
        val baseUrl = secureStorage.get(StorageKeys.BACKEND_URL)
        val refreshToken = secureStorage.get(StorageKeys.REFRESH_TOKEN)
        if (baseUrl == null || refreshToken == null) {
            // Without a refresh token there is no server session this device can revoke.
            clearAuth()
            return@withLock Result.success(Unit)
        }

        networkConfigProvider.updateBaseUrl(baseUrl)

        // Public client: no Authorization header and no auth interceptor, so a 401 never triggers a refresh.
        val result = client.call(
            path = "/auth.v1.AuthService/Logout",
            request = auth.v1.LogoutRequest(refresh_token = refreshToken),
            requestSerializer = { auth.v1.LogoutRequest.ADAPTER.encode(it) },
            responseDeserializer = { auth.v1.LogoutResponse.ADAPTER.decode(it) }
        )
        currentCoroutineContext().ensureActive()

        val failure = result.exceptionOrNull()
        when {
            failure == null -> {
                clearAuth()
                Result.success(Unit)
            }
            failure is CancellationException -> throw failure
            // 401 means invalid or expired: nothing was revoked, but the session is unusable anyway.
            failure is NetworkException.ClientError && failure.code == UNAUTHORIZED -> {
                clearAuth()
                Result.success(Unit)
            }
            // Anything else leaves the revocation unconfirmed, so tokens stay for a retry.
            else -> Result.failure(AuthFailure(AuthErrorMapper.fromNetworkException(failure)))
        }
    }

    override fun observeExternalLogout(): Flow<Unit> = secureStorage.observeDeletions(StorageKeys.ACCESS_TOKEN)

    override fun isAuthenticated(): Boolean {
        return secureStorage.get(StorageKeys.ACCESS_TOKEN) != null
    }

    override fun clearAuth() {
        secureStorage.delete(StorageKeys.ACCESS_TOKEN)
        secureStorage.delete(StorageKeys.REFRESH_TOKEN)
    }

    override fun getAccessToken(): String? {
        return secureStorage.get(StorageKeys.ACCESS_TOKEN)
    }

    override fun getBaseUrl(): String? {
        return secureStorage.get(StorageKeys.BACKEND_URL)
    }

    private companion object {
        const val UNAUTHORIZED = 401
    }
}
