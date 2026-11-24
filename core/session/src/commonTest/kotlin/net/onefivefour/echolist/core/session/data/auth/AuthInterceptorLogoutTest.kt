package net.onefivefour.echolist.core.session.data.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.AuthEvent
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.StorageKeys

class AuthInterceptorLogoutTest : FunSpec({

    fun repository(storage: FakeSecureStorage, onRefresh: () -> Unit) = object : AuthRepository {
        override suspend fun login(baseUrl: String, username: String, password: String) = Result.success(Unit)
        override suspend fun refreshToken(): Result<String> {
            onRefresh()
            return Result.failure(Exception("refresh"))
        }
        override suspend fun logout(): Result<Unit> = Result.success(Unit)
        override fun isAuthenticated() = storage.get(StorageKeys.ACCESS_TOKEN) != null
        override fun clearAuth() {
            storage.delete(StorageKeys.ACCESS_TOKEN)
            storage.delete(StorageKeys.REFRESH_TOKEN)
        }
        override fun getAccessToken() = storage.get(StorageKeys.ACCESS_TOKEN)
        override fun getBaseUrl() = storage.get(StorageKeys.BACKEND_URL)
    }

    test("a 401 from Logout neither attaches Authorization nor starts a refresh") {
        val storage = FakeSecureStorage().apply { put(StorageKeys.ACCESS_TOKEN, "access") }
        var refreshCount = 0
        var authorization: String? = "unset"
        val client = HttpClient(
            MockEngine { request ->
                authorization = request.headers[HttpHeaders.Authorization]
                respond("unauthenticated", HttpStatusCode.Unauthorized)
            }
        ) {
            install(AuthInterceptor) {
                authRepository = repository(storage) { refreshCount++ }
                authEventBus = AuthEventBus()
            }
        }

        client.post("http://localhost/auth.v1.AuthService/Logout")

        refreshCount shouldBe 0
        authorization shouldBe null
        client.close()
    }

    test("LoggedOut aborts a request that is still in flight") {
        runTest {
            val storage = FakeSecureStorage().apply { put(StorageKeys.ACCESS_TOKEN, "access") }
            val bus = AuthEventBus()
            val started = CompletableDeferred<Unit>()
            val client = HttpClient(
                MockEngine {
                    started.complete(Unit)
                    awaitCancellation()
                }
            ) {
                install(AuthInterceptor) {
                    authRepository = repository(storage) {}
                    authEventBus = bus
                }
            }

            val request = async(start = CoroutineStart.UNDISPATCHED) { runCatching { client.get("http://localhost/api") } }
            started.await()
            bus.emit(AuthEvent.LoggedOut)

            request.await().isFailure shouldBe true
            client.close()
        }
    }
})
