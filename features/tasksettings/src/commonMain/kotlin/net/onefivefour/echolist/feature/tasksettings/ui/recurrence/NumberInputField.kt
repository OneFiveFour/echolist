package net.onefivefour.echolist.feature.tasksettings.ui.recurrence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.components.EchoListBackground
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/**
 * A compact, bordered numeric input box used inside the recurrence detail rows
 * (e.g. "every N weeks"). It keeps the existing free-text numeric entry behaviour
 * but presents it as a clearly tappable, well-sized field.
 */
@Composable
internal fun NumberInputField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val textStyle = EchoListTheme.typography.bodyMedium.copy(
        color = EchoListTheme.materialColors.onSurface,
        textAlign = TextAlign.Center
    )

    Surface(
        modifier = modifier,
        shape = EchoListTheme.shapes.small,
        color = EchoListTheme.materialColors.surfaceVariant,
        contentColor = EchoListTheme.materialColors.onSurface,
        border = BorderStroke(
            width = EchoListTheme.dimensions.borderWidth,
            color = if (isError) {
                EchoListTheme.materialColors.error
            } else {
                EchoListTheme.materialColors.surfaceVariant
            }
        )
    ) {
        BasicTextField(
            value = value?.toString().orEmpty(),
            onValueChange = { newValue ->
                when {
                    newValue.isEmpty() -> onValueChange(null)
                    isEditableNumberInput(newValue) -> newValue.toIntOrNull()?.let(onValueChange)
                }
            },
            textStyle = textStyle,
            cursorBrush = SolidColor(EchoListTheme.materialColors.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .widthIn(min = EchoListTheme.dimensions.xl)
                        .padding(
                            horizontal = EchoListTheme.dimensions.m,
                            vertical = EchoListTheme.dimensions.s
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
    }
}

@Preview
@Composable
private fun NumberInputFieldPreview() {
    EchoListTheme {
        EchoListBackground {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NumberInputField(
                    value = 3,
                    onValueChange = {},
                    isError = false
                )
            }
        }
    }
}