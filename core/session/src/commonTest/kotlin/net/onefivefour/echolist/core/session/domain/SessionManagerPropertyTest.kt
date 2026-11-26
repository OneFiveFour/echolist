package net.onefivefour.echolist.core.session.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.fixtures.FakeAuthRepository

/**
 * Property 11: AuthState reflects storage
 *
 * *For any* SecureStorage state, AuthState should be Authenticated
 * if and only if SecureStorage.get(ACCESS_TOKEN) returns a non-null value;
 * otherwise it should be Unauthenticated.
 *
 * **Validates: Requirements 6.1, 6.2**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()


    test("Property 11: AuthState is Authenticated when access token is present") {
        checkAll(PropTestConfig(iterations = 20), Arb.string(1..100)) { token ->
            runTest(testDispatcher) {
                val storage = FakeSecureStorage()
                storage.put(StorageKeys.ACCESS_TOKEN, token)
                val authEventBus = AuthEventBus()
                val scope = CoroutineScope(testDispatcher)

                val sessionManager = SessionManager(FakeAuthRepository(storage), authEventBus, scope)

                sessionManager.authState.value shouldBe AuthState.Authenticated
                scope.cancel()
            }
        }
    }

    test("Property 11: AuthState is Unauthenticated when no access token") {
        checkAll(PropTestConfig(iterations = 20), Arb.boolean()) { _ ->
            runTest(testDispatcher) {
                val storage = FakeSecureStorage()
                // No access token stored
                val authEventBus = AuthEventBus()
                val scope = CoroutineScope(testDispatcher)

                val sessionManager = SessionManager(FakeAuthRepository(storage), authEventBus, scope)

                sessionManager.authState.value shouldBe AuthState.Unauthenticated
                scope.cancel()
            }
        }
    }

    test("Property 11: ReAuthRequired transitions to Unauthenticated") {
        checkAll(PropTestConfig(iterations = 20), Arb.string(1..100)) { token ->
            runTest(testDispatcher) {
                val storage = FakeSecureStorage()
                storage.put(StorageKeys.ACCESS_TOKEN, token)
                val authEventBus = AuthEventBus()
                val scope = CoroutineScope(testDispatcher)

                val sessionManager = SessionManager(FakeAuthRepository(storage), authEventBus, scope)
                // Let the scope coroutine start collecting
                testScheduler.advanceUntilIdle()
                sessionManager.authState.value shouldBe AuthState.Authenticated

                authEventBus.emit(AuthEvent.ReAuthRequired)
                testScheduler.advanceUntilIdle()

                sessionManager.authState.value shouldBe AuthState.Unauthenticated
                scope.cancel()
            }
        }
    }
})