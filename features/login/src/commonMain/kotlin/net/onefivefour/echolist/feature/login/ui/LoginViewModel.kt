package net.onefivefour.echolist.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.error_backend_url_required
import net.onefivefour.echolist.feature.login.resources.error_password_required
import net.onefivefour.echolist.feature.login.resources.error_username_required
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.AuthError
import net.onefivefour.echolist.core.session.domain.AuthFailure
import org.jetbrains.compose.resources.getString

internal class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val loginUiState: StateFlow<LoginUiState>
        field = MutableStateFlow(LoginUiState())

    val loginSuccess: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

    init {
        val storedUrl = authRepository.getBaseUrl()
        if (storedUrl != null) {
            loginUiState.update { it.copy(backendUrl = storedUrl) }
        }
    }

    fun onBackendUrlChanged(value: String) {
        loginUiState.update { it.copy(
            backendUrl = value,
            backendUrlError = null,
            authError = null
        ) }
    }

    fun onUsernameChanged(value: String) {
        loginUiState.update { it.copy(
            username = value,
            usernameError = null,
            authError = null
        ) }
    }

    fun onPasswordChanged(value: String) {
        loginUiState.update { it.copy(
            password = value,
            passwordError = null,
            authError = null
        ) }
    }

    fun onLoginClick() {
        val current = loginUiState.value

        if (current.isLoading) {
            return
        }

        loginUiState.update { it.copy(
            isLoading = true,
            authError = null
        ) }

        viewModelScope.launch {
            val backendUrlError = when {
                current.backendUrl.isBlank() -> getString(Res.string.error_backend_url_required)
                else -> null
            }
            val usernameError = when {
                current.username.isBlank() -> getString(Res.string.error_username_required)
                else -> null
            }
            val passwordError = when {
                current.password.isBlank() -> getString(Res.string.error_password_required)
                else -> null
            }

            if (backendUrlError != null || usernameError != null || passwordError != null) {
                loginUiState.update {
                    it.copy(
                        isLoading = false,
                        backendUrlError = backendUrlError,
                        usernameError = usernameError,
                        passwordError = passwordError
                    )
                }
                return@launch
            }

            val result = authRepository.login(
                baseUrl = current.backendUrl.trim(),
                username = current.username.trim(),
                password = current.password.trim()
            )

            result.fold(
                onSuccess = {
                    loginUiState.update { it.copy(isLoading = false) }
                    loginSuccess.emit(Unit)
                },
                onFailure = { throwable ->
                    val authError = (throwable as? AuthFailure)?.error
                        ?: AuthError.Unknown(throwable.message ?: "Unknown error occurred")

                    loginUiState.update {
                        it.copy(
                            isLoading = false,
                            authError = authError
                        )
                    }
                }
            )
        }
    }
}