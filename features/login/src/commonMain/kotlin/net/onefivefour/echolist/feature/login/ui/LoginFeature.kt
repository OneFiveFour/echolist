package net.onefivefour.echolist.feature.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginFeature(onAuthenticated: () -> Unit) {

    val loginViewModel = koinViewModel<LoginViewModel>()
    val loginState by loginViewModel.loginUiState.collectAsStateWithLifecycle()

    LaunchedEffect(loginViewModel) {
        loginViewModel.loginSuccess.collect {
            onAuthenticated()
        }
    }

    LoginScreen(
        loginUiState = loginState,
        onBackendUrlChange = loginViewModel::onBackendUrlChanged,
        onUsernameChange = loginViewModel::onUsernameChanged,
        onPasswordChange = loginViewModel::onPasswordChanged,
        onLoginClick = loginViewModel::onLoginClick
    )
}