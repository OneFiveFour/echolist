package net.onefivefour.echolist.core.designsystem.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
fun ElTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    style: TextStyle = EchoListTheme.typography.bodyMedium,
    color: Color = EchoListTheme.materialColors.onSurface,
    imeAction: ImeAction = ImeAction.Default,
    onKeyboardAction: (() -> Unit)? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onFocusLost: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    var wasFocused by remember(state) { mutableStateOf(false) }

    BasicTextField(
        state = state,
        modifier = modifier
            .then(if (focusRequester == null) Modifier else Modifier.focusRequester(focusRequester))
            .then(
                if (onFocusLost == null) {
                    if (onFocusChanged == null) {
                        Modifier
                    } else {
                        Modifier.onFocusChanged { focusState ->
                            onFocusChanged(focusState.isFocused)
                        }
                    }
                } else {
                    Modifier.onFocusChanged { focusState ->
                        if (wasFocused && !focusState.isFocused) {
                            onFocusLost()
                        }
                        onFocusChanged?.invoke(focusState.isFocused)
                        wasFocused = focusState.isFocused
                    }
                }
            ),
        textStyle = style.copy(color = color),
        cursorBrush = SolidColor(EchoListTheme.materialColors.primary),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        onKeyboardAction = onKeyboardAction?.let { keyboardAction ->
            { keyboardAction() }
        }
    )
}

@PreviewComponentLightDark
@Composable
private fun ElTextFieldPreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        ElTextField(
            state = rememberTextFieldState(
                initialText = "Meeting notes",
            ),
        )
    }
}
