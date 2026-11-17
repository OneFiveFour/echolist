package net.onefivefour.echolist.feature.tasksettings.ui

import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.RecurrenceState
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.hasValidDetails

internal class MainTaskSettingsViewModel(
    private val mainTaskId: String,
    currentDueDate: String,
    currentRecurrence: String,
    currentIsNotificationEnabled: Boolean,
    private val permissionChecker: NotificationPermissionChecker,
    private val permissionRequester: NotificationPermissionRequester,
    private val resultBus: net.onefivefour.echolist.core.tasks.domain.TaskSettingsResultSink
) : ViewModel() {

    val uiState: StateFlow<MainTaskSettingsUiState>
        field = MutableStateFlow<MainTaskSettingsUiState>(
            MainTaskSettingsUiState.Ready(
                selectedDueDate = currentDueDate,
                recurrenceState = rruleToRecurrenceState(currentRecurrence),
                initialDateMillis = dueDateToUtcMillis(currentDueDate),
                isNotificationEnabled = currentIsNotificationEnabled,
                isNotificationToggleEnabled = currentDueDate.isNotBlank()
            )
        )

    fun onDateSelected(dateMillis: Long) {
        val dueDate = utcMillisToDueDate(dateMillis)
        updateReady { state ->
            state.copy(
                selectedDueDate = dueDate,
                isNotificationToggleEnabled = dueDate.isNotBlank(),
                initialDateMillis = dateMillis,
                showRecurrenceValidationErrors = false,
                showDueDateRequiredError = false
            )
        }
        confirm()
    }

    fun onNotificationToggleChanged(enabled: Boolean) {
        updateReady { it.copy(isNotificationEnabled = enabled && it.isNotificationToggleEnabled) }
        confirm()
    }

    fun onRecurrenceIntervalSelected(interval: RecurrenceInterval) {
        val newRecurrenceState = when (interval) {
            RecurrenceInterval.Off -> RecurrenceState.Off
            RecurrenceInterval.Daily -> RecurrenceState.Daily()
            RecurrenceInterval.Weekly -> RecurrenceState.Weekly()
            RecurrenceInterval.Monthly -> RecurrenceState.Monthly()
            RecurrenceInterval.Yearly -> RecurrenceState.Yearly
        }

        updateReady { state ->
            if (interval == RecurrenceInterval.Off) {
                state.copy(
                    recurrenceState = newRecurrenceState,
                    showRecurrenceValidationErrors = false
                )
            } else {
                state.copy(
                    recurrenceState = newRecurrenceState,
                    showRecurrenceValidationErrors = false
                )
            }
        }
        confirm()
    }

    fun onRecurrenceDetailChanged(state: RecurrenceState) {
        updateReady { it.copy(recurrenceState = state) }
        confirm()
    }

    private fun updateReady(transform: (MainTaskSettingsUiState.Ready) -> MainTaskSettingsUiState.Ready) {
        uiState.update { current ->
            when (current) {
                is MainTaskSettingsUiState.Ready -> transform(current)
                is MainTaskSettingsUiState.Loading -> current
            }
        }
    }

    private fun confirm(): Boolean {
        val currentState = uiState.value as? MainTaskSettingsUiState.Ready ?: return false

        if (currentState.recurrenceState != RecurrenceState.Off && currentState.selectedDueDate.isBlank()) {
            updateReady { it.copy(showDueDateRequiredError = true) }
            return false
        }

        if (!currentState.recurrenceState.hasValidDetails()) {
            updateReady { it.copy(showRecurrenceValidationErrors = true) }
            return false
        }

        viewModelScope.launch {
            resultBus.emit(
                TaskSettingsChanges(
                    mainTaskId = mainTaskId,
                    dueDate = currentState.selectedDueDate,
                    recurrence = currentState.recurrenceState.toRrule(),
                    isNotificationEnabled = currentState.isNotificationEnabled
                )
            )
        }
        return true
    }

    fun onScreenLeaving() {
        viewModelScope.launch {
            val state = uiState.value as? MainTaskSettingsUiState.Ready ?: return@launch

            if (!state.isNotificationEnabled || state.selectedDueDate.isBlank()) {
                return@launch
            }

            val isGranted = runCatching { permissionChecker.isGranted() }.getOrDefault(false)
            if (isGranted) return@launch

            val wasGranted = runCatching { permissionRequester.request() }.getOrDefault(false)
            if (wasGranted) return@launch

            updateReady { it.copy(isNotificationEnabled = false) }

            resultBus.emit(
                TaskSettingsChanges(
                    mainTaskId = mainTaskId,
                    dueDate = state.selectedDueDate,
                    recurrence = state.recurrenceState.toRrule(),
                    isNotificationEnabled = false
                )
            )
        }
    }
}