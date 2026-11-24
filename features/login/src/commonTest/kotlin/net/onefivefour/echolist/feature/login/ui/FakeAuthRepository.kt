package net.onefivefour.echolist.feature.login.ui

import net.onefivefour.echolist.core.session.domain.StorageKeys

import net.onefivefour.echolist.core.session.domain.AuthRepository

/**
 * Fake [AuthRepository] for LoginViewModel tests.
 * By default, login succeeds. Set [loginResult] to control behavior.
 */
internal open class FakeAuthRepository(
    private val storage: net.onefivefour.echolist.core.session.domain.SecureStorage? = null
) : AuthRepository {

    var loginResult: Result<Unit> = Result.success(Unit)

    private var authenticated = storage?.get(StorageKeys.ACCESS_TOKEN) != null

    open override suspend fun login(baseUrl: String, username: String, password: String): Result<Unit> {
        return loginResult.also { if (it.isSuccess) authenticated = true }
    }

    override suspend fun refreshToken(): Result<String> {
        return Result.failure(UnsupportedOperationException("Not used in login tests"))
    }

    override suspend fun logout(): Result<Unit> {
        authenticated = false
        return Result.success(Unit)
    }

    override fun isAuthenticated(): Boolean = authenticated

    override fun clearAuth() {
        authenticated = false
    }

    override fun getAccessToken(): String? = if (authenticated) "fake_token" else null

    override fun getBaseUrl(): String? = storage?.get(
        StorageKeys.BACKEND_URL
    )
}