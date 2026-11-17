package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.components.ElButton
import net.onefivefour.echolist.core.designsystem.ui.components.ElFormTextField
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.hide_password
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
    val echoListTypography = EchoListTheme.echoListTypography
    val keyboard = LocalSoftwareKeyboardController.current

    val errorMessages = listOfNotNull(
        loginUiState.backendUrlError,
        loginUiState.usernameError,
        loginUiState.passwordError,
        loginUiState.authError?.let { authErrorMessage(it) }
    )

    val errorSlotHeight = 96.dp * LocalDensity.current.fontScale

    Column(
        modifier
            .padding(bottom = 20.dp)
            .semantics { testTag = "login-form" }
    ) {

        Text(
            stringResource(Res.string.login_welcome),
            Modifier.semantics { testTag = "login-welcome" },
            style = echoListTypography.label,
            color = colors.primary
        )

        Spacer(Modifier.height(EchoListTheme.dimensions.s))

        Text(
            stringResource(Res.string.login_heading),
            modifier = Modifier.semantics { testTag = "login-heading" },
            style = echoListTypography.titleLarge.copy(
                fontSize = if (isDesktop) 38.sp else 31.sp,
                lineHeight = if (isDesktop) 44.sp else 37.sp,
                letterSpacing = if (isDesktop) (-1.71).sp else (-1.395).sp
            ),
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

        val inputFieldSpacing = when {
            isDesktop -> 19.dp
            else -> 16.dp
        }

        val minInputFieldHeight = when {
            isDesktop -> 54.dp
            else -> 52.dp
        }

        val buttonHeight = when {
            isDesktop -> 56.dp
            else -> 58.dp
        }

        Column(verticalArrangement = Arrangement.spacedBy(inputFieldSpacing)) {

            ElFormTextField(
                value = loginUiState.backendUrl,
                label = stringResource(Res.string.login_backend_url_label),
                placeholder = stringResource(Res.string.login_server_placeholder),
                onValueChange = onBackendUrlChange,
                errorMessage = loginUiState.backendUrlError,
                minHeight = minInputFieldHeight,
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
                minHeight = minInputFieldHeight,
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
                minHeight = minInputFieldHeight,
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
                            .size(36.dp)
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
                                modifier = Modifier.size(19.dp),
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

        val spacerHeight = when {
            isDesktop -> 26.dp
            else -> 22.dp
        }
        Spacer(Modifier.height(spacerHeight))

        ElButton(
            onClick = onLoginClick,
            isEnabled = !loginUiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .shadow(12.dp, EchoListTheme.shapes.extraLarge)
                .semantics { testTag = "login-connect" },
            shape = EchoListTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.primary,
                disabledContentColor = colors.onPrimary
            ),
            contentPadding = PaddingValues(start = 21.dp, end = 14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.login_button),
                    style = echoListTypography.label.copy(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.sp
                    )
                )
                Box(
                    Modifier.size(34.dp).background(
                        colors.onPrimary.copy(alpha = 0.13f),
                        EchoListTheme.shapes.medium
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (loginUiState.isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(19.dp), color = colors.onPrimary,
                            strokeWidth = 2.dp, trackColor = colors.onPrimary.copy(alpha = 0.13f)
                        )
                    } else {
                        Text(
                            "→",
                            style = echoListTypography.body.copy(
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.sp
                            )
                        )
                    }
                }
            }
        }
    }
}