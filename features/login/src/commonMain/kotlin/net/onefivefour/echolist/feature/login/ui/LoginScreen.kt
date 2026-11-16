package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.window.core.layout.WindowSizeClass
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
internal fun LoginScreen(
    uiState: LoginUiState,
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
    // Kept above the adaptive panes so a resize never resets password visibility or field focus.
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val usernameFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val fieldErrors = listOfNotNull(uiState.backendUrlError, uiState.usernameError, uiState.passwordError)

    Row(modifier.fillMaxSize().imePadding()) {
        if (isDesktop) {
            LoginIntroduction(Modifier.fillMaxWidth(0.44f).fillMaxHeight())
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
            val horizontalPadding = if (isDesktop) (maxWidth * 0.125f).coerceAtMost(100.8.dp) else 20.dp
            val topPadding = if (isDesktop) 36.dp else 22.dp
            // Keep the action anchored independently of errors and loading.
            val bottomPadding = if (isDesktop) 36.dp else 56.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .heightIn(min = maxHeight)
                    .padding(start = horizontalPadding, end = horizontalPadding, top = topPadding, bottom = bottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isDesktop) Arrangement.Center else Arrangement.Bottom
            ) {
                // One form at one composition location in both modes: input state survives resizing.
                LoginForm(
                    uiState, isDesktop, isPasswordVisible, { isPasswordVisible = !isPasswordVisible },
                    usernameFocus, passwordFocus, fieldErrors,
                    onBackendUrlChange, onUsernameChange, onPasswordChange, onLoginClick,
                    Modifier.widthIn(max = 448.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    isDesktop: Boolean,
    isPasswordVisible: Boolean,
    onTogglePassword: () -> Unit,
    usernameFocus: FocusRequester,
    passwordFocus: FocusRequester,
    fieldErrors: List<String>,
    onBackendUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier
) {
    val colors = EchoListTheme.materialColors
    val scale = EchoListTheme.typeScale
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val errorMessages = fieldErrors + listOfNotNull(uiState.authError?.let { authErrorMessage(it) })
    val errorSlotHeight = 96.dp * LocalDensity.current.fontScale
    Column(modifier.padding(bottom = 20.dp).semantics { testTag = "login-form" }) {
        Text(stringResource(Res.string.login_welcome), Modifier.semantics { testTag = "login-welcome" }, style = scale.label, color = colors.primary)
        Spacer(Modifier.height(EchoListTheme.dimensions.s))
        Text(
            stringResource(Res.string.login_heading),
            modifier = Modifier.semantics { testTag = "login-heading" },
            style = scale.titleLarge.copy(
                fontSize = if (isDesktop) 38.sp else 31.sp,
                lineHeight = if (isDesktop) 44.sp else 37.sp,
                letterSpacing = if (isDesktop) (-1.71).sp else (-1.395).sp
            ),
            color = colors.onBackground
        )
        // The reserved slot is present in every state. Its text scales, and long summaries scroll
        // internally, so neither the fields nor Connect move as messages appear or disappear.
        Box(
            Modifier.fillMaxWidth().height(errorSlotHeight).padding(vertical = EchoListTheme.dimensions.m)
                .semantics { testTag = "login-error-slot" },
            contentAlignment = Alignment.BottomStart
        ) {
            if (errorMessages.isNotEmpty()) {
                LoginErrorSummary(
                    errorMessages,
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                        .semantics { liveRegion = LiveRegionMode.Polite; testTag = "login-errors" }
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(if (isDesktop) 19.dp else 16.dp)) {
            ElFormTextField(
                value = uiState.backendUrl,
                label = stringResource(Res.string.login_backend_url_label),
                placeholder = stringResource(Res.string.login_server_placeholder),
                onValueChange = onBackendUrlChange,
                errorMessage = uiState.backendUrlError,
                minHeight = if (isDesktop) 54.dp else 52.dp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { usernameFocus.requestFocus() }),
                leadingIcon = { LoginFieldIcon(Res.drawable.login_server) },
                modifier = Modifier.semantics { testTag = "login-server" }
            )
            ElFormTextField(
                value = uiState.username,
                label = stringResource(Res.string.login_username_label),
                placeholder = stringResource(Res.string.login_username_placeholder),
                onValueChange = onUsernameChange,
                errorMessage = uiState.usernameError,
                minHeight = if (isDesktop) 54.dp else 52.dp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                leadingIcon = { LoginFieldIcon(Res.drawable.login_username) },
                modifier = Modifier.focusRequester(usernameFocus).semantics { testTag = "login-username" }
            )
            ElFormTextField(
                value = uiState.password,
                label = stringResource(Res.string.login_password_label),
                placeholder = stringResource(Res.string.login_password_placeholder),
                onValueChange = onPasswordChange,
                errorMessage = uiState.passwordError,
                minHeight = if (isDesktop) 54.dp else 52.dp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide(); onLoginClick() }),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { LoginFieldIcon(Res.drawable.login_password) },
                trailingIcon = {
                    val description = stringResource(if (isPasswordVisible) Res.string.hide_password else Res.string.show_password)
                    Box(
                        Modifier.size(36.dp).clickable(onClickLabel = description, onClick = onTogglePassword)
                            .semantics { contentDescription = description; testTag = "login-visibility" },
                        contentAlignment = Alignment.Center
                    ) {
                        // The reference eye is preserved; the existing eye-off asset represents the extra state.
                        if (isPasswordVisible) {
                            Icon(painterResource(Res.drawable.visibility_off), null, Modifier.size(19.dp), tint = colors.onSurfaceVariant)
                        } else {
                            LoginFieldIcon(Res.drawable.login_visibility, colors.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier.focusRequester(passwordFocus).semantics { testTag = "login-password" }
            )
        }
        Spacer(Modifier.height(if (isDesktop) 26.dp else 22.dp))
        ElButton(
            onClick = onLoginClick,
            isEnabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(if (isDesktop) 56.dp else 58.dp)
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.login_button),
                    style = scale.label.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp)
                )
                Box(
                    Modifier.size(34.dp).background(colors.onPrimary.copy(alpha = 0.13f), EchoListTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(19.dp), color = colors.onPrimary,
                            strokeWidth = 2.dp, trackColor = colors.onPrimary.copy(alpha = 0.13f)
                        )
                    } else {
                        Text("→", style = scale.body.copy(fontSize = 20.sp, lineHeight = 20.sp, letterSpacing = 0.sp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginFieldIcon(
    resource: org.jetbrains.compose.resources.DrawableResource,
    tint: androidx.compose.ui.graphics.Color = EchoListTheme.materialColors.outline
) {
    Icon(painterResource(resource), null, Modifier.size(19.dp), tint = tint)
}
