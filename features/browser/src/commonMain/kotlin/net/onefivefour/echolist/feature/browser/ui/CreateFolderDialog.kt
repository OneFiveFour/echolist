package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import net.onefivefour.echolist.core.designsystem.ui.components.ElButton
import net.onefivefour.echolist.core.designsystem.ui.components.ElOutlinedTextField
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun CreateFolderDialog(
    uiState: CreateFolderUiState,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!uiState.isVisible) return

    val focusRequester = remember { FocusRequester() }
    val dimensions = EchoListTheme.dimensions

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
                text = "Create Folder",
                style = EchoListTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                ElOutlinedTextField(
                    text = uiState.folderName,
                    label = "Folder name",
                    onValueChange = onNameChange,
                    isError = uiState.error != null,
                    modifier = Modifier.focusRequester(focusRequester),
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { onConfirm() })
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
                    Text("Create")
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

@PreviewComponentLightDark
@Composable
private fun CreateFolderDialogPreview() {
    EchoListTheme(modifier = Modifier) {
        CreateFolderDialog(
            uiState = CreateFolderUiState(
                isVisible = true,
                folderName = "Projects"
            ),
            onNameChange = {},
            onConfirm = {},
            onDismiss = {})
    }
}
