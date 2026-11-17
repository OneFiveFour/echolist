package net.onefivefour.echolist.feature.tasklist.ui

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4
 *
 * Unit tests verifying that the EditTaskListViewModel correctly schedules and cancels
 * notifications through the injected NotificationScheduler.
 *
 * The ViewModel launches notification coroutines on Dispatchers.Default. Since test
 * dispatchers can't control the real Default pool, we use eventually{} with a wall-clock
 * timeout to synchronize with the async notification coroutines.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class EditTaskListViewModelNotificationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    fun taskList(
        id: String,
        name: String = "Grocery list",
        tasks: List<MainTask> = listOf(
            MainTask(
                id = "task-1",
                description = "Buy milk",
                isDone = false,
                dueDate = "2027-06-01",
                recurrence = "FREQ=WEEKLY",
                subTasks = emptyList()
            )
        ),
        isAutoDelete: Boolean = false
    ): TaskList = TaskList(
        id = id,
        parentDir = "home",
        name = name,
        tasks = tasks,
        updatedAt = 1L,
        isAutoDelete = isAutoDelete
    )

    // --- Test: Saving task with recurrence triggers schedule ---

    test("saving a task with recurrence and future due date triggers schedule") {
        runTest(testDispatcher) {
            val repo = FakeNotificationTestRepository()
            val scheduler = RecordingNotificationScheduler()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = scheduler
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Weekly shopping") }
            vm.onAddMainTask()
            vm.uiState.value.uiMainTasks[0].descriptionState.edit {
                replace(0, length, "Buy groceries")
            }
            vm.uiState.value.uiMainTasks[0].dueDateState.edit {
                replace(0, length, "2027-06-01")
            }
            vm.uiState.value.uiMainTasks[0].recurrenceState.edit {
                replace(0, length, "FREQ=WEEKLY")
            }

            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 1

            // Notification scheduling happens on Dispatchers.Default (real thread pool)
            eventually(2.seconds) {
                scheduler.scheduleCalls shouldHaveSize 1
            }

            val call = scheduler.scheduleCalls[0]
            call.taskId shouldBe repo.createTaskListCalls[0].tasks[0].id
            call.title shouldBe "Task due: Weekly shopping"
            call.body shouldBe "Buy groceries"
            call.dueDateIso shouldBe "2027-06-01"
        }
    }

    // --- Test: Deleting task list triggers cancel for all tasks ---

    test("deleting a task list triggers cancel for each task") {
        runTest(testDispatcher) {
            val repo = FakeNotificationTestRepository()
            val scheduler = RecordingNotificationScheduler()
            val existing = taskList(
                id = "tl-delete",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Task 1",
                        isDone = false,
                        dueDate = "2027-06-01",
                        recurrence = "FREQ=DAILY",
                        subTasks = emptyList()
                    ),
                    MainTask(
                        id = "t2",
                        description = "Task 2",
                        isDone = false,
                        dueDate = "2027-06-02",
                        recurrence = "FREQ=WEEKLY",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = scheduler
            )

            testScheduler.advanceUntilIdle()

            val navigateBackDeferred = async { vm.navigateBack.first() }
            vm.onDeleteClick()
            testScheduler.advanceUntilIdle()

            navigateBackDeferred.await() shouldBe Unit
            repo.deleteTaskListCalls shouldBe listOf("tl-delete")

            // Cancel happens on Dispatchers.Default — wait for it
            eventually(2.seconds) {
                scheduler.cancelCalls.map { it.taskId }.toSet() shouldBe setOf("t1", "t2")
            }
        }
    }

    // --- Test: Removing a main task triggers cancel ---

    test("removing a main task triggers cancel for that task ID") {
        runTest(testDispatcher) {
            val repo = FakeNotificationTestRepository()
            val scheduler = RecordingNotificationScheduler()
            val existing = taskList(
                id = "tl-remove",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Task 1",
                        isDone = false,
                        dueDate = "2027-06-01",
                        recurrence = "FREQ=DAILY",
                        subTasks = emptyList()
                    ),
                    MainTask(
                        id = "t2",
                        description = "Task 2",
                        isDone = false,
                        dueDate = "2027-06-02",
                        recurrence = "FREQ=WEEKLY",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = scheduler
            )

            testScheduler.advanceUntilIdle()

            // Clear any schedule/cancel calls from initial load
            scheduler.cancelCalls.clear()
            scheduler.scheduleCalls.clear()

            vm.onRemoveMainTask(0)
            testScheduler.advanceUntilIdle()

            // Cancel happens on Dispatchers.Default — wait for it
            eventually(2.seconds) {
                scheduler.cancelCalls.any { it.taskId == "t1" } shouldBe true
            }
        }
    }

    // --- Test: Removing recurrence preserves the optional reminder ---

    test("removing recurrence preserves scheduling for a dated one-time task") {
        runTest(testDispatcher) {
            val repo = FakeNotificationTestRepository()
            val scheduler = RecordingNotificationScheduler()
            val existing = taskList(
                id = "tl-recurrence-remove",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Recurring task",
                        isDone = false,
                        dueDate = "2027-06-01",
                        recurrence = "FREQ=WEEKLY",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val settingsFlow = MainTaskSettingsResultBus()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = settingsFlow.results,
                notificationScheduler = scheduler
            )

            testScheduler.advanceUntilIdle()

            // Clear schedule/cancel calls from initial load
            scheduler.scheduleCalls.clear()
            scheduler.cancelCalls.clear()

            // Remove recurrence by emitting settings result with empty recurrence
            settingsFlow.emit(TaskSettingsChanges(mainTaskId = "t1", dueDate = "2027-06-01", recurrence = ""))
            testScheduler.advanceUntilIdle()

            // A due date remains sufficient after recurrence is removed.
            eventually(2.seconds) {
                scheduler.scheduleCalls.any { it.taskId == "t1" } shouldBe true
            }
        }
    }

    // --- Property 6: Notification idempotency ---

    test("Property 6: scheduling the same task twice uses the same taskId (replacement semantics)") {
        /**
         * **Validates: Requirements 4.2**
         *
         * Property 6: Notification idempotency — scheduling a notification twice with
         * the same or different due dates results in exactly one pending notification
         * for that taskId (the most recent scheduling replaces the previous).
         *
         * We verify that after two syncs with the same task, all schedule() calls
         * reference the same taskId, demonstrating that the real scheduler (which
         * replaces by ID) would only keep the latest.
         */
        runTest(testDispatcher) {
            val repo = FakeNotificationTestRepository()
            val scheduler = RecordingNotificationScheduler()
            val existing = taskList(
                id = "tl-idempotent",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Recurring task",
                        isDone = false,
                        dueDate = "2027-06-01",
                        recurrence = "FREQ=WEEKLY",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val settingsFlow = MainTaskSettingsResultBus()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = settingsFlow.results,
                notificationScheduler = scheduler
            )

            testScheduler.advanceUntilIdle()
            scheduler.scheduleCalls.clear()

            // First sync: update due date
            settingsFlow.emit(
                TaskSettingsChanges(mainTaskId = "t1", dueDate = "2027-07-01", recurrence = "FREQ=WEEKLY")
            )
            testScheduler.advanceUntilIdle()

            // Wait for first schedule call
            eventually(2.seconds) {
                scheduler.scheduleCalls.size shouldBe 1
            }

            // Second sync: update due date again
            settingsFlow.emit(
                TaskSettingsChanges(mainTaskId = "t1", dueDate = "2027-08-01", recurrence = "FREQ=WEEKLY")
            )
            testScheduler.advanceUntilIdle()

            // Wait for second schedule call
            eventually(2.seconds) {
                scheduler.scheduleCalls.size shouldBe 2
            }

            // Both schedule calls should reference the same taskId
            scheduler.scheduleCalls.all { it.taskId == "t1" } shouldBe true

            // The second call has the latest due date (replacement semantics)
            scheduler.scheduleCalls.last().dueDateIso shouldBe "2027-08-01"
        }
    }

    // --- Property 6 (property-based): Notification idempotency across many inputs ---

    test("Property 6 (property-based): repeated syncs always schedule with the same taskId") {
        /**
         * **Validates: Requirements 4.2**
         *
         * For any task with a stable ID, scheduling N times (via N syncs with different
         * due dates) produces N schedule calls all referencing the same taskId.
         */
        val arbMonth = Arb.int(1..12)
        val arbDay = Arb.int(1..28)

        checkAll(10, arbMonth, arbDay) { month, day ->
            runTest(testDispatcher) {
                val repo = FakeNotificationTestRepository()
                val scheduler = RecordingNotificationScheduler()
                val dueDate1 = "2028-%02d-%02d".format(month, day)
                val dueDate2 = "2029-%02d-%02d".format(month, day)

                val existing = TaskList(
                    id = "tl-prop6",
                    parentDir = "home",
                    name = "Prop test list",
                    tasks = listOf(
                        MainTask(
                            id = "stable-task-id",
                            description = "A recurring task",
                            isDone = false,
                            dueDate = dueDate1,
                            recurrence = "FREQ=MONTHLY",
                            subTasks = emptyList()
                        )
                    ),
                    updatedAt = 1L,
                    isAutoDelete = false
                )
                repo.addTaskList(existing)

                val settingsFlow = MainTaskSettingsResultBus()
                val vm = EditTaskListViewModel(
                    mode = EditTaskListMode.Edit(existing.id),
                    taskListRepository = repo,
                    settingsResults = settingsFlow.results,
                    notificationScheduler = scheduler
                )

                testScheduler.advanceUntilIdle()
                scheduler.scheduleCalls.clear()

                // Update due date to trigger a sync
                settingsFlow.emit(
                    TaskSettingsChanges(mainTaskId = "stable-task-id", dueDate = dueDate2, recurrence = "FREQ=MONTHLY")
                )
                testScheduler.advanceUntilIdle()

                // Wait for Dispatchers.Default notification coroutine
                eventually(2.seconds) {
                    scheduler.scheduleCalls.isNotEmpty() shouldBe true
                }

                // All schedule calls for this task should use the same ID
                scheduler.scheduleCalls
                    .all { it.taskId == "stable-task-id" } shouldBe true
            }
        }
    }
})

