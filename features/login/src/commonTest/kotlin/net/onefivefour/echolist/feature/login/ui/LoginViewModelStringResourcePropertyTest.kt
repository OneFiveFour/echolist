package net.onefivefour.echolist.feature.login.ui

import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.error_backend_url_invalid
import net.onefivefour.echolist.feature.login.resources.error_backend_url_required
import net.onefivefour.echolist.feature.login.resources.error_password_required
import net.onefivefour.echolist.feature.login.resources.error_username_required
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.getString

/**
 * Feature: string-resource-extraction, Property 2: Validation errors use resource strings
 *
 * Property 2: Validation errors use resource strings for all input combinations
 *
 * *For any* combination of backend URL, username, and password strings (where each may be blank or non-blank),
 * when `LoginViewModel.onLoginClick()` is called, every non-null error field in the resulting `LoginUiState`
 * should exactly match the corresponding localized resource string (`error_backend_url_required`, `error_backend_url_invalid`,
 * `error_username_required`, `error_password_required`), and no error field should contain a hardcoded
 * English literal that differs from the resource value.
 *
 * **Validates: Requirements 3.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelStringResourcePropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    test("Property 2: Validation errors use resource strings for all input combinations") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..20),
            Arb.string(0..20),
            Arb.string(0..20)
        ) { backendUrl, username, password ->
            runTest(testDispatcher) {
                // Resolve the expected resource strings
                val expectedBackendUrlError = getString(Res.string.error_backend_url_required)
                val expectedInvalidBackendUrlError = getString(Res.string.error_backend_url_invalid)
                val expectedUsernameError = getString(Res.string.error_username_required)
                val expectedPasswordError = getString(Res.string.error_password_required)

                // Create ViewModel and set input values
                val vm = LoginViewModel(FakeAuthRepository())
                vm.onBackendUrlChanged(backendUrl)
                vm.onUsernameChanged(username)
                vm.onPasswordChanged(password)
                vm.onLoginClick()
                advanceUntilIdle()

                val uiState = vm.loginUiState.value

                // Assert that each non-null error field matches the corresponding resource string
                if (uiState.backendUrlError != null) {
                    uiState.backendUrlError shouldBe when {
                        backendUrl.isBlank() -> expectedBackendUrlError
                        else -> expectedInvalidBackendUrlError
                    }
                }

                if (uiState.usernameError != null) {
                    uiState.usernameError shouldBe expectedUsernameError
                }

                if (uiState.passwordError != null) {
                    uiState.passwordError shouldBe expectedPasswordError
                }

                // Verify that errors are set correctly based on blank/non-blank inputs
                if (backendUrl.isBlank()) {
                    uiState.backendUrlError shouldBe expectedBackendUrlError
                }

                if (username.isBlank()) {
                    uiState.usernameError shouldBe expectedUsernameError
                }

                if (password.isBlank()) {
                    uiState.passwordError shouldBe expectedPasswordError
                }
            }
        }
    }
})