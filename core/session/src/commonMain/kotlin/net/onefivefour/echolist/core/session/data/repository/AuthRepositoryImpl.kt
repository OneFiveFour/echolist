package net.onefivefour.echolist.core.session.data.repository

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

    override suspend fun refreshToken(): Result<String> {
        val baseUrl = secureStorage.get(StorageKeys.BACKEND_URL)
            ?: return Result.failure(IllegalStateException("No backend URL stored"))
        val refreshToken = secureStorage.get(StorageKeys.REFRESH_TOKEN)
            ?: return Result.failure(IllegalStateException("No refresh token stored"))

        networkConfigProvider.updateBaseUrl(baseUrl)

        val request = auth.v1.RefreshTokenRequest(refresh_token = refreshToken)

        return client.call(
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
}