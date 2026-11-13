package net.onefivefour.echolist.feature.browser.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.MainTaskPeek
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository

@OptIn(ExperimentalCoroutinesApi::class)
class TaskPeekViewModelTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    test("toggle loads, groups and hides done tasks for the current directory") {
        runTest(dispatcher) {
            val repository = FakePeekRepository(
                Result.success(
                    listOf(
                        MainTaskPeek("a", "A", false, LocalDate(2026, 1, 1), "l1", "List 1"),
                        MainTaskPeek("b", "B", true, null, "l1", "List 1")
                    )
                )
            )
            val viewModel = TaskPeekViewModel(parentDir = "projects", taskListRepository = repository)

            viewModel.toggle()
            advanceUntilIdle()

            val state = viewModel.uiState.first()
            state.isVisible shouldBe true
            state.isLoading shouldBe false
            repository.requestedParentDir shouldBe "projects"
            state.groups shouldBe listOf(
                TaskPeekGroup("l1", "List 1", listOf(TaskPeekRow("a", "A", false, LocalDate(2026, 1, 1))))
            )
        }
    }

    test("toggling again hides the peek view and clears state") {
        runTest(dispatcher) {
            val repository = FakePeekRepository(Result.success(emptyList()))
            val viewModel = TaskPeekViewModel(parentDir = "", taskListRepository = repository)

            viewModel.toggle()
            advanceUntilIdle()
            viewModel.toggle()

            viewModel.uiState.first().isVisible shouldBe false
        }
    }

    test("surfaces an error message on failure") {
        runTest(dispatcher) {
            val repository = FakePeekRepository(Result.failure(IllegalStateException("boom")))
            val viewModel = TaskPeekViewModel(parentDir = "", taskListRepository = repository)

            viewModel.toggle()
            advanceUntilIdle()

            val state = viewModel.uiState.first()
            state.isLoading shouldBe false
            state.error shouldBe "boom"
        }
    }
})

private class FakePeekRepository(
    private val result: Result<List<MainTaskPeek>>
) : TaskListRepository {
    var requestedParentDir: String? = null

    override suspend fun listMainTaskPeek(parentDir: String): Result<List<MainTaskPeek>> {
        requestedParentDir = parentDir
        return result
    }

    override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> = error("unused")
    override suspend fun getTaskList(taskListId: String): Result<TaskList> = error("unused")
    override suspend fun getMainTask(mainTaskId: String): Result<MainTask> = error("unused")
    override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> = error("unused")
    override suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>> = error("unused")
    override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> = error("unused")
    override suspend fun deleteTaskList(taskListId: String): Result<Unit> = error("unused")
}