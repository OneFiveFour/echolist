package net.onefivefour.echolist.core.session.data.auth

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.session.domain.AuthEvent
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.AuthRepository

/**
 * Ktor client plugin that:
 * 1. Attaches Bearer token on non-auth endpoints from SecureStorage
 * 2. Handles 401 responses: acquires mutex, refreshes token, retries original request
 * 3. Only a rejected refresh token clears auth; transient failures preserve the session.
 */
val AuthInterceptor = createClientPlugin("AuthInterceptor", ::AuthInterceptorConfig) {
    val authRepository = pluginConfig.authRepository
    val authEventBus = pluginConfig.authEventBus
    val refreshMutex = Mutex()

    onRequest { request, _ ->
        val path = request.url.buildString()
        if (!isAuthEndpoint(path)) {
            authRepository.getAccessToken()?.let { token ->
                request.header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    suspend fun Send.Sender.sendWithRefresh(request: HttpRequestBuilder): HttpClientCall {
        val originalCall = proceed(request)

        val path = originalCall.request.url.encodedPath
        if (originalCall.response.status != HttpStatusCode.Unauthorized || isAuthEndpoint(path)) {
            return originalCall
        }

        val refreshResult = refreshMutex.withLock {
            // Another request may already have refreshed while this one waited.
            val currentToken = authRepository.getAccessToken()
            val sentToken = originalCall.request.headers[HttpHeaders.Authorization]
            if (currentToken != null && "Bearer $currentToken" != sentToken) {
                Result.success(currentToken)
            } else {
                authRepository.refreshToken().also { result ->
                    val failure = result.exceptionOrNull()
                    if (failure is CancellationException) throw failure
                    if (failure is NetworkException.ClientError && failure.code == 401) {
                        authRepository.clearAuth()
                        authEventBus.emit(AuthEvent.ReAuthRequired)
                    }
                }
            }
        }

        return if (refreshResult.isSuccess) {
            // Retry with the new token
            val newToken = refreshResult.getOrThrow()
            val retryRequest = HttpRequestBuilder().apply {
                takeFrom(request)
                headers.remove(HttpHeaders.Authorization)
                header(HttpHeaders.Authorization, "Bearer $newToken")
            }
            proceed(retryRequest)
        } else {
            // A timeout, offline device or unavailable backend does not log out.
            originalCall
        }
    }

    on(Send) { request ->
        val sender = this
        // A logout (here or in another tab) aborts requests in flight so none finishes with the old tokens.
        coroutineScope {
            val call = async { sender.sendWithRefresh(request) }
            val abortOnLogout = launch(start = CoroutineStart.UNDISPATCHED) {
                authEventBus.events.first { it == AuthEvent.LoggedOut }
                call.cancel(CancellationException("Signed out"))
            }
            try {
                call.await()
            } finally {
                abortOnLogout.cancel()
            }
        }
    }
}

/**
 * Configuration for the [AuthInterceptor] Ktor client plugin.
 */
class AuthInterceptorConfig {
    lateinit var authRepository: AuthRepository
    lateinit var authEventBus: AuthEventBus
}

private val PUBLIC_AUTH_ENDPOINTS = setOf(
    "/auth.v1.AuthService/Login",
    "/auth.v1.AuthService/RefreshToken",
    "/auth.v1.AuthService/Logout"
)

private fun isAuthEndpoint(path: String): Boolean {
    return PUBLIC_AUTH_ENDPOINTS.any { path.endsWith(it) }
}