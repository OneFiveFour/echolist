package net.onefivefour.echolist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.core.session.data.storage.FakeSecureStorage
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.SessionDataCleaner
import net.onefivefour.echolist.core.session.domain.StorageKeys
import net.onefivefour.echolist.ui.fixtures.FakeAuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelLogoutTest : FunSpec({

    val dispatcher = StandardTestDispatcher()
    beforeTest { Dispatchers.setMain(dispatcher) }
    afterTest { Dispatchers.resetMain() }

    fun signedInStorage() = FakeSecureStorage().apply { put(StorageKeys.ACCESS_TOKEN, "access") }

    class LogoutRepository(storage: FakeSecureStorage) : FakeAuthRepository(storage) {
        var logoutResult: Result<Unit> = Result.success(Unit)
        var logoutCalls = 0
        val externalLogouts = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        override suspend fun logout(): Result<Unit> {
            logoutCalls++
            return logoutResult.also { if (it.isSuccess) clearAuth() }
        }

        override fun observeExternalLogout() = externalLogouts
    }

    test("successful logout resets session data and shows the login") {
        var cleared = 0
        val repository = LogoutRepository(signedInStorage())
        val vm = AuthViewModel(repository, AuthEventBus(), listOf(SessionDataCleaner { cleared++ }))
        dispatcher.scheduler.advanceUntilIdle()

        vm.logout()
        dispatcher.scheduler.advanceUntilIdle()

        vm.authState.value shouldBe AuthState.Unauthenticated
        vm.logoutState.value shouldBe LogoutState.Idle
        cleared shouldBe 1
    }

    test("a failed logout stays signed in, keeps data and can be retried") {
        var cleared = 0
        val repository = LogoutRepository(signedInStorage())
        repository.logoutResult = Result.failure(Exception("offline"))
        val vm = AuthViewModel(repository, AuthEventBus(), listOf(SessionDataCleaner { cleared++ }))
        dispatcher.scheduler.advanceUntilIdle()

        vm.logout()
        dispatcher.scheduler.advanceUntilIdle()

        vm.authState.value shouldBe AuthState.Authenticated
        vm.logoutState.value shouldBe LogoutState.Failed
        cleared shouldBe 0

        repository.logoutResult = Result.success(Unit)
        vm.logout()
        dispatcher.scheduler.advanceUntilIdle()

        vm.authState.value shouldBe AuthState.Unauthenticated
        repository.logoutCalls shouldBe 2
        cleared shouldBe 1
    }

    test("a sign-out from another tab logs this one out") {
        val repository = LogoutRepository(signedInStorage())
        val vm = AuthViewModel(repository, AuthEventBus())
        dispatcher.scheduler.advanceUntilIdle()

        repository.externalLogouts.tryEmit(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        vm.authState.value shouldBe AuthState.Unauthenticated
    }

    test("a failing cleaner does not keep the user signed in") {
        val repository = LogoutRepository(signedInStorage())
        val vm = AuthViewModel(repository, AuthEventBus(), listOf(SessionDataCleaner { error("db") }))
        dispatcher.scheduler.advanceUntilIdle()

        vm.logout()
        dispatcher.scheduler.advanceUntilIdle()

        vm.authState.value shouldBe AuthState.Unauthenticated
    }
})