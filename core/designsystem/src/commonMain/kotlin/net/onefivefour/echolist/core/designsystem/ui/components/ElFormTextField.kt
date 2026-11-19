package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
fun ElFormTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val colors = EchoListTheme.materialColors
    val dimensions = EchoListTheme.dimensions
    val interaction = remember { MutableInteractionSource() }
    val isFocused by interaction.collectIsFocusedAsState()
    val textStyle = EchoListTheme.typography.bodyMedium

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.s)) {

        Text(
            label,
            style = EchoListTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = label
                    if (errorMessage != null) error(errorMessage)
                },
            textStyle = textStyle.copy(color = colors.onSurface),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            interactionSource = interaction,
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimensions.formFieldMinHeight)
                        .background(colors.surface, EchoListTheme.shapes.large)
                        .border(
                            dimensions.borderWidth,
                            when {
                                errorMessage != null -> colors.error
                                isFocused -> colors.primary
                                else -> colors.outlineVariant
                            },
                            EchoListTheme.shapes.large
                        )
                        .padding(horizontal = dimensions.l, vertical = dimensions.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (leadingIcon != null) {
                        Box(Modifier.width(dimensions.formFieldLeadingIconWidth)) { leadingIcon() }
                        Box(Modifier.width(dimensions.m))
                    }

                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle,
                                color = colors.onSurfaceVariant.copy(alpha = 0.68f)
                            )
                        }
                        innerTextField()
                    }

                    if (trailingIcon != null) {
                        Box(Modifier.width(dimensions.m))
                        trailingIcon()
                    }
                }
            }
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun ElFormTextFieldPreview() {
    EchoListTheme(modifier = Modifier) {
        ElFormTextField(
            value = "Alex",
            label = "Username",
            onValueChange = {},
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun ElFormTextFieldPlaceholderPreview() {
    EchoListTheme(modifier = Modifier) {
        ElFormTextField(
            value = "",
            label = "Username",
            onValueChange = {},
            placeholder = "Enter your username",
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun ElFormTextFieldErrorPreview() {
    EchoListTheme(modifier = Modifier) {
        ElFormTextField(
            value = "",
            label = "Username",
            onValueChange = {},
            placeholder = "Enter your username",
            errorMessage = "Username is required",
        )
    }
}
