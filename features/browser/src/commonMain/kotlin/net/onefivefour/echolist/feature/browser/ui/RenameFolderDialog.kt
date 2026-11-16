package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.components.ElButton
import net.onefivefour.echolist.core.designsystem.ui.components.EchoListBackground
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun RenameFolderDialog(
    uiState: RenameFolderUiState,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!uiState.isVisible) return

    val focusRequester = remember { FocusRequester() }
    val dimensions = EchoListTheme.dimensions

    var textFieldValue by remember(uiState.folderName) {
        mutableStateOf(
            TextFieldValue(
                text = uiState.folderName,
                selection = TextRange(uiState.folderName.length)
            )
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EchoListTheme.echoListColorScheme.folderColor,
        titleContentColor = EchoListTheme.materialColors.onSurface,
        shape = EchoListTheme.shapes.medium,
        title = {
            Text(
                text = "Rename Folder",
                style = EchoListTheme.typography.titleSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onNameChange(newValue.text)
                    },
                    label = { Text(text = "Folder name") },
                    isError = uiState.error != null,
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = EchoListTheme.materialColors.surface,
                        unfocusedContainerColor = EchoListTheme.materialColors.surface,
                        disabledContainerColor = EchoListTheme.materialColors.surface,
                        errorContainerColor = EchoListTheme.materialColors.surface,
                        focusedBorderColor = EchoListTheme.materialColors.surfaceVariant,
                        unfocusedBorderColor = EchoListTheme.materialColors.surfaceVariant
                    )
                )
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(dimensions.xs))
                    Text(
                        text = uiState.error,
                        style = EchoListTheme.typography.bodySmall,
                        color = EchoListTheme.materialColors.secondary
                    )
                }
            }
        },
        confirmButton = {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensions.xl),
                    color = EchoListTheme.materialColors.primary,
                    strokeWidth = dimensions.xxs
                )
            } else {
                ElButton(
                    onClick = onConfirm,
                    isEnabled = uiState.isConfirmEnabled
                ) {
                    Text("Rename")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = EchoListTheme.materialColors.onSurface
                )
            }
        }
    )
}

@Preview
@Composable
internal fun RenameFolderDialogPreview() {
    EchoListTheme {
        EchoListBackground {
            RenameFolderDialog(
                uiState = RenameFolderUiState(
                    isVisible = true,
                    folderName = "My Folder",
                    isLoading = false,
                    error = null
                ),
                onNameChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}