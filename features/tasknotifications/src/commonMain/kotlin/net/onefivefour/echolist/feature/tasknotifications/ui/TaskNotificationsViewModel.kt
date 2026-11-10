package net.onefivefour.echolist.feature.tasknotifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

internal data class TaskNotificationsUiState(
    val isLoading: Boolean = true,
    val tasks: List<UpcomingMainTask> = emptyList(),
    val enabledTaskIds: Set<String> = emptySet(),
    val currentlyChangingTaskIds: Set<String> = emptySet(),
    val error: NotificationError? = null
)

internal class TaskNotificationsViewModel(
    private val taskListRepository: TaskListRepository,
    private val notifications: NotificationCoordinator,
    private val permissionChecker: NotificationPermissionChecker,
    private val permissionRequester: NotificationPermissionRequester,
    private val notificationSyncHandler: NotificationSyncHandler,
    private val clock: Clock = Clock.System
) : ViewModel() {
    private val state = MutableStateFlow(TaskNotificationsUiState())
    val uiState = state.asStateFlow()

    init { load() }

    fun load() {
        if (state.value.currentlyChangingTaskIds.isNotEmpty()) return
        state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // The API cutoff is inclusive; only dates after today belong in this screen.
                val fromDate = clock.todayIn(TimeZone.UTC).plus(1, DateTimeUnit.DAY)
                val tasks = taskListRepository.listUpcomingMainTasks(fromDate).getOrThrow()
                    .filter { it.dueDate >= fromDate }
                    .sortedBy { it.dueDate }
                val enabledTaskIds = tasks.filter { notifications.isEnabled(it.taskId) }
                    .mapTo(mutableSetOf()) { it.taskId }
                state.update { it.copy(isLoading = false, tasks = tasks, enabledTaskIds = enabledTaskIds) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                state.update { it.copy(isLoading = false, error = NotificationError.Load) }
            }
        }
    }

    fun setEnabled(taskId: String, isEnabled: Boolean) {
        if (state.value.isLoading || taskId in state.value.currentlyChangingTaskIds || state.value.tasks.none { it.taskId == taskId }) return
        state.update { it.copy(currentlyChangingTaskIds = it.currentlyChangingTaskIds + taskId, error = null) }
        viewModelScope.launch {
            try {
                notifications.setEnabled(taskId, isEnabled)
                state.update { current ->
                    current.copy(enabledTaskIds = if (isEnabled) {
                        current.enabledTaskIds + taskId
                    } else {
                        current.enabledTaskIds - taskId
                    })
                }
                if (isEnabled) {
                    if (!permissionChecker.isGranted() && !permissionRequester.request()) {
                        state.update { it.copy(error = NotificationError.Permission) }
                    }
                    // Restore known reminders even if the network is unavailable, then fetch metadata.
                    notifications.restore()
                    notificationSyncHandler.synchronize()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                state.update { it.copy(error = NotificationError.Save) }
            } finally {
                state.update { it.copy(currentlyChangingTaskIds = it.currentlyChangingTaskIds - taskId) }
            }
        }
    }
}
