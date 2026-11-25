package net.onefivefour.echolist.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.error_backend_url_invalid
import net.onefivefour.echolist.feature.login.resources.error_backend_url_required
import net.onefivefour.echolist.feature.login.resources.error_password_required
import net.onefivefour.echolist.feature.login.resources.error_username_required
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.AuthError
import net.onefivefour.echolist.core.session.domain.AuthFailure
import org.jetbrains.compose.resources.getString

/** Scheme, host (name, IPv4 or bracketed IPv6), optional port and optional path. */
private val backendUrlPattern = Regex(
    pattern = """^https?://(\[[0-9a-f:.]+]|[^\s/?#:@\[\]]+)(:\d{1,5})?(/\S*)?$""",
    option = RegexOption.IGNORE_CASE
)

internal fun isValidBackendUrl(url: String): Boolean = backendUrlPattern.matches(url)

internal class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val loginUiState: StateFlow<LoginUiState>
        field = MutableStateFlow(LoginUiState())

    // Buffered so a success is still delivered if the UI collector is restarting at that moment.
    private val loginSuccessEvents = Channel<Unit>(Channel.BUFFERED)
    val loginSuccess: Flow<Unit> = loginSuccessEvents.receiveAsFlow()

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
            val backendUrl = current.backendUrl.trim()
            val backendUrlError = when {
                backendUrl.isEmpty() -> getString(Res.string.error_backend_url_required)
                !isValidBackendUrl(backendUrl) -> getString(Res.string.error_backend_url_invalid)
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

            // The password is sent exactly as entered; leading or trailing spaces may be part of it.
            val result = authRepository.login(
                baseUrl = backendUrl,
                username = current.username.trim(),
                password = current.password
            )

            result.fold(
                onSuccess = {
                    loginUiState.update { it.copy(isLoading = false) }
                    loginSuccessEvents.send(Unit)
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