package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CreateItemMenu(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    createItemCallbacks: CreateItemCallbacks
) {
    DropdownMenu(expanded = isExpanded, onDismissRequest = onDismiss,
        containerColor = EchoListTheme.materialColors.surface) {
        for ((label, action) in listOf(
            stringResource(Res.string.create_folder) to createItemCallbacks.onCreateFolder,
            stringResource(Res.string.create_note) to createItemCallbacks.onCreateNote,
            stringResource(Res.string.create_task_list) to createItemCallbacks.onCreateTaskList
        )) {
            DropdownMenuItem(modifier = Modifier.testTag("create-$label"), text = { Text(label) }, onClick = { onDismiss(); action() })
        }
    }
}
