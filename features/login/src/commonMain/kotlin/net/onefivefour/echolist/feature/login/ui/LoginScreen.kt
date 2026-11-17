package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
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

    // The editorial sidebar needs Expanded width. Compact and Medium share the form-only layout.
    // Future screens choose their own canonical layout from the same official adaptive information.
    val isDesktop = EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    val scrollState = rememberScrollState()

    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    Row(modifier.fillMaxSize().imePadding()) {

        if (isDesktop) {
            DesktopHeroBanner(Modifier.fillMaxWidth(0.44f).fillMaxHeight())
        }

        BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {

            val horizontalPadding = when {
                isDesktop -> (maxWidth * 0.125f).coerceAtMost(100.8.dp)
                else -> 20.dp
            }

            val topPadding = when {
                isDesktop -> 36.dp
                else -> 22.dp
            }

            val bottomPadding = when {
                isDesktop -> 36.dp
                else -> 56.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .heightIn(min = maxHeight)
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = topPadding,
                        bottom = bottomPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = when {
                    isDesktop -> Arrangement.Center
                    else -> Arrangement.Bottom
                }
            ) {
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
    }
}

