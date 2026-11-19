package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewDesktopLightDark
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewPhoneLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun LoginScreen(
    loginUiState: LoginUiState,
    onBackendUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDesktop = EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val scrollState = rememberScrollState()
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    LoginLayout(isDesktop, scrollState, modifier) {
        LoginForm(
            loginUiState = loginUiState,
            isDesktop = isDesktop,
            isPasswordVisible = isPasswordVisible,
            usernameFocusRequester = usernameFocusRequester,
            passwordFocusRequester = passwordFocusRequester,
            onBackendUrlChange = onBackendUrlChange,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onTogglePassword = { isPasswordVisible = !isPasswordVisible },
            onLoginClick = onLoginClick,
            modifier = Modifier.widthIn(max = 448.dp).fillMaxWidth()
        )
    }
}

@PreviewPhoneLightDark
@Composable
private fun LoginScreenPhonePreview() {
    EchoListTheme {
        LoginScreen(LoginUiState(), {}, {}, {}, {})
    }
}

@PreviewDesktopLightDark
@Composable
private fun LoginScreenDesktopPreview() {
    EchoListTheme {
        LoginScreen(LoginUiState(), {}, {}, {}, {})
    }
}
