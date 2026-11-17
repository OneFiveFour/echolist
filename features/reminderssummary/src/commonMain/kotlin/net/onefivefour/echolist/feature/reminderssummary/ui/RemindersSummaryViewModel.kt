package net.onefivefour.echolist.feature.reminderssummary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.domain.NotificationSyncHandler
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import kotlin.time.Clock

internal enum class NotificationError { Load, Save, Permission }

internal data class RemindersSummaryUiState(
    val isLoading: Boolean = true,
    val tasks: List<UpcomingMainTask> = emptyList(),
    val enabledTaskIds: Set<String> = emptySet(),
    val currentlyChangingTaskIds: Set<String> = emptySet(),
    val error: NotificationError? = null
)

internal class RemindersSummaryViewModel(
    private val taskListRepository: TaskListRepository,
    private val notifications: NotificationCoordinator,
    private val permissionChecker: NotificationPermissionChecker,
    private val permissionRequester: NotificationPermissionRequester,
    private val notificationSyncHandler: NotificationSyncHandler,
    private val clock: Clock = Clock.System
) : ViewModel() {
    val uiState: StateFlow<RemindersSummaryUiState>
        field = MutableStateFlow(RemindersSummaryUiState())

    init {
        load()
    }

    fun load() {
        if (uiState.value.currentlyChangingTaskIds.isNotEmpty()) return
        uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // The API cutoff is inclusive; only dates after today belong in this screen.
                val fromDate = clock.todayIn(TimeZone.UTC).plus(1, DateTimeUnit.DAY)
                val tasks = taskListRepository.listUpcomingMainTasks(fromDate).getOrThrow()
                    .filter { it.dueDate >= fromDate }
                    .sortedBy { it.dueDate }
                val enabledTaskIds = tasks.filter { notifications.isEnabled(it.taskId) }
                    .mapTo(mutableSetOf()) { it.taskId }
                uiState.update { it.copy(isLoading = false, tasks = tasks, enabledTaskIds = enabledTaskIds) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState.update { it.copy(isLoading = false, error = NotificationError.Load) }
            }
        }
    }

    fun setEnabled(taskId: String, isEnabled: Boolean) {
        if (uiState.value.isLoading || taskId in uiState.value.currentlyChangingTaskIds || uiState.value.tasks.none {
                it.taskId == taskId
            }
        ) {
            return
        }
        uiState.update { it.copy(currentlyChangingTaskIds = it.currentlyChangingTaskIds + taskId, error = null) }
        viewModelScope.launch {
            try {
                notifications.setEnabled(taskId, isEnabled)
                uiState.update { current ->
                    current.copy(
                        enabledTaskIds = if (isEnabled) {
                            current.enabledTaskIds + taskId
                        } else {
                            current.enabledTaskIds - taskId
                        }
                    )
                }
                if (isEnabled) {
                    if (!permissionChecker.isGranted() && !permissionRequester.request()) {
                        uiState.update { it.copy(error = NotificationError.Permission) }
                    }
                    // Restore known reminders even if the network is unavailable, then fetch metadata.
                    notifications.restore()
                    notificationSyncHandler.synchronize()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                uiState.update { it.copy(error = NotificationError.Save) }
            } finally {
                uiState.update { it.copy(currentlyChangingTaskIds = it.currentlyChangingTaskIds - taskId) }
            }
        }
    }
}