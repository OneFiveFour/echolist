package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_folder
import net.onefivefour.echolist.feature.browser.resources.folder_create_cancel
import net.onefivefour.echolist.feature.browser.resources.folder_create_confirm
import net.onefivefour.echolist.feature.browser.resources.folder_create_saving
import net.onefivefour.echolist.feature.browser.resources.folder_name
import net.onefivefour.echolist.feature.browser.resources.folder_rename_cancel
import net.onefivefour.echolist.feature.browser.resources.folder_rename_confirm
import net.onefivefour.echolist.feature.browser.resources.folder_rename_saving
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

@Composable
internal fun FolderDraftItem(
    draft: FolderDraft,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var textFieldValue by rememberSaveable(draft.id, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = draft.name,
                selection = if (draft.isRenaming) TextRange(0, draft.name.length) else TextRange(draft.name.length)
            )
        )
    }

    val focusRequester = remember(draft.id) {
        FocusRequester()
    }

    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

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

    var lastFocusRequest by rememberSaveable(draft.id) {
        mutableStateOf(-1L)
    }

    LaunchedEffect(draft.id, draft.focusRequest) {
        if (!draft.isSaving && lastFocusRequest != draft.focusRequest) {
            lastFocusRequest = draft.focusRequest
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    val savingLabel = stringResource(
        when {
            draft.isRenaming -> Res.string.folder_rename_saving
            else -> Res.string.folder_create_saving
        }
    )

    val confirmLabel = stringResource(
        when {
            draft.isRenaming -> Res.string.folder_rename_confirm
            else -> Res.string.folder_create_confirm
        }
    )

    val cancelLabel = stringResource(
        when {
            draft.isRenaming -> Res.string.folder_rename_cancel
            else -> Res.string.folder_create_cancel
        }
    )

    val nameLabel = stringResource(Res.string.folder_name)

    val colors = EchoListTheme.materialColors

    BrowserItemCard(
        title = "",
        icon = Res.drawable.browser_folder,
        accent = EchoListTheme.echoListColorScheme.folderColor,
        isFolder = true,
        onClick = null,
        modifier = Modifier
            .testTag("browser-folder-draft")
            .onPreviewKeyEvent { event ->
                when (event.key) {
                    Key.Escape -> {
                        if (event.type == KeyEventType.KeyUp) {
                            cancel()
                        }
                        true
                    }
                    Key.Enter, Key.NumPadEnter -> {
                        if (event.type == KeyEventType.KeyUp) {
                            confirm()
                        }
                        true
                    }
                    else -> false
                }
            },
        titleContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = when (textFieldValue.text) {
                        draft.name -> textFieldValue
                        else -> textFieldValue.copy(text = draft.name)
                    },
                    onValueChange = { value ->
                        textFieldValue = value
                        if (value.text != draft.name) {
                            onNameChange(value.text)
                        }
                    },
                    enabled = !draft.isSaving,
                    singleLine = true,
                    cursorBrush = SolidColor(colors.primary),
                    textStyle = EchoListTheme.typography.titleSmall.copy(color = colors.onSurface),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirm() }),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = EchoListTheme.dimensions.m)
                        .focusRequester(focusRequester)
                        .testTag("folder-draft-name")
                        .semantics {
                            contentDescription = nameLabel
                            draft.error?.let { error(it) }
                        }
                )

                if (draft.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(EchoListTheme.dimensions.m)
                            .size(EchoListTheme.dimensions.xl)
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
                            confirmLabel,
                            tint = if (draft.canConfirm) colors.primary else colors.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { cancel() },
                        modifier = Modifier.testTag("folder-draft-cancel")
                    ) {
                        Icon(Icons.Default.Close, cancelLabel, tint = colors.onSurfaceVariant)
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

@PreviewComponentLightDark
@Composable
private fun FolderDraftItemPreview() {
    EchoListTheme(modifier = Modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)) {
            // focusRequest = -1 keeps previews from requesting focus and showing the keyboard.
            listOf(
                FolderDraft(id = 1, focusRequest = -1),
                FolderDraft(id = 2, name = "Projects", focusRequest = -1),
                FolderDraft(id = 3, name = "Projects", error = "Folder already exists", focusRequest = -1),
                FolderDraft(id = 4, name = "Projects", isSaving = true, focusRequest = -1)
            ).forEach { folderDraft ->
                FolderDraftItem(
                    draft = folderDraft,
                    onNameChange = {},
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }
    }
}