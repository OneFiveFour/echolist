package net.onefivefour.echolist.feature.tasklist.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun SubTaskRow(
    subTask: UiSubTask,
    shouldRequestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onKeyboardAction: (String) -> Unit,
    onFocusLost: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val focusRequester = remember(subTask.id) { FocusRequester() }

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EchoListTheme.dimensions.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides EchoListTheme.dimensions.xxl) {
            Checkbox(
                checked = subTask.isDone,
                onCheckedChange = onCheckedChange
            )
        }

        TaskDescriptionField(
            state = subTask.descriptionState,
            modifier = Modifier.weight(1f),
            isDone = subTask.isDone,
            onKeyboardAction = { onKeyboardAction(subTask.id) },
            onFocusLost = onFocusLost,
            focusRequester = focusRequester
        )
    }
}

@Preview
@Composable
private fun SubTaskRowPreview() {
    val subTask = remember {
        UiSubTask(id = "preview-sub-1", description = "Review copy", isDone = false)
    }
    EchoListTheme {
        SubTaskRow(
            subTask = subTask,
            shouldRequestFocus = false,
            onFocusHandled = {},
            onKeyboardAction = {},
            onFocusLost = {},
            onCheckedChange = {}
        )
    }
}

@Preview
@Composable
private fun SubTaskRowDonePreview() {
    val subTask = remember {
        UiSubTask(id = "preview-sub-2", description = "Draft checklist", isDone = true)
    }
    EchoListTheme {
        SubTaskRow(
            subTask = subTask,
            shouldRequestFocus = false,
            onFocusHandled = {},
            onKeyboardAction = {},
            onFocusLost = {},
            onCheckedChange = {}
        )
    }
}