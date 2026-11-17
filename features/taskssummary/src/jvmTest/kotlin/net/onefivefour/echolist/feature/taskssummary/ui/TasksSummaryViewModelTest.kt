package net.onefivefour.echolist.feature.taskssummary.ui

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
import net.onefivefour.echolist.core.tasks.domain.model.MainTasksSummary
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository

@OptIn(ExperimentalCoroutinesApi::class)
class TasksSummaryViewModelTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    test("initialization loads, groups and hides done tasks for the current directory") {
        runTest(dispatcher) {
            val repository = FakeSummaryRepository(
                Result.success(
                    listOf(
                        MainTasksSummary("a", "A", false, LocalDate(2026, 1, 1), "l1", "List 1"),
                        MainTasksSummary("b", "B", true, null, "l1", "List 1")
                    )
                )
            )
            val viewModel = TasksSummaryViewModel(parentDir = "projects", taskListRepository = repository)

            advanceUntilIdle()

            val state = viewModel.uiState.first()
            state.isLoading shouldBe false
            repository.requestedParentDir shouldBe "projects"
            state.groups shouldBe listOf(
                TasksSummaryGroup("l1", "List 1", listOf(TasksSummaryRow("a", "A", false, LocalDate(2026, 1, 1))))
            )
        }
    }

    test("surfaces an error message on failure") {
        runTest(dispatcher) {
            val repository = FakeSummaryRepository(Result.failure(IllegalStateException("boom")))
            val viewModel = TasksSummaryViewModel(parentDir = "", taskListRepository = repository)

            advanceUntilIdle()

            val state = viewModel.uiState.first()
            state.isLoading shouldBe false
            state.error shouldBe "boom"
        }
    }
})

private class FakeSummaryRepository(
    private val result: Result<List<MainTasksSummary>>
) : TaskListRepository {
    var requestedParentDir: String? = null

    override suspend fun listMainTasksSummary(parentDir: String): Result<List<MainTasksSummary>> {
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