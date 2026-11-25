package net.onefivefour.echolist.feature.login.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.StorageKeys

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()
    val validUrlArb = Arb.element(
        "https://echo.example.com",
        "http://localhost:9090",
        "https://192.168.0.2:8443/api/"
    )

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Property 1: Blank field validation rejects submission
     *
     * *For any* login form submission where at least one required field
     * (backend URL, username, or password) is a string composed entirely
     * of whitespace characters, the system should reject the submission
     * and set the corresponding field-level error message to a non-null value.
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    test("Property 1: Blank backend URL sets backendUrlError") {
        val whitespaceArb = Arb.string(0..20).filter { it.isBlank() }
        val nonBlankArb = Arb.string(1..20).filter { it.isNotBlank() }

        checkAll(PropTestConfig(iterations = 20), whitespaceArb, nonBlankArb, nonBlankArb) { blankUrl, user, pass ->
            runTest(testDispatcher) {
                val vm = LoginViewModel(FakeAuthRepository())
                vm.onBackendUrlChanged(blankUrl)
                vm.onUsernameChanged(user)
                vm.onPasswordChanged(pass)
                vm.onLoginClick()
                advanceUntilIdle()

                vm.loginUiState.value.backendUrlError.shouldNotBeNull()
                vm.loginUiState.value.isLoading shouldBe false
            }
        }
    }

    test("Property 1: Blank username sets usernameError") {
        val whitespaceArb = Arb.string(0..20).filter { it.isBlank() }
        val nonBlankArb = Arb.string(1..20).filter { it.isNotBlank() }

        checkAll(PropTestConfig(iterations = 20), validUrlArb, whitespaceArb, nonBlankArb) { url, blankUser, pass ->
            runTest(testDispatcher) {
                val vm = LoginViewModel(FakeAuthRepository())
                vm.onBackendUrlChanged(url)
                vm.onUsernameChanged(blankUser)
                vm.onPasswordChanged(pass)
                vm.onLoginClick()
                advanceUntilIdle()

                vm.loginUiState.value.usernameError.shouldNotBeNull()
                vm.loginUiState.value.isLoading shouldBe false
            }
        }
    }

    test("Property 1: Blank password sets passwordError") {
        val whitespaceArb = Arb.string(0..20).filter { it.isBlank() }
        val nonBlankArb = Arb.string(1..20).filter { it.isNotBlank() }

        checkAll(PropTestConfig(iterations = 20), validUrlArb, nonBlankArb, whitespaceArb) { url, user, blankPass ->
            runTest(testDispatcher) {
                val vm = LoginViewModel(FakeAuthRepository())
                vm.onBackendUrlChanged(url)
                vm.onUsernameChanged(user)
                vm.onPasswordChanged(blankPass)
                vm.onLoginClick()
                advanceUntilIdle()

                vm.loginUiState.value.passwordError.shouldNotBeNull()
                vm.loginUiState.value.isLoading shouldBe false
            }
        }
    }

    /**
     * Property 2: Backend URL prefill round-trip
     *
     * *For any* non-null string stored under the BACKEND_URL key in SecureStorage,
     * when LoginViewModel initializes, its UiState.backendUrl should equal that stored string.
     *
     * **Validates: Requirements 1.2, 7.2**
     */
    test("Property 2: Backend URL prefill round-trip") {
        checkAll(PropTestConfig(iterations = 20), Arb.string(1..100)) { url ->
            runTest(testDispatcher) {
                val storage = FakeSecureStorage()
                storage.put(StorageKeys.BACKEND_URL, url)

                val vm = LoginViewModel(FakeAuthRepository(storage))
                advanceUntilIdle()

                vm.loginUiState.value.backendUrl shouldBe url
            }
        }
    }

    /**
     * Property 3: Valid submission triggers loading state
     *
     * *For any* login form state where all three fields (backend URL, username, password)
     * are valid (a well-formed backend URL and non-blank credentials), calling onLoginClick should transition isLoading to true
     * and all field-level errors to null.
     *
     * **Validates: Requirements 1.6**
     */
    test("Property 3: Valid submission triggers loading state") {
        val nonBlankArb = Arb.string(1..50).filter { it.isNotBlank() }

        checkAll(PropTestConfig(iterations = 20), validUrlArb, nonBlankArb, nonBlankArb) { url, user, pass ->
            runTest(testDispatcher) {
                // Use a repo that never completes so we can observe the loading state
                val repo = object : FakeAuthRepository() {
                    override suspend fun login(baseUrl: String, username: String, password: String): Result<Unit> {
                        // Suspend indefinitely so isLoading stays true
                        kotlinx.coroutines.awaitCancellation()
                    }
                }

                val vm = LoginViewModel(repo)
                vm.onBackendUrlChanged(url)
                vm.onUsernameChanged(user)
                vm.onPasswordChanged(pass)
                vm.onLoginClick()
                // Don't advanceUntilIdle — we want to observe the loading state before login completes

                vm.loginUiState.value.isLoading shouldBe true
                vm.loginUiState.value.backendUrlError.shouldBeNull()
                vm.loginUiState.value.usernameError.shouldBeNull()
                vm.loginUiState.value.passwordError.shouldBeNull()
            }
        }
    }

    /**
     * Property 6: Login errors surface to UI
     *
     * *For any* error result returned by AuthRepository.login, the LoginViewModel's
     * UiState should have a non-null authError and isLoading set to false.
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 6: Login errors surface to UI") {
        val nonBlankArb = Arb.string(1..50).filter { it.isNotBlank() }

        checkAll(
            PropTestConfig(iterations = 20),
            validUrlArb,
            nonBlankArb,
            nonBlankArb,
            Arb.string(1..100)
        ) { url, user, pass, errorMsg ->
            runTest(testDispatcher) {
                val repo = FakeAuthRepository()
                repo.loginResult = Result.failure(RuntimeException(errorMsg))

                val vm = LoginViewModel(repo)
                vm.onBackendUrlChanged(url)
                vm.onUsernameChanged(user)
                vm.onPasswordChanged(pass)
                vm.onLoginClick()
                advanceUntilIdle()

                vm.loginUiState.value.authError.shouldNotBeNull()
                vm.loginUiState.value.isLoading shouldBe false
            }
        }
    }
})