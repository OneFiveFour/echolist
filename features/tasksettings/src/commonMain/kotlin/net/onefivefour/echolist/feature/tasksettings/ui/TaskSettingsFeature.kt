package net.onefivefour.echolist.feature.tasksettings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.onefivefour.echolist.core.tasks.domain.TaskSettingsResultSink
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TaskSettingsFeature(
    initial: TaskSettingsChanges,
    onResult: TaskSettingsResultSink,
    onDebugReminder: (() -> Unit)? = null
) {
    val viewModel = koinViewModel<MainTaskSettingsViewModel>(
        key = "mainTaskSettings-${initial.mainTaskId}"
    ) {
        parametersOf(
            initial.mainTaskId,
            initial.dueDate,
            initial.recurrence,
            initial.isNotificationEnabled,
            onResult
        )
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(viewModel) {
        onDispose { viewModel.onScreenLeaving() }
    }

    MainTaskSettingsScreen(
        uiState = uiState,
        onDateSelected = viewModel::onDateSelected,
        onRecurrenceIntervalSelected = viewModel::onRecurrenceIntervalSelected,
        onRecurrenceDetailChanged = viewModel::onRecurrenceDetailChanged,
        onNotificationToggleChanged = viewModel::onNotificationToggleChanged,
        onDebugReminder = onDebugReminder
    )
}
