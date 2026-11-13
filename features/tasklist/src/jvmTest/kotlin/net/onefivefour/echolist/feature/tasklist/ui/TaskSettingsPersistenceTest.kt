package net.onefivefour.echolist.feature.tasklist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.MainTaskPeek
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import net.onefivefour.echolist.testutil.NoOpNotificationScheduler

@OptIn(ExperimentalCoroutinesApi::class)
internal class TaskSettingsPersistenceTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }
    class FakeTaskListRepository : TaskListRepository {
        val taskLists = mutableMapOf<String, TaskList>()

        fun addTaskList(taskList: TaskList) {
            taskLists[taskList.id] = taskList
        }

        override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> {
            val created = TaskList(
                id = "created-1",
                parentDir = params.parentDir,
                name = params.name,
                tasks = params.tasks,
                updatedAt = 1L,
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

        override suspend fun listMainTaskPeek(parentDir: String): Result<List<MainTaskPeek>> =
            error("Task peek is not used by this test fixture")

        override suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>> =
            error("Upcoming tasks are not used by this test fixture")

        override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> {
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
            taskLists.remove(taskListId)
            return Result.success(Unit)
        }
    }

    test("an empty draft retains settings until its description allows persistence") {
        runTest(dispatcher) {
            val repo = FakeTaskListRepository()
            val results = MainTaskSettingsResultBus()
            val editor =
                EditTaskListViewModel(
                    EditTaskListMode.Create("home"),
                    repo,
                    results.results,
                    NoOpNotificationScheduler()
                )
            editor.uiState.value.titleState.edit { replace(0, length, "Draft list") }
            val taskId = editor.onAddMainTask()
            editor.onSettingsNavigationStarted()
            editor.onScreenLeft()
            testScheduler.runCurrent()
            results.emit(TaskSettingsChanges(taskId, "2026-09-01", "", true))
            testScheduler.advanceUntilIdle()
            editor.uiState.value.uiMainTasks.single().dueDateState.text.toString() shouldBe "2026-09-01"
            repo.taskLists shouldBe emptyMap()
            editor.uiState.value.uiMainTasks.single().descriptionState.edit { replace(0, length, "Name the draft") }
            editor.onFieldFocusLost()
            testScheduler.advanceUntilIdle()
            repo.taskLists.values.single().tasks.single().dueDate shouldBe "2026-09-01"
        }
    }

    test("settings persist due date and recurrence and survive reopening the editor") {
        runTest(dispatcher) {
            val repo = FakeTaskListRepository()
            repo.addTaskList(
                TaskList(
                    "list",
                    "home",
                    "Garden",
                    listOf(
                        MainTask(
                            id = "task",
                            description = "Water plants",
                            isDone = false,
                            dueDate = "",
                            recurrence = "",
                            subTasks = emptyList()
                        )
                    ),
                    1L,
                    false
                )
            )
            val results = MainTaskSettingsResultBus()
            val editor =
                EditTaskListViewModel(EditTaskListMode.Edit("list"), repo, results.results, NoOpNotificationScheduler())
            testScheduler.advanceUntilIdle()
            results.emit(TaskSettingsChanges("task", "2026-08-01", "FREQ=WEEKLY;INTERVAL=2", true))
            testScheduler.advanceUntilIdle()
            val stored = repo.taskLists.getValue("list").tasks.single()
            stored.dueDate shouldBe "2026-08-01"
            stored.recurrence shouldBe "FREQ=WEEKLY;INTERVAL=2"
            val reopened =
                EditTaskListViewModel(
                    EditTaskListMode.Edit("list"),
                    repo,
                    MainTaskSettingsResultBus().results,
                    NoOpNotificationScheduler()
                )
            testScheduler.advanceUntilIdle()
            reopened.uiState.value.uiMainTasks.single().dueDateState.text.toString() shouldBe stored.dueDate
            reopened.uiState.value.uiMainTasks.single().recurrenceState.text.toString() shouldBe stored.recurrence
        }
    }
})