package net.onefivefour.echolist.feature.tasklist.ui

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.MainTasksSummary
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository

/**
 * Integration tests verifying the toggle deactivation → sync → cancel flow.
 *
 * Validates: Requirements 5.3
 *
 * When the notification toggle changes from enabled to disabled via TaskSettingsChanges,
 * the EditTaskListViewModel must:
 * 1. Update the UiMainTask's isNotificationEnabled to false
 * 2. Trigger a sync (requestSync → repository updateTaskList call)
 * 3. Cancel the notification for that task via the scheduler
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class EditTaskListNotificationIntegrationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    test("toggle deactivation via settingsResultBus triggers sync and cancels notification") {
        runTest(testDispatcher) {
            val repo = IntegrationTestRepository()
            val scheduler = IntegrationRecordingScheduler()
            val settingsResultBus = MainTaskSettingsResultBus()

            val existingTaskList = TaskList(
                id = "tl-integration-1",
                parentDir = "home",
                name = "Daily routine",
                tasks = listOf(
                    MainTask(
                        id = "task-A",
                        description = "Morning workout",
                        isDone = false,
                        dueDate = "2027-06-01",
                        recurrence = "FREQ=DAILY",
                        isNotificationEnabled = true,
                        subTasks = emptyList()
                    )
                ),
                updatedAt = 1L,
                isAutoDelete = false
            )
            repo.addTaskList(existingTaskList)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existingTaskList.id),
                taskListRepository = repo,
                settingsResults = settingsResultBus.results,
                notificationScheduler = scheduler
            )

            // Wait for initial load to complete
            testScheduler.advanceUntilIdle()

            // Verify load was successful
            vm.uiState.value.isLoading shouldBe false
            vm.uiState.value.uiMainTasks shouldHaveSize 1

            // Clear any calls from initial load (load itself doesn't schedule,
            // but clear for safety)
            scheduler.scheduleCalls.clear()
            scheduler.cancelCalls.clear()
            repo.updateTaskListCalls.clear()

            // Emit a settings result that disables notifications for the task
            settingsResultBus.emit(
                TaskSettingsChanges(
                    mainTaskId = "task-A",
                    dueDate = "2027-06-01",
                    recurrence = "FREQ=DAILY",
                    isNotificationEnabled = false
                )
            )
            testScheduler.advanceUntilIdle()

            // 1. UiMainTask's isNotificationEnabled should be false
            val uiTask = vm.uiState.value.uiMainTasks.first { it.id == "task-A" }
            uiTask.isNotificationEnabled shouldBe false

            // 2. requestSync() was triggered — repository received an update call
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].id shouldBe "tl-integration-1"

            // 3. Notification scheduler receives cancel for that task
            // (scheduleTaskNotification sees isNotificationEnabled=false → calls cancel)
            // Notification scheduling happens on Dispatchers.Default (real thread pool)
            eventually(2.seconds) {
                scheduler.cancelCalls.any { it.taskId == "task-A" } shouldBe true
            }

            // schedule() should NOT have been called for this task (disabled notification)
            scheduler.scheduleCalls.filter { it.taskId == "task-A" } shouldHaveSize 0
        }
    }

    test("toggle reactivation via settingsResultBus triggers sync and schedules notification") {
        runTest(testDispatcher) {
            val repo = IntegrationTestRepository()
            val scheduler = IntegrationRecordingScheduler()
            val settingsResultBus = MainTaskSettingsResultBus()

            val existingTaskList = TaskList(
                id = "tl-integration-2",
                parentDir = "home",
                name = "Weekly review",
                tasks = listOf(
                    MainTask(
                        id = "task-B",
                        description = "Review goals",
                        isDone = false,
                        dueDate = "2027-06-01",
                        recurrence = "FREQ=WEEKLY",
                        isNotificationEnabled = false,
                        subTasks = emptyList()
                    )
                ),
                updatedAt = 1L,
                isAutoDelete = false
            )
            repo.addTaskList(existingTaskList)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existingTaskList.id),
                taskListRepository = repo,
                settingsResults = settingsResultBus.results,
                notificationScheduler = scheduler
            )

            // Wait for initial load
            testScheduler.advanceUntilIdle()
            vm.uiState.value.isLoading shouldBe false

            // Clear to isolate the reactivation flow
            scheduler.scheduleCalls.clear()
            scheduler.cancelCalls.clear()
            repo.updateTaskListCalls.clear()

            // Re-enable notifications
            settingsResultBus.emit(
                TaskSettingsChanges(
                    mainTaskId = "task-B",
                    dueDate = "2027-06-01",
                    recurrence = "FREQ=WEEKLY",
                    isNotificationEnabled = true
                )
            )
            testScheduler.advanceUntilIdle()

            // UiMainTask's isNotificationEnabled should be true
            val uiTask = vm.uiState.value.uiMainTasks.first { it.id == "task-B" }
            uiTask.isNotificationEnabled shouldBe true

            // Sync triggered
            repo.updateTaskListCalls shouldHaveSize 1

            // Notification should be scheduled (not canceled) — the task has
            // isNotificationEnabled=true, a valid dueDate, and recurrence
            eventually(2.seconds) {
                scheduler.scheduleCalls.any { it.taskId == "task-B" } shouldBe true
            }
        }
    }

    test("toggle deactivation with multiple tasks only cancels the affected task") {
        runTest(testDispatcher) {
            val repo = IntegrationTestRepository()
            val scheduler = IntegrationRecordingScheduler()
            val settingsResultBus = MainTaskSettingsResultBus()

            val existingTaskList = TaskList(
                id = "tl-integration-3",
                parentDir = "home",
                name = "Multi-task list",
                tasks = listOf(
                    MainTask(
                        id = "task-X",
                        description = "Task X",
                        isDone = false,
                        dueDate = "2027-06-01",
                        recurrence = "FREQ=DAILY",
                        isNotificationEnabled = true,
                        subTasks = emptyList()
                    ),
                    MainTask(
                        id = "task-Y",
                        description = "Task Y",
                        isDone = false,
                        dueDate = "2027-06-02",
                        recurrence = "FREQ=WEEKLY",
                        isNotificationEnabled = true,
                        subTasks = emptyList()
                    )
                ),
                updatedAt = 1L,
                isAutoDelete = false
            )
            repo.addTaskList(existingTaskList)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existingTaskList.id),
                taskListRepository = repo,
                settingsResults = settingsResultBus.results,
                notificationScheduler = scheduler
            )

            // Wait for initial load
            testScheduler.advanceUntilIdle()
            vm.uiState.value.isLoading shouldBe false
            vm.uiState.value.uiMainTasks shouldHaveSize 2

            // Clear to isolate the deactivation
            scheduler.scheduleCalls.clear()
            scheduler.cancelCalls.clear()
            repo.updateTaskListCalls.clear()

            // Disable notifications only for task-X
            settingsResultBus.emit(
                TaskSettingsChanges(
                    mainTaskId = "task-X",
                    dueDate = "2027-06-01",
                    recurrence = "FREQ=DAILY",
                    isNotificationEnabled = false
                )
            )
            testScheduler.advanceUntilIdle()

            // Sync triggered
            repo.updateTaskListCalls shouldHaveSize 1

            // Wait for notification calls to complete on Dispatchers.Default
            eventually(2.seconds) {
                // task-X should be canceled (isNotificationEnabled = false)
                scheduler.cancelCalls.any { it.taskId == "task-X" } shouldBe true
                // task-Y should be scheduled (still enabled with valid dueDate + recurrence)
                scheduler.scheduleCalls.any { it.taskId == "task-Y" } shouldBe true
            }

            // task-X should NOT have been scheduled
            scheduler.scheduleCalls.filter { it.taskId == "task-X" } shouldHaveSize 0
        }
    }
})

// --- Test doubles ---

private data class IntegrationScheduleCall(
    val taskId: String,
    val title: String,
    val body: String,
    val dueDateIso: String
)

private data class IntegrationCancelCall(val taskId: String)

private class IntegrationRecordingScheduler : NotificationScheduler {
    val scheduleCalls = mutableListOf<IntegrationScheduleCall>()
    val cancelCalls = mutableListOf<IntegrationCancelCall>()

    override suspend fun schedule(
        taskId: String,
        taskListId: String,
        title: String,
        body: String,
        dueDateIso: String
    ) {
        scheduleCalls.add(IntegrationScheduleCall(taskId, title, body, dueDateIso))
    }

    override suspend fun cancel(taskId: String) {
        cancelCalls.add(IntegrationCancelCall(taskId))
    }
}

private class IntegrationTestRepository : TaskListRepository {
    val createTaskListCalls = mutableListOf<CreateTaskListParams>()
    val updateTaskListCalls = mutableListOf<UpdateTaskListParams>()
    val deleteTaskListCalls = mutableListOf<String>()
    private val taskLists = mutableMapOf<String, TaskList>()
    private var nextCreatedId = 1

    fun addTaskList(taskList: TaskList) {
        taskLists[taskList.id] = taskList
    }

    override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> {
        createTaskListCalls.add(params)
        val created = TaskList(
            id = "created-${nextCreatedId++}",
            parentDir = params.parentDir,
            name = params.name,
            tasks = params.tasks,
            updatedAt = 0L,
            isAutoDelete = params.isAutoDelete
        )
        taskLists[created.id] = created
        return Result.success(created)
    }

    override suspend fun getTaskList(taskListId: String): Result<TaskList> {
        return taskLists[taskListId]?.let(Result.Companion::success)
            ?: Result.failure(NoSuchElementException("TaskList not found: $taskListId"))
    }

    override suspend fun getMainTask(mainTaskId: String): Result<MainTask> {
        val task = taskLists.values
            .flatMap { it.tasks }
            .firstOrNull { it.id == mainTaskId }
        return task?.let(Result.Companion::success)
            ?: Result.failure(NoSuchElementException("MainTask not found: $mainTaskId"))
    }

    override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> =
        Result.success(emptyList())

    override suspend fun listMainTasksSummary(parentDir: String): Result<List<MainTasksSummary>> =
        error("Task peek is not used by this test fixture")

    override suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>> =
        error("Upcoming tasks are not used by this test fixture")

    override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> {
        updateTaskListCalls.add(params)
        val existing = taskLists[params.id]
            ?: return Result.failure(NoSuchElementException("TaskList not found: ${params.id}"))

        val updated = existing.copy(
            name = params.title,
            tasks = params.tasks,
            updatedAt = existing.updatedAt + 1,
            isAutoDelete = params.isAutoDelete
        )
        taskLists[updated.id] = updated
        return Result.success(updated)
    }

    override suspend fun deleteTaskList(taskListId: String): Result<Unit> {
        deleteTaskListCalls.add(taskListId)
        taskLists.remove(taskListId)
        return Result.success(Unit)
    }
}