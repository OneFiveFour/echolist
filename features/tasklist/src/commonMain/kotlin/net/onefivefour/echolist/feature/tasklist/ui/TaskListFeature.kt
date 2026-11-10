package net.onefivefour.echolist.feature.tasklist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import net.onefivefour.echolist.core.files.domain.normalizePath
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TaskListFeature(
    parentDir: String,
    taskListId: String?,
    settingsResults: Flow<TaskSettingsChanges>,
    onOpenSettings: (String?, TaskSettingsChanges) -> Unit,
    onNavigateBack: () -> Unit
) {
    val resolvedTaskListId = taskListId?.takeIf { it.isNotBlank() }

    val mode = resolvedTaskListId?.let(EditTaskListMode::Edit)
        ?: EditTaskListMode.Create(normalizePath(parentDir))

    val viewModel = koinViewModel<EditTaskListViewModel>(
        key = "editTaskList-$parentDir-${resolvedTaskListId.orEmpty()}"
    ) { parametersOf(mode, settingsResults) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navigateBack.collect { onNavigateBack() }
    }

    DisposableEffect(viewModel) {
        onDispose { viewModel.onScreenLeft() }
    }

    EditTaskListScreen(
        uiState = uiState,
        onAddMainTask = viewModel::onAddMainTask,
        onRemoveMainTask = viewModel::onRemoveMainTask,
        onAddSubTask = viewModel::onAddSubTask,
        onRemoveSubTask = viewModel::onRemoveSubTask,
        onMainTaskCheckedChange = viewModel::onMainTaskCheckedChange,
        onSubTaskCheckedChange = viewModel::onSubTaskCheckedChange,
        onToggleAutoDelete = viewModel::onToggleAutoDelete,
        onFieldFocusLost = viewModel::onFieldFocusLost,
        onNavigateToSettings = {
                mainTaskId,
                currentDueDate,
                currentRecurrence,
                currentIsNotificationEnabled ->
            viewModel.onSettingsNavigationStarted()
            onOpenSettings(
                viewModel.taskListId,
                TaskSettingsChanges(mainTaskId, currentDueDate, currentRecurrence, currentIsNotificationEnabled)
            )
        },
        onDeleteClick = viewModel::onDeleteClick
    )
}
