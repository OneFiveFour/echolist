package net.onefivefour.echolist.core.session.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.networking.data.config.NetworkConfigProvider
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.AuthError
import net.onefivefour.echolist.core.session.domain.AuthFailure
import net.onefivefour.echolist.core.session.domain.StorageKeys

class AuthRepositoryLogoutTest : FunSpec({

    fun storedSession() = FakeSecureStorage().apply {
        put(StorageKeys.BACKEND_URL, "https://example.test")
        put(StorageKeys.ACCESS_TOKEN, "access")
        put(StorageKeys.REFRESH_TOKEN, "refresh")
    }

    class RecordingClient(
        private val logoutResult: () -> Result<Unit> = { Result.success(Unit) }
    ) : ConnectRpcClient {
        val calls = mutableListOf<Pair<String, String>>()

        @Suppress("UNCHECKED_CAST")
        override suspend fun <Req, Res> call(
            path: String,
            request: Req,
            requestSerializer: (Req) -> ByteArray,
            responseDeserializer: (ByteArray) -> Res
        ): Result<Res> {
            val token = when (path) {
                "/auth.v1.AuthService/Logout" ->
                    auth.v1.LogoutRequest.ADAPTER.decode(requestSerializer(request)).refresh_token
                else -> auth.v1.RefreshTokenRequest.ADAPTER.decode(requestSerializer(request)).refresh_token
            }
            calls += path to token
            if (path != "/auth.v1.AuthService/Logout") {
                return Result.success(
                    responseDeserializer(
                        auth.v1.RefreshTokenResponse.ADAPTER.encode(
                            auth.v1.RefreshTokenResponse(access_token = "access-2", refresh_token = "refresh-2")
                        )
                    )
                )
            }
            return logoutResult().map {
                responseDeserializer(auth.v1.LogoutResponse.ADAPTER.encode(auth.v1.LogoutResponse()))
            }
        }
    }

    fun repository(storage: FakeSecureStorage, client: ConnectRpcClient) =
        AuthRepositoryImpl(storage, client, NetworkConfigProvider(storage))

    test("200 sends the stored refresh token and clears both tokens") {
        val storage = storedSession()
        val client = RecordingClient()

        repository(storage, client).logout().isSuccess shouldBe true

        client.calls shouldBe listOf("/auth.v1.AuthService/Logout" to "refresh")
        storage.get(StorageKeys.ACCESS_TOKEN) shouldBe null
        storage.get(StorageKeys.REFRESH_TOKEN) shouldBe null
    }

    test("unauthenticated (401) counts as signed out and clears tokens") {
        val storage = storedSession()
        val client = RecordingClient { Result.failure(NetworkException.ClientError(401, "{\"code\":\"unauthenticated\"}")) }

        repository(storage, client).logout().isSuccess shouldBe true

        client.calls.size shouldBe 1
        storage.get(StorageKeys.ACCESS_TOKEN) shouldBe null
        storage.get(StorageKeys.REFRESH_TOKEN) shouldBe null
    }

    listOf<Pair<String, NetworkException>>(
        "network error" to NetworkException.NetworkError("offline"),
        "timeout" to NetworkException.TimeoutError("slow"),
        "server error" to NetworkException.ServerError(503, "unavailable"),
        "other client error" to NetworkException.ClientError(400, "bad request")
    ).forEach { (name, error) ->
        test("$name keeps tokens and reports a failure") {
            val storage = storedSession()
            val client = RecordingClient { Result.failure(error) }

            val result = repository(storage, client).logout()

            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<AuthFailure>()
            storage.get(StorageKeys.ACCESS_TOKEN) shouldBe "access"
            storage.get(StorageKeys.REFRESH_TOKEN) shouldBe "refresh"
        }
    }

    test("failure messages never contain the tokens") {
        val storage = storedSession()
        val client = RecordingClient { Result.failure(NetworkException.NetworkError("offline")) }

        val failure = repository(storage, client).logout().exceptionOrNull() as AuthFailure

        failure.error.shouldBeInstanceOf<AuthError.NetworkError>()
        (failure.message?.contains("refresh") ?: false) shouldBe false
    }

    test("a retry after a failure succeeds with the same token") {
        val storage = storedSession()
        var attempts = 0
        val client = RecordingClient {
            if (attempts++ == 0) Result.failure(NetworkException.NetworkError("offline")) else Result.success(Unit)
        }
        val repo = repository(storage, client)

        repo.logout().isFailure shouldBe true
        repo.logout().isSuccess shouldBe true

        client.calls.map { it.second } shouldBe listOf("refresh", "refresh")
        storage.get(StorageKeys.REFRESH_TOKEN) shouldBe null
    }

    test("without a stored refresh token nothing is sent and local state is cleared") {
        val storage = FakeSecureStorage().apply { put(StorageKeys.ACCESS_TOKEN, "access") }
        val client = RecordingClient()

        repository(storage, client).logout().isSuccess shouldBe true

        client.calls shouldBe emptyList()
        storage.get(StorageKeys.ACCESS_TOKEN) shouldBe null
    }

    test("logout waits for a running refresh and sends the rotated token") {
        runTest {
            val storage = storedSession()
            val refreshStarted = CompletableDeferred<Unit>()
            val releaseRefresh = CompletableDeferred<Unit>()
            val calls = mutableListOf<Pair<String, String>>()
            val client = object : ConnectRpcClient {
                override suspend fun <Req, Res> call(
                    path: String,
                    request: Req,
                    requestSerializer: (Req) -> ByteArray,
                    responseDeserializer: (ByteArray) -> Res
                ): Result<Res> {
                    val bytes = requestSerializer(request)
                    return if (path.endsWith("RefreshToken")) {
                        calls += path to auth.v1.RefreshTokenRequest.ADAPTER.decode(bytes).refresh_token
                        refreshStarted.complete(Unit)
                        releaseRefresh.await()
                        Result.success(
                            responseDeserializer(
                                auth.v1.RefreshTokenResponse.ADAPTER.encode(
                                    auth.v1.RefreshTokenResponse(access_token = "access-2", refresh_token = "refresh-2")
                                )
                            )
                        )
                    } else {
                        calls += path to auth.v1.LogoutRequest.ADAPTER.decode(bytes).refresh_token
                        Result.success(responseDeserializer(auth.v1.LogoutResponse.ADAPTER.encode(auth.v1.LogoutResponse())))
                    }
                }
            }
            val repo = repository(storage, client)

            val refresh = async { repo.refreshToken() }
            refreshStarted.await()
            val logout = async { repo.logout() }
            launch { releaseRefresh.complete(Unit) }

            refresh.await().isSuccess shouldBe true
            logout.await().isSuccess shouldBe true
            calls.map { it.second } shouldBe listOf("refresh", "refresh-2")
        }
    }
})
