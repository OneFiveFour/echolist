package net.onefivefour.echolist.feature.tasklist.ui

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun TaskDescriptionField(
    state: TextFieldState,
    isDone: Boolean,
    modifier: Modifier = Modifier,
    onKeyboardAction: (() -> Unit)? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onFocusLost: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    var hasFocus by remember(state) { mutableStateOf(false) }

    BasicTextField(
        state = state,
        modifier = modifier
            .then(if (focusRequester == null) Modifier else Modifier.focusRequester(focusRequester))
            .onFocusChanged { focusState ->
                if (hasFocus && !focusState.isFocused) {
                    onFocusLost?.invoke()
                }
                onFocusChanged?.invoke(focusState.isFocused)
                hasFocus = focusState.isFocused
            },
        textStyle = EchoListTheme.typography.bodyLarge.copy(
            color = EchoListTheme.materialColors.onSurface,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
        ),
        cursorBrush = SolidColor(EchoListTheme.materialColors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        onKeyboardAction = onKeyboardAction?.let { keyboardAction ->
            { keyboardAction() }
        }
    )
}

@PreviewComponentLightDark
@Composable
private fun TaskDescriptionFieldPreview() {
    EchoListTheme(modifier = Modifier) {
        TaskDescriptionField(
            state = rememberTextFieldState(
                initialText = "Review meeting notes",
            ),
            isDone = false,
        )
    }
}
