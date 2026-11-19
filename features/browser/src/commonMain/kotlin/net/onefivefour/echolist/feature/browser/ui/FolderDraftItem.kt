package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FolderDraftItem(
    draft: FolderDraft,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isDesktop: Boolean
) {
    val focusRequester = remember(draft.id) { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    // Shared by the button, hardware Enter and the IME action.
    val confirm = {
        if (draft.canConfirm) {
            keyboard?.hide()
            focusManager.clearFocus()
            onConfirm()
        }
    }
    val cancel = {
        if (!draft.isSaving) {
            keyboard?.hide()
            focusManager.clearFocus()
            onCancel()
        }
    }
    var lastFocusRequest by rememberSaveable(draft.id) { mutableStateOf(-1L) }
    LaunchedEffect(draft.id, draft.focusRequest) {
        if (!draft.isSaving && lastFocusRequest != draft.focusRequest) {
            lastFocusRequest = draft.focusRequest
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    val savingLabel = stringResource(Res.string.folder_create_saving)
    val nameLabel = stringResource(Res.string.folder_name)
    val colors = EchoListTheme.materialColors
    BrowserItemCard(
        title = "",
        icon = Res.drawable.browser_folder,
        accent = EchoListTheme.echoListColorScheme.folderColor,
        isFolder = true,
        isDesktop = isDesktop,
        onClick = null,
        modifier = Modifier.testTag("browser-folder-draft").onPreviewKeyEvent { event ->
            when {
                event.key == Key.Escape -> {
                    if (event.type == KeyEventType.KeyUp) cancel()
                    true
                }
                event.key == Key.Enter || event.key == Key.NumPadEnter -> {
                    if (event.type == KeyEventType.KeyUp) confirm()
                    true
                }
                else -> false
            }
        },
        titleContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = draft.name,
                    onValueChange = onNameChange,
                    enabled = !draft.isSaving,
                    singleLine = true,
                    cursorBrush = SolidColor(colors.primary),
                    textStyle = EchoListTheme.typography.titleSmall.copy(color = colors.onSurface),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirm() }),
                    modifier = Modifier.weight(1f).padding(vertical = EchoListTheme.dimensions.m)
                        .focusRequester(focusRequester).testTag("folder-draft-name")
                        .semantics {
                            contentDescription = nameLabel
                            draft.error?.let { error(it) }
                        }
                )
                if (draft.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(EchoListTheme.dimensions.m).size(EchoListTheme.dimensions.xl)
                            .semantics { contentDescription = savingLabel },
                        color = colors.primary
                    )
                } else {
                    IconButton(
                        onClick = { confirm() },
                        enabled = draft.canConfirm,
                        modifier = Modifier.testTag("folder-draft-confirm")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            stringResource(Res.string.folder_create_confirm),
                            tint = if (draft.canConfirm) colors.primary else colors.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { cancel() }, modifier = Modifier.testTag("folder-draft-cancel")) {
                        Icon(Icons.Default.Close, stringResource(Res.string.folder_create_cancel), tint = colors.onSurfaceVariant)
                    }
                }
            }
        }
    ) {
        draft.error?.let { message ->
            Text(
                message,
                color = colors.error,
                style = EchoListTheme.typography.bodySmall,
                modifier = Modifier.testTag("folder-draft-error")
            )
        }
    }
}
