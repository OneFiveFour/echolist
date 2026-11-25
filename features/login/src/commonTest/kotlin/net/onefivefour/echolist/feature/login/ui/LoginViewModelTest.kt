package net.onefivefour.echolist.feature.login.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.error_backend_url_invalid
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    test("the password is sent exactly as entered while URL and username are trimmed") {
        runTest(testDispatcher) {
            var sentCredentials: Triple<String, String, String>? = null
            val authRepository = object : FakeAuthRepository() {
                override suspend fun login(baseUrl: String, username: String, password: String): Result<Unit> {
                    sentCredentials = Triple(baseUrl, username, password)
                    return Result.success(Unit)
                }
            }
            val loginViewModel = LoginViewModel(authRepository)

            loginViewModel.onBackendUrlChanged("  https://echo.example.com  ")
            loginViewModel.onUsernameChanged("  alex  ")
            loginViewModel.onPasswordChanged("  secret with spaces  ")
            loginViewModel.onLoginClick()
            advanceUntilIdle()

            sentCredentials shouldBe Triple("https://echo.example.com", "alex", "  secret with spaces  ")
        }
    }

    test("malformed backend URLs are rejected before a login request") {
        val invalidUrls = listOf(
            "https://",
            "http://",
            "echo.example.com",
            "ftp://echo.example.com",
            "https://echo example.com",
            "https://echo.example.com:",
            "https://user@echo.example.com"
        )

        for (invalidUrl in invalidUrls) {
            runTest(testDispatcher) {
                var hasSentLogin = false
                val authRepository = object : FakeAuthRepository() {
                    override suspend fun login(baseUrl: String, username: String, password: String): Result<Unit> {
                        hasSentLogin = true
                        return Result.success(Unit)
                    }
                }
                val loginViewModel = LoginViewModel(authRepository)

                loginViewModel.onBackendUrlChanged(invalidUrl)
                loginViewModel.onUsernameChanged("alex")
                loginViewModel.onPasswordChanged("secret")
                loginViewModel.onLoginClick()
                advanceUntilIdle()

                val invalidUrlMessage = getString(Res.string.error_backend_url_invalid)
                loginViewModel.loginUiState.value.backendUrlError shouldBe invalidUrlMessage
                loginViewModel.loginUiState.value.isLoading shouldBe false
                hasSentLogin shouldBe false
            }
        }
    }

    test("well-formed backend URLs pass validation") {
        val validUrls = listOf(
            "https://echo.example.com",
            "HTTPS://Echo.Example.com/",
            "http://localhost:9090",
            "https://192.168.0.2:8443/api/v1",
            "http://[::1]:9090"
        )

        for (validUrl in validUrls) {
            isValidBackendUrl(validUrl) shouldBe true
        }
    }

    test("a login success is delivered even when nobody collects at that moment") {
        runTest(testDispatcher) {
            val loginViewModel = LoginViewModel(FakeAuthRepository())

            loginViewModel.onBackendUrlChanged("https://echo.example.com")
            loginViewModel.onUsernameChanged("alex")
            loginViewModel.onPasswordChanged("secret")
            loginViewModel.onLoginClick()
            advanceUntilIdle()

            loginViewModel.loginUiState.value.authError.shouldBeNull()
            loginViewModel.loginSuccess.first() shouldBe Unit
        }
    }
})