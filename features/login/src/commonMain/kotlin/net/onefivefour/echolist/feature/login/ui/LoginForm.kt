package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import net.onefivefour.echolist.core.designsystem.ui.components.ElButton
import net.onefivefour.echolist.core.designsystem.ui.components.ElFormTextField
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.error_backend_url_required
import net.onefivefour.echolist.feature.login.resources.error_password_required
import net.onefivefour.echolist.feature.login.resources.error_username_required
import net.onefivefour.echolist.feature.login.resources.hide_password
import net.onefivefour.echolist.feature.login.resources.login_arrow_forward
import net.onefivefour.echolist.feature.login.resources.login_backend_url_label
import net.onefivefour.echolist.feature.login.resources.login_button
import net.onefivefour.echolist.feature.login.resources.login_heading
import net.onefivefour.echolist.feature.login.resources.login_password
import net.onefivefour.echolist.feature.login.resources.login_password_label
import net.onefivefour.echolist.feature.login.resources.login_password_placeholder
import net.onefivefour.echolist.feature.login.resources.login_server
import net.onefivefour.echolist.feature.login.resources.login_server_placeholder
import net.onefivefour.echolist.feature.login.resources.login_username
import net.onefivefour.echolist.feature.login.resources.login_username_label
import net.onefivefour.echolist.feature.login.resources.login_username_placeholder
import net.onefivefour.echolist.feature.login.resources.login_visibility
import net.onefivefour.echolist.feature.login.resources.login_welcome
import net.onefivefour.echolist.feature.login.resources.show_password
import net.onefivefour.echolist.feature.login.resources.visibility_off
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginForm(
    loginUiState: LoginUiState,
    isDesktop: Boolean,
    isPasswordVisible: Boolean,
    usernameFocusRequester: FocusRequester,
    passwordFocusRequester: FocusRequester,
    onBackendUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier
) {

    val colors = EchoListTheme.materialColors
    val typography = EchoListTheme.typography
    val keyboard = LocalSoftwareKeyboardController.current

    val errorMessages = listOfNotNull(
        loginUiState.backendUrlError,
        loginUiState.usernameError,
        loginUiState.passwordError,
        loginUiState.authError?.let { authErrorMessage(it) }
    )

    val errorSlotHeight = (EchoListTheme.dimensions.xxxl * 2) * LocalDensity.current.fontScale

    Column(
        modifier
            .padding(bottom = EchoListTheme.dimensions.xl)
            .semantics { testTag = "login-form" }
    ) {

        Text(
            stringResource(Res.string.login_welcome),
            Modifier.semantics { testTag = "login-welcome" },
            style = typography.labelMedium,
            color = colors.primary
        )

        Spacer(Modifier.height(EchoListTheme.dimensions.s))

        Text(
            stringResource(Res.string.login_heading),
            modifier = Modifier.semantics { testTag = "login-heading" },
            style = if (isDesktop) typography.headlineLarge else typography.headlineMedium,
            color = colors.onBackground
        )

        // The reserved slot is present in every state. Its text scales, and long summaries scroll
        // internally, so neither the fields nor Connect move as messages appear or disappear.
        Box(
            Modifier
                .fillMaxWidth()
                .height(errorSlotHeight)
                .padding(vertical = EchoListTheme.dimensions.m)
                .semantics { testTag = "login-error-slot" },
            contentAlignment = Alignment.BottomStart
        ) {
            if (errorMessages.isNotEmpty()) {
                LoginErrorSummary(
                    errorMessages,
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .semantics { liveRegion = LiveRegionMode.Polite; testTag = "login-errors" }
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.l)) {

            ElFormTextField(
                value = loginUiState.backendUrl,
                label = stringResource(Res.string.login_backend_url_label),
                placeholder = stringResource(Res.string.login_server_placeholder),
                onValueChange = onBackendUrlChange,
                errorMessage = loginUiState.backendUrlError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { usernameFocusRequester.requestFocus() }),
                leadingIcon = { LoginFieldIcon(Res.drawable.login_server) },
                modifier = Modifier.semantics { testTag = "login-server" }
            )

            ElFormTextField(
                value = loginUiState.username,
                label = stringResource(Res.string.login_username_label),
                placeholder = stringResource(Res.string.login_username_placeholder),
                onValueChange = onUsernameChange,
                errorMessage = loginUiState.usernameError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                leadingIcon = { LoginFieldIcon(Res.drawable.login_username) },
                modifier = Modifier.focusRequester(usernameFocusRequester)
                    .semantics { testTag = "login-username" }
            )

            ElFormTextField(
                value = loginUiState.password,
                label = stringResource(Res.string.login_password_label),
                placeholder = stringResource(Res.string.login_password_placeholder),
                onValueChange = onPasswordChange,
                errorMessage = loginUiState.passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide(); onLoginClick() }),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { LoginFieldIcon(Res.drawable.login_password) },
                trailingIcon = {
                    val description = stringResource(
                        when {
                            isPasswordVisible -> Res.string.hide_password
                            else -> Res.string.show_password
                        }
                    )

                    Box(
                        Modifier
                            .size(EchoListTheme.dimensions.iconSmall)
                            .clickable(onClickLabel = description, onClick = onTogglePassword)
                            .semantics {
                                contentDescription = description; testTag = "login-visibility"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPasswordVisible) {
                            Icon(
                                painter = painterResource(resource = Res.drawable.visibility_off),
                                contentDescription = null,
                                modifier = Modifier.size(EchoListTheme.dimensions.l),
                                tint = colors.onSurfaceVariant
                            )
                        } else {
                            LoginFieldIcon(
                                resource = Res.drawable.login_visibility,
                                tint = colors.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .focusRequester(passwordFocusRequester)
                    .semantics { testTag = "login-password" }
            )
        }

        Spacer(Modifier.height(EchoListTheme.dimensions.xl))

        ElButton(
            onClick = onLoginClick,
            isEnabled = !loginUiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "login-connect" }
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.login_button),
                    style = typography.labelLarge
                )
                Box(
                    Modifier.size(EchoListTheme.dimensions.xxl).background(
                        colors.onPrimary.copy(alpha = 0.13f),
                        EchoListTheme.shapes.medium
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (loginUiState.isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(EchoListTheme.dimensions.l), color = colors.onPrimary,
                            strokeWidth = EchoListTheme.dimensions.xxs, trackColor = colors.onPrimary.copy(alpha = 0.13f)
                        )
                    } else {
                        Icon(
                            painterResource(Res.drawable.login_arrow_forward),
                            contentDescription = null,
                            modifier = Modifier.size(EchoListTheme.dimensions.xl)
                        )
                    }
                }
            }
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun LoginFormPreview() {
    EchoListTheme(modifier = Modifier) {
        LoginForm(
            LoginUiState(backendUrl = "https://echo.example.com", username = "Alex", password = "password"),
            isDesktop = false,
            isPasswordVisible = false,
            usernameFocusRequester = remember { FocusRequester() },
            passwordFocusRequester = remember { FocusRequester() },
            onBackendUrlChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePassword = {},
            onLoginClick = {},
            modifier = Modifier,
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun LoginFormErrorPreview() {
    EchoListTheme(modifier = Modifier) {
        LoginForm(
            loginUiState = LoginUiState(
                backendUrl = "",
                backendUrlError = stringResource(Res.string.error_backend_url_required),
                usernameError = stringResource(Res.string.error_username_required),
                passwordError = stringResource(Res.string.error_password_required)
            ),
            isDesktop = false,
            isPasswordVisible = false,
            usernameFocusRequester = remember { FocusRequester() },
            passwordFocusRequester = remember { FocusRequester() },
            onBackendUrlChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePassword = {},
            onLoginClick = {},
            modifier = Modifier,
        )
    }
}
