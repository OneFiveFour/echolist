package net.onefivefour.echolist.feature.tasklist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.MainTaskPeek
import net.onefivefour.echolist.core.tasks.domain.model.SubTask
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import net.onefivefour.echolist.testutil.NoOpNotificationScheduler

@OptIn(ExperimentalCoroutinesApi::class)
internal class EditTaskListViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    class FakeTaskListRepository : TaskListRepository {
        val createTaskListCalls = mutableListOf<CreateTaskListParams>()
        val updateTaskListCalls = mutableListOf<UpdateTaskListParams>()
        val deleteTaskListCalls = mutableListOf<String>()
        val getTaskListCalls = mutableListOf<String>()
        val taskLists = mutableMapOf<String, TaskList>()
        var nextCreatedId = 1
        var blockNextUpdate: CompletableDeferred<Unit>? = null
        var updateTaskListTransform: (TaskList) -> TaskList = { it }

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

        override suspend fun listMainTaskPeek(parentDir: String): Result<List<MainTaskPeek>> =
            error("Task peek is not used by this test fixture")

        override suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>> =
            error("Upcoming tasks are not used by this test fixture")

        override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> {
            updateTaskListCalls.add(params)
            blockNextUpdate?.let { deferred ->
                deferred.await()
                if (blockNextUpdate === deferred) {
                    blockNextUpdate = null
                }
            }

            val existing = taskLists[params.id]
                ?: return Result.failure(NoSuchElementException("TaskList not found: ${params.id}"))

            val updated = updateTaskListTransform(
                existing.copy(
                    name = params.title,
                    tasks = params.tasks,
                    updatedAt = existing.updatedAt + 1,
                    isAutoDelete = params.isAutoDelete
                )
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

    fun taskList(
        id: String,
        name: String = "Existing list",
        tasks: List<MainTask> = listOf(
            MainTask(
                id = "task-1",
                description = "Existing task",
                isDone = false,
                dueDate = "",
                recurrence = "",
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

    test("create mode auto-creates on the first valid blur and becomes persisted") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Sprint plan") }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 0

            vm.onAddMainTask()
            vm.uiState.value.uiMainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release")
            }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls shouldHaveSize 0
            repo.createTaskListCalls[0].name shouldBe "Sprint plan"
            repo.createTaskListCalls[0].tasks.single().description shouldBe "Plan release"
            vm.uiState.value.isPersisted shouldBe true
        }
    }

    test("later blur after auto-create updates instead of creating again") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Sprint plan") }
            vm.onAddMainTask()
            vm.uiState.value.uiMainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release")
            }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release v2")
            }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].title shouldBe "Sprint plan"
            repo.updateTaskListCalls[0].tasks.single().description shouldBe "Plan release v2"
        }
    }

    test("title blur updates an existing task list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-1", name = "Trip prep")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v2") }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].title shouldBe "Trip prep v2"
        }
    }

    test("main task checkbox changes sync immediately") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-check")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.onMainTaskCheckedChange(0, true)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.single().isDone shouldBe true
        }
    }

    test("manual delete syncs immediately") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-delete-row",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Task 1",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = emptyList()
                    ),
                    MainTask(
                        id = "t2",
                        description = "Task 2",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.onRemoveMainTask(1)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.map { it.description } shouldBe listOf("Task 1")
        }
    }

    test("auto-delete removes completed main tasks locally and syncs the reduced list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-auto-main",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Task 1",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = emptyList()
                    ),
                    MainTask(
                        id = "t2",
                        description = "Task 2",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = emptyList()
                    )
                ),
                isAutoDelete = true
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.onMainTaskCheckedChange(0, true)
            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks.map { it.descriptionState.text.toString() } shouldBe listOf("Task 2")
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.map { it.description } shouldBe listOf("Task 2")
        }
    }

    test("auto-delete keeps recurring main tasks and applies the renewed due date") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-auto-recurring",
                tasks = listOf(
                    MainTask(
                        id = "recurring",
                        description = "Recurring task",
                        isDone = false,
                        dueDate = "2026-04-01",
                        recurrence = "FREQ=WEEKLY",
                        subTasks = emptyList()
                    )
                ),
                isAutoDelete = true
            )
            repo.addTaskList(existing)
            repo.updateTaskListTransform = { updated ->
                updated.copy(
                    tasks = updated.tasks.map { task ->
                        task.copy(isDone = false, dueDate = "2026-04-08")
                    }
                )
            }

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.onMainTaskCheckedChange(0, true)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls.single().tasks.single().isDone shouldBe true
            vm.uiState.value.uiMainTasks shouldHaveSize 1
            vm.uiState.value.uiMainTasks.single().isDone shouldBe false
            vm.uiState.value.uiMainTasks.single().dueDateState.text.toString() shouldBe "2026-04-08"
        }
    }

    test("auto-delete removes completed subtasks locally and syncs the reduced list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-auto-sub",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Parent",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = listOf(
                            SubTask(id = "s1", description = "Sub 1", isDone = false),
                            SubTask(id = "s2", description = "Sub 2", isDone = false)
                        )
                    )
                ),
                isAutoDelete = true
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.onSubTaskCheckedChange(0, 0, true)
            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks[0].subTasks.map { it.descriptionState.text.toString() } shouldBe
                listOf("Sub 2")
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks[0].subTasks.map { it.description } shouldBe listOf("Sub 2")
        }
    }

    test("auto-delete toggle syncs immediately") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-toggle", isAutoDelete = false)
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.onToggleAutoDelete(true)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].isAutoDelete shouldBe true
        }
    }

    test("invalid due date blocks sync and preserves the validation error") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Sprint plan") }
            vm.onAddMainTask()
            vm.uiState.value.uiMainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release")
            }
            vm.uiState.value.uiMainTasks[0].dueDateState.edit {
                replace(0, length, "2026/04/01")
            }

            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 0
            repo.updateTaskListCalls shouldHaveSize 0
            vm.uiState.value.error shouldBe "Due date must use YYYY-MM-DD."
        }
    }

    test("selecting a due date in create mode immediately creates the task list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val settingsFlow = MainTaskSettingsResultBus()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo,
                settingsResults = settingsFlow.results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            vm.uiState.value.titleState.edit { replace(0, length, "Sprint plan") }
            vm.onAddMainTask()
            val taskId = vm.uiState.value.uiMainTasks[0].id
            vm.uiState.value.uiMainTasks[0].descriptionState.edit {
                replace(0, length, "Plan release")
            }

            testScheduler.advanceUntilIdle()
            settingsFlow.emit(TaskSettingsChanges(mainTaskId = taskId, dueDate = "2026-04-01", recurrence = ""))
            testScheduler.advanceUntilIdle()

            repo.createTaskListCalls shouldHaveSize 1
            repo.createTaskListCalls[0].tasks.single().dueDate shouldBe "2026-04-01"
        }
    }

    test("selecting a due date on an existing task list sends an update") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-due-date")
            repo.addTaskList(existing)

            val settingsFlow = MainTaskSettingsResultBus()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = settingsFlow.results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            val taskId = vm.uiState.value.uiMainTasks[0].id
            settingsFlow.emit(TaskSettingsChanges(mainTaskId = taskId, dueDate = "2026-04-01", recurrence = ""))
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.single().dueDate shouldBe "2026-04-01"
        }
    }

    test("completed main tasks and subtasks are sorted after incomplete tasks") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-sort-completed",
                tasks = listOf(
                    MainTask(
                        id = "completed",
                        description = "Completed",
                        isDone = true,
                        dueDate = "2026-04-01",
                        recurrence = "",
                        subTasks = listOf(
                            SubTask(id = "done-sub", description = "Done subtask", isDone = true),
                            SubTask(id = "open-sub", description = "Open subtask", isDone = false),
                            SubTask(id = "other-open-sub", description = "Other open subtask", isDone = false)
                        )
                    ),
                    MainTask(
                        id = "active",
                        description = "Active",
                        isDone = false,
                        dueDate = "2026-05-01",
                        recurrence = "",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks.map { it.id } shouldBe listOf("active", "completed")
            vm.uiState.value.uiMainTasks[1].subTasks.map { it.id } shouldBe
                listOf("open-sub", "other-open-sub", "done-sub")

            vm.onMainTaskCheckedChange(0, true)
            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks.map { it.id } shouldBe listOf("completed", "active")
            repo.updateTaskListCalls.last().tasks.map { it.id } shouldBe listOf("completed", "active")

            vm.onSubTaskCheckedChange(0, 0, true)
            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks[0].subTasks.map { it.id } shouldBe
                listOf("other-open-sub", "open-sub", "done-sub")
            repo.updateTaskListCalls.last().tasks[0].subTasks.map {
                it.id
            } shouldBe listOf("other-open-sub", "open-sub", "done-sub")
        }
    }

    test("main tasks are ordered by ascending due date with undated tasks last") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-sort-due-date",
                tasks = listOf(
                    MainTask(
                        id = "late",
                        description = "Late",
                        isDone = false,
                        dueDate = "2026-05-10",
                        recurrence = "",
                        subTasks = emptyList()
                    ),
                    MainTask(
                        id = "none",
                        description = "No date",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = emptyList()
                    ),
                    MainTask(
                        id = "early",
                        description = "Early",
                        isDone = false,
                        dueDate = "2026-04-01",
                        recurrence = "",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks.map { it.id } shouldBe listOf("early", "late", "none")

            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 0

            val settingsFlow = MainTaskSettingsResultBus()
            val sortingVm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = settingsFlow.results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            settingsFlow.emit(TaskSettingsChanges(mainTaskId = "none", dueDate = "2026-03-01", recurrence = ""))
            testScheduler.advanceUntilIdle()

            sortingVm.uiState.value.uiMainTasks.map { it.id } shouldBe listOf("none", "early", "late")
            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls.single().tasks.map { it.id } shouldBe listOf("none", "early", "late")
        }
    }

    test("selecting a due date clears recurrence before syncing") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-recurrence",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Recurring task",
                        isDone = false,
                        dueDate = "",
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
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            val taskId = vm.uiState.value.uiMainTasks[0].id
            settingsFlow.emit(TaskSettingsChanges(mainTaskId = taskId, dueDate = "2026-05-10", recurrence = ""))
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
            repo.updateTaskListCalls[0].tasks.single().dueDate shouldBe "2026-05-10"
            repo.updateTaskListCalls[0].tasks.single().recurrence shouldBe ""
        }
    }

    test("selecting the same due date again does not send a duplicate request") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-same-date",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Task",
                        isDone = false,
                        dueDate = "2026-04-01",
                        recurrence = "",
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
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            val taskId = vm.uiState.value.uiMainTasks[0].id
            settingsFlow.emit(TaskSettingsChanges(mainTaskId = taskId, dueDate = "2026-04-01", recurrence = ""))
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 0
        }
    }

    test("repeated blur without changes does not send duplicate requests") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-dedupe", name = "Trip prep")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v2") }
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 1
        }
    }

    test("queued edits run after an in-flight sync finishes") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-queue", name = "Trip prep")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            val gate = CompletableDeferred<Unit>()
            repo.blockNextUpdate = gate

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v2") }
            vm.onFieldFocusLost()
            testScheduler.runCurrent()

            vm.uiState.value.titleState.edit { replace(0, length, "Trip prep v3") }
            vm.onFieldFocusLost()

            gate.complete(Unit)
            testScheduler.advanceUntilIdle()

            repo.updateTaskListCalls shouldHaveSize 2
            repo.updateTaskListCalls.last().title shouldBe "Trip prep v3"
        }
    }

    test("empty main tasks are stripped from the UI on screen left") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-strip-empty",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Real task",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            // Simulate pressing Enter which adds an empty main task
            vm.onAddMainTask()
            vm.uiState.value.uiMainTasks shouldHaveSize 2
            vm.uiState.value.uiMainTasks[1].descriptionState.text.toString() shouldBe ""

            // Simulate navigating away (screen left triggers cleanup)
            vm.onScreenLeft()
            testScheduler.advanceUntilIdle()

            // The empty task should be removed from the UI
            vm.uiState.value.uiMainTasks shouldHaveSize 1
            vm.uiState.value.uiMainTasks[0].descriptionState.text.toString() shouldBe "Real task"

            // No update sent since effective content hasn't changed
            repo.updateTaskListCalls shouldHaveSize 0
        }
    }

    test("empty main task is preserved while navigating to settings and stripped on actual exit") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            vm.uiState.value.titleState.edit { replace(0, length, "New list") }
            vm.onAddMainTask()
            val draftId = vm.uiState.value.uiMainTasks.single().id

            vm.onSettingsNavigationStarted()
            vm.onScreenLeft()
            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks.single().id shouldBe draftId
            repo.createTaskListCalls shouldHaveSize 0

            vm.onScreenLeft()
            testScheduler.advanceUntilIdle()

            vm.uiState.value.uiMainTasks shouldHaveSize 0
            repo.createTaskListCalls shouldHaveSize 0
        }
    }

    test("empty sub tasks are stripped from the UI on screen left") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-strip-empty-sub",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Parent task",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = listOf(
                            SubTask(id = "s1", description = "Real sub", isDone = false)
                        )
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            // Simulate adding an empty sub task
            vm.onAddSubTask(0)
            vm.uiState.value.uiMainTasks[0].subTasks shouldHaveSize 2
            vm.uiState.value.uiMainTasks[0].subTasks[1].descriptionState.text.toString() shouldBe ""

            // Simulate navigating away
            vm.onScreenLeft()
            testScheduler.advanceUntilIdle()

            // The empty sub task should be removed from the UI
            vm.uiState.value.uiMainTasks[0].subTasks shouldHaveSize 1
            vm.uiState.value.uiMainTasks[0].subTasks[0].descriptionState.text.toString() shouldBe "Real sub"

            // No update sent since the effective content hasn't changed
            repo.updateTaskListCalls shouldHaveSize 0
        }
    }

    test("empty main tasks in create mode are not persisted on screen left") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Create("home"),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            vm.uiState.value.titleState.edit { replace(0, length, "New list") }

            // Add a real task and an empty task
            vm.onAddMainTask()
            vm.uiState.value.uiMainTasks[0].descriptionState.edit {
                replace(0, length, "Do something")
            }
            vm.onAddMainTask() // empty task added by pressing Enter

            vm.uiState.value.uiMainTasks shouldHaveSize 2

            // Trigger screen left (e.g. navigating away)
            vm.onScreenLeft()
            testScheduler.advanceUntilIdle()

            // Empty task should be stripped from UI
            vm.uiState.value.uiMainTasks shouldHaveSize 1

            // Only the real task should be persisted
            repo.createTaskListCalls shouldHaveSize 1
            repo.createTaskListCalls[0].tasks shouldHaveSize 1
            repo.createTaskListCalls[0].tasks[0].description shouldBe "Do something"
        }
    }

    test("adding an empty main task does not get stripped by regular sync") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(
                id = "task-list-keep-empty",
                tasks = listOf(
                    MainTask(
                        id = "t1",
                        description = "Real task",
                        isDone = false,
                        dueDate = "",
                        recurrence = "",
                        subTasks = emptyList()
                    )
                )
            )
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            // Simulate pressing Enter which adds an empty main task
            vm.onAddMainTask()
            vm.uiState.value.uiMainTasks shouldHaveSize 2

            // Regular focus lost should NOT strip the empty task (user is still editing)
            vm.onFieldFocusLost()
            testScheduler.advanceUntilIdle()

            // Empty task should still be in the UI
            vm.uiState.value.uiMainTasks shouldHaveSize 2
        }
    }

    test("delete emits navigate-back for a persisted task list") {
        runTest(testDispatcher) {
            val repo = FakeTaskListRepository()
            val existing = taskList(id = "task-list-delete")
            repo.addTaskList(existing)

            val vm = EditTaskListViewModel(
                mode = EditTaskListMode.Edit(existing.id),
                taskListRepository = repo,
                settingsResults = MainTaskSettingsResultBus().results,
                notificationScheduler = NoOpNotificationScheduler()
            )

            testScheduler.advanceUntilIdle()

            val navigateBackDeferred = async { vm.navigateBack.first() }

            vm.onDeleteClick()
            testScheduler.advanceUntilIdle()

            navigateBackDeferred.await() shouldBe Unit
            repo.deleteTaskListCalls shouldBe listOf(existing.id)
        }
    }
})