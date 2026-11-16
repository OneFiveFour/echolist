package net.onefivefour.echolist.feature.tasklist.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.SubTask
import net.onefivefour.echolist.core.designsystem.ui.components.EditTitle
import net.onefivefour.echolist.core.designsystem.ui.components.ElButton
import net.onefivefour.echolist.core.designsystem.ui.components.EchoListBackground
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun EditTaskListScreen(
    uiState: EditTaskListUiState,
    onAddMainTask: () -> String,
    onRemoveMainTask: (Int) -> Unit,
    onAddSubTask: (Int) -> Unit,
    onRemoveSubTask: (Int, Int) -> Unit,
    onMainTaskCheckedChange: (Int, Boolean) -> Unit,
    onSubTaskCheckedChange: (Int, Int, Boolean) -> Unit,
    onToggleAutoDelete: (Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    onNavigateToSettings: (String, String, String, Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controller = rememberEditTaskListScreenController(
        mainTasks = uiState.uiMainTasks,
        onAddMainTask = onAddMainTask,
        onRemoveMainTask = onRemoveMainTask,
        onAddSubTask = onAddSubTask,
        onRemoveSubTask = onRemoveSubTask
    )

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        EditTitle(
            textFieldState = uiState.titleState,
            requestFocus = uiState.isCreateMode,
            onNext = controller.onTitleKeyboardAction,
            onFocusLost = onFieldFocusLost
        )

        TaskListContentCard(
            modifier = Modifier.weight(1f),
            uiState = uiState,
            controller = controller,
            onToggleAutoDelete = onToggleAutoDelete,
            onFieldFocusLost = onFieldFocusLost,
            onMainTaskCheckedChange = onMainTaskCheckedChange,
            onRemoveMainTask = onRemoveMainTask,
            onSubTaskCheckedChange = onSubTaskCheckedChange,
            onNavigateToSettings = onNavigateToSettings,
            onDeleteClick = onDeleteClick
        )

        Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

        ElButton(
            onClick = controller.onAddMainTaskAndFocus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Add another main task",
                style = EchoListTheme.typography.labelMedium
            )
        }

        uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))
            Text(
                text = errorMessage,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error
            )
        }

        Box(
            modifier = Modifier
                .size(1.dp)
                .focusRequester(controller.blurFocusRequester)
                .focusable()
        )
    }
}

@Composable
private fun TaskListContentCard(
    modifier: Modifier = Modifier,
    uiState: EditTaskListUiState,
    controller: EditTaskListScreenController,
    onToggleAutoDelete: (Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    onMainTaskCheckedChange: (Int, Boolean) -> Unit,
    onRemoveMainTask: (Int) -> Unit,
    onSubTaskCheckedChange: (Int, Int, Boolean) -> Unit,
    onNavigateToSettings: (String, String, String, Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = EchoListTheme.shapes.medium,
        color = EchoListTheme.materialColors.surface,
        modifier = modifier
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = EchoListTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .padding(EchoListTheme.dimensions.m)
                .fillMaxSize()
        ) {
            DeleteRow(
                uiState = uiState,
                onToggleAutoDelete = onToggleAutoDelete,
                onDeleteTaskList = onDeleteClick
            )

            Spacer(modifier = Modifier.height(EchoListTheme.dimensions.m))

            when {
                uiState.isLoading -> TaskListLoading()
                uiState.uiMainTasks.isEmpty() -> EmptyTaskList(controller.onAddMainTaskAndFocus)
                else -> TaskList(
                    focusedMainTaskId = controller.focusedMainTaskId,
                    focusTarget = controller.resolvedFocusTarget,
                    isAutoDelete = uiState.isAutoDelete,
                    mainTasks = uiState.uiMainTasks,
                    onAddSubTask = controller.onAddFirstSubTaskAndFocus,
                    onFieldFocusLost = onFieldFocusLost,
                    onFocusHandled = controller.onFocusHandled,
                    onMainTaskCheckedChange = onMainTaskCheckedChange,
                    onMainTaskDescriptionFocusChanged = controller.onMainTaskDescriptionFocusChanged,
                    onNavigateToSettings = onNavigateToSettings,
                    onMainTaskKeyboardAction = controller.onMainTaskKeyboardAction,
                    onRemoveMainTask = onRemoveMainTask,
                    onSubTaskCheckedChange = onSubTaskCheckedChange,
                    onSubTaskKeyboardAction = controller.onSubTaskKeyboardAction
                )
            }
        }
    }
}

@Preview
@Composable
private fun EditTaskListScreenPreview() {
    val tasks = remember {
        mutableStateListOf(
            UiMainTask.fromDomain(
                domain = MainTask(
                    id = "preview-1",
                    description = "Plan launch",
                    isDone = false,
                    dueDate = "2026-04-01",
                    recurrence = "",
                    subTasks = listOf(
                        SubTask(id = "sub-1", description = "Draft checklist", isDone = true),
                        SubTask(id = "sub-2", description = "Review copy", isDone = false)
                    )
                )
            )
        )
    }

    EchoListTheme {
        EchoListBackground {
            EditTaskListScreen(
                uiState = EditTaskListUiState(
                    titleState = TextFieldState(initialText = "Launch plan"),
                    uiMainTasks = tasks,
                    isAutoDelete = false,
                    mode = EditTaskListMode.Create(parentDir = "")
                ),
                onAddMainTask = { "preview-main-task" },
                onRemoveMainTask = {},
                onAddSubTask = {},
                onRemoveSubTask = { _, _ -> },
                onMainTaskCheckedChange = { _, _ -> },
                onSubTaskCheckedChange = { _, _, _ -> },
                onToggleAutoDelete = {},
                onFieldFocusLost = {},
                onNavigateToSettings = { _, _, _, _ -> },
                onDeleteClick = {}
            )
        }
    }
}