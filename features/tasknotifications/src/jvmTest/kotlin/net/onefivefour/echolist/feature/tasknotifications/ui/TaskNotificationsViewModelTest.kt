package net.onefivefour.echolist.feature.tasknotifications.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionRequester
import net.onefivefour.echolist.core.notifications.domain.NotificationSyncHandler
import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.ReminderState
import net.onefivefour.echolist.core.notifications.domain.ReminderStore
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TaskNotificationsViewModelTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeEach { Dispatchers.setMain(dispatcher) }
    afterEach { Dispatchers.resetMain() }

    test("requests tomorrow in UTC and sorts future tasks by next due date") {
        runTest(dispatcher) {
            val fixture = NotificationFixture()
            val tomorrow = fixture.task.copy(taskId = "tomorrow", dueDate = LocalDate(2028, 2, 29))
            val sameDate = tomorrow.copy(taskId = "same-date", title = "Same date", isRecurring = false)
            fixture.upcomingResult = Result.success(listOf(
                fixture.task,
                fixture.task.copy(taskId = "today", dueDate = LocalDate(2028, 2, 28)),
                tomorrow,
                sameDate,
                fixture.task.copy(taskId = "past", dueDate = LocalDate(2028, 2, 27))
            ))
            fixture.coordinator.setEnabled("tomorrow", false)
            val vm = fixture.viewModel()
            advanceUntilIdle()

            fixture.requestedDates shouldBe listOf(LocalDate(2028, 2, 29))
            vm.uiState.value.tasks shouldBe listOf(tomorrow, sameDate, fixture.task)
            vm.uiState.value.enabledTaskIds shouldBe setOf("task", "same-date")
            // A disabled task remains in the catalog and can be enabled again.
            vm.setEnabled("tomorrow", true)
            advanceUntilIdle()
            vm.uiState.value.enabledTaskIds shouldBe setOf("task", "tomorrow", "same-date")
            vm.uiState.value.tasks shouldBe listOf(tomorrow, sameDate, fixture.task)
            vm.setEnabled("today", false)
            advanceUntilIdle()
            fixture.coordinator.isEnabled("today") shouldBe true
        }
    }

    test("an empty upcoming response produces an empty list") {
        runTest(dispatcher) {
            val fixture = NotificationFixture()
            fixture.upcomingResult = Result.success(emptyList())
            val vm = fixture.viewModel()
            advanceUntilIdle()
            vm.uiState.value.tasks shouldBe emptyList()
            vm.uiState.value.enabledTaskIds shouldBe emptySet()
            vm.uiState.value.isLoading shouldBe false
            vm.uiState.value.error shouldBe null
        }
    }

    test("loads persisted preferences and toggles cancel and reschedule through the coordinator") {
        runTest(dispatcher) {
            val fixture = NotificationFixture()
            fixture.coordinator.refreshSession()
            fixture.coordinator.schedule("task", "list", "Task", "Body", "2095-01-01")
            fixture.coordinator.setEnabled("task", false)
            val vm = fixture.viewModel()
            advanceUntilIdle()
            vm.uiState.value.enabledTaskIds shouldBe emptySet()

            vm.setEnabled("task", true)
            advanceUntilIdle()
            vm.uiState.value.enabledTaskIds shouldBe setOf("task")
            fixture.coordinator.isEnabled("task") shouldBe true
            fixture.scheduled.keys.toList() shouldBe listOf("task")
            fixture.syncCount shouldBe 1

            vm.setEnabled("task", false)
            advanceUntilIdle()
            fixture.coordinator.isEnabled("task") shouldBe false
            fixture.scheduled.isEmpty() shouldBe true
            fixture.dismissed.last() shouldBe "task"
            vm.uiState.value.currentlyChangingTaskIds shouldBe emptySet()
        }
    }

    test("denied system permission keeps the preference enabled and shows a useful message") {
        runTest(dispatcher) {
            val fixture = NotificationFixture()
            fixture.permissionGranted = false
            val vm = fixture.viewModel()
            advanceUntilIdle()
            vm.setEnabled("task", true)
            advanceUntilIdle()
            fixture.permissionRequests shouldBe 1
            vm.uiState.value.error shouldBe NotificationError.Permission
            fixture.coordinator.isEnabled("task") shouldBe true
        }
    }

    test("load failures are retryable and save failures leave the toggle unchanged") {
        runTest(dispatcher) {
            val fixture = NotificationFixture()
            fixture.upcomingResult = Result.failure(IllegalStateException("Offline"))
            val vm = fixture.viewModel()
            advanceUntilIdle()
            vm.uiState.value.error shouldBe NotificationError.Load
            fixture.upcomingResult = Result.success(listOf(fixture.task))
            vm.load()
            advanceUntilIdle()
            vm.uiState.value.error shouldBe null
            fixture.failWrites = true
            vm.setEnabled("task", false)
            advanceUntilIdle()
            vm.uiState.value.error shouldBe NotificationError.Save
            vm.uiState.value.enabledTaskIds shouldBe setOf("task")
            vm.uiState.value.currentlyChangingTaskIds shouldBe emptySet()
        }
    }
})

private class NotificationFixture {
    val task = UpcomingMainTask("task", "Task", LocalDate(2095, 1, 1), "list", isRecurring = true)
    var upcomingResult: Result<List<UpcomingMainTask>> = Result.success(listOf(task))
    val requestedDates = mutableListOf<LocalDate>()
    val clock = object : Clock {
        override fun now() = Instant.parse("2028-02-28T23:30:00Z")
    }
    val repository = object : TaskListRepository {
        override suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>> {
            requestedDates.add(fromDate)
            return upcomingResult
        }
        override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> = error("Unexpected create")
        override suspend fun getTaskList(taskListId: String): Result<TaskList> = error("Unexpected task-list fetch")
        override suspend fun getMainTask(mainTaskId: String): Result<MainTask> = error("Unexpected task fetch")
        override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> = error("Unexpected directory fetch")
        override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> = error("Unexpected update")
        override suspend fun deleteTaskList(taskListId: String): Result<Unit> = error("Unexpected delete")
    }
    var snapshot = ReminderState()
    var failWrites = false
    var permissionGranted = true
    var permissionRequests = 0
    var syncCount = 0
    val scheduled = mutableMapOf<String, Reminder>()
    val dismissed = mutableListOf<String>()
    val permission = object : NotificationPermissionChecker {
        override suspend fun isGranted() = permissionGranted
    }
    val coordinator = NotificationCoordinator(
        object : ReminderStore {
            override suspend fun read() = snapshot
            override suspend fun write(state: ReminderState) {
                check(!failWrites) { "Storage unavailable" }
                snapshot = state
            }
        },
        object : ReminderPlatform {
            override suspend fun schedule(reminder: Reminder) { scheduled[reminder.taskId] = reminder }
            override suspend fun cancelPending(reminder: Reminder) { scheduled.remove(reminder.taskId) }
            override suspend fun dismiss(reminder: Reminder) { dismissed.add(reminder.taskId) }
            override suspend fun show(reminder: Reminder) = true
        },
        permission,
        NotificationAccountProvider { "account" },
        object : ReminderBackgroundScheduler {
            override fun start() {}
            override fun requestSync() {}
            override fun stop() {}
        }
    )

    fun viewModel() =
        TaskNotificationsViewModel(
            repository, coordinator, permission,
            object : NotificationPermissionRequester {
                override suspend fun request(): Boolean {
                    permissionRequests++
                    return permissionGranted
                }
            },
            NotificationSyncHandler { syncCount++; Result.success(Unit) },
            clock
        )
}