// --- Test doubles defined at file level to avoid nested data class restrictions ---

private data class ScheduleCallRecord(
    val taskId: String,
    val title: String,
    val body: String,
    val dueDateIso: String
)

private data class CancelCallRecord(val taskId: String)

/**
 * Recording implementation of [NotificationScheduler] that captures all schedule/cancel calls.
 */
private class RecordingNotificationScheduler : NotificationScheduler {
    val scheduleCalls = mutableListOf<ScheduleCallRecord>()
    val cancelCalls = mutableListOf<CancelCallRecord>()

    override suspend fun schedule(
        taskId: String,
        taskListId: String,
        title: String,
        body: String,
        dueDateIso: String
    ) {
        scheduleCalls.add(ScheduleCallRecord(taskId, title, body, dueDateIso))
    }

    override suspend fun cancel(taskId: String) {
        cancelCalls.add(CancelCallRecord(taskId))
    }
}

/**
 * Fake task list repository for notification tests.
 */
private class FakeNotificationTestRepository : TaskListRepository {
    val createTaskListCalls = mutableListOf<CreateTaskListParams>()
    val updateTaskListCalls = mutableListOf<UpdateTaskListParams>()
    val deleteTaskListCalls = mutableListOf<String>()
    val getTaskListCalls = mutableListOf<String>()
    val taskLists = mutableMapOf<String, TaskList>()
    var nextCreatedId = 1

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
        getTaskListCalls.add(taskListId)
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