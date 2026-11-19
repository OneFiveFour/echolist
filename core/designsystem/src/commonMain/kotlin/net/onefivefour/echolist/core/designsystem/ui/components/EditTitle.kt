package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
fun EditTitle(
    textFieldState: TextFieldState,
    requestFocus: Boolean = false,
    onNext: () -> Unit = {},
    onFocusLost: (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    var wasFocused by remember(textFieldState) { mutableStateOf(false) }

    if (requestFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        modifier = Modifier
            .padding(EchoListTheme.dimensions.m)
            .focusRequester(focusRequester)
            .then(
                if (onFocusLost == null) {
                    Modifier
                } else {
                    Modifier.onFocusChanged { focusState ->
                        if (wasFocused && !focusState.isFocused) {
                            onFocusLost()
                        }
                        wasFocused = focusState.isFocused
                    }
                }
            ),
        state = textFieldState,
        textStyle = EchoListTheme.typography.titleLarge.copy(
            color = EchoListTheme.materialColors.onSurface
        ),
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        onKeyboardAction = { onNext() }
    )
}

@PreviewComponentLightDark
@Composable
private fun EditTitlePreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        EditTitle(
            textFieldState = rememberTextFieldState(
                initialText = "Release checklist",
            ),
        )
    }
}
