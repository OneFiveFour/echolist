package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
fun ElOutlinedTextField(
    text: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = text,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        isError = isError,
        singleLine = true,
        shape = RoundedCornerShape(50),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = EchoListTheme.materialColors.surface,
            unfocusedContainerColor = EchoListTheme.materialColors.surface,
            disabledContainerColor = EchoListTheme.materialColors.surface,
            errorContainerColor = EchoListTheme.materialColors.surface,
            focusedBorderColor = EchoListTheme.materialColors.surfaceVariant,
            unfocusedBorderColor = EchoListTheme.materialColors.surfaceVariant
        ),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon
    )
}

@PreviewComponentLightDark
@Composable
private fun ElOutlinedTextFieldPreview() {
    EchoListTheme(modifier = Modifier) {
        ElOutlinedTextField(
            text = "Projects",
            label = "Folder name",
            onValueChange = {},
            isError = false,
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun ElOutlinedTextFieldErrorPreview() {
    EchoListTheme(modifier = Modifier) {
        ElOutlinedTextField(
            text = "",
            label = "Folder name",
            onValueChange = {},
            isError = true,
        )
    }
}
