package net.onefivefour.echolist.core.session.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.networking.data.config.NetworkConfigProvider
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.StorageKeys

class AuthRepositoryRefreshTest : FunSpec({
    test("refresh persists rotated credentials and uses them on the next refresh") {
        val storage = FakeSecureStorage()
        storage.put(StorageKeys.BACKEND_URL, "https://example.test")
        storage.put(StorageKeys.ACCESS_TOKEN, "old-access")
        storage.put(StorageKeys.REFRESH_TOKEN, "old-refresh")
        val sentTokens = mutableListOf<String>()
        val client = object : ConnectRpcClient {
            override suspend fun <Req, Res> call(
                path: String,
                request: Req,
                requestSerializer: (Req) -> ByteArray,
                responseDeserializer: (ByteArray) -> Res
            ): Result<Res> {
                path shouldBe "/auth.v1.AuthService/RefreshToken"
                sentTokens += auth.v1.RefreshTokenRequest.ADAPTER.decode(requestSerializer(request)).refresh_token
                return Result.success(responseDeserializer(auth.v1.RefreshTokenResponse.ADAPTER.encode(
                    auth.v1.RefreshTokenResponse(access_token = "new-access", refresh_token = "new-refresh")
                )))
            }
        }
        val repo = AuthRepositoryImpl(storage, client, NetworkConfigProvider(storage))
        repo.refreshToken().getOrThrow() shouldBe "new-access"
        storage.get(StorageKeys.ACCESS_TOKEN) shouldBe "new-access"
        storage.get(StorageKeys.REFRESH_TOKEN) shouldBe "new-refresh"
        repo.refreshToken().getOrThrow()
        sentTokens shouldBe listOf("old-refresh", "new-refresh")
    }
})
