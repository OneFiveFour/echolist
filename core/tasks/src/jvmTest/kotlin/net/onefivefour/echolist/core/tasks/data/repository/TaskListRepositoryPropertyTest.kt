package net.onefivefour.echolist.core.tasks.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.PropTestConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.core.tasks.data.source.network.TaskListRemoteDataSource
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.SubTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import tasks.v1.CreateTaskListRequest
import tasks.v1.CreateTaskListResponse
import tasks.v1.DeleteTaskListRequest
import tasks.v1.DeleteTaskListResponse
import tasks.v1.GetMainTaskRequest
import tasks.v1.GetMainTaskResponse
import tasks.v1.GetTaskListRequest
import tasks.v1.GetTaskListResponse
import tasks.v1.ListTaskListsRequest
import tasks.v1.ListTaskListsResponse
import tasks.v1.UpdateTaskListRequest
import tasks.v1.UpdateTaskListResponse

/**
 * Feature: proto-api-update
 * Property 7: TaskListRepository error propagation
 *
 * Validates: Requirements 12.7
 */
class TaskListRepositoryPropertyTest : FunSpec({

    class NoOpDirectoryChangeNotifier : DirectoryChangeNotifier {
        override val directoryChanged = MutableSharedFlow<String>()

        override suspend fun notifyChanged(path: String) = Unit
    }

    fun newRepo(dataSource: TaskListRemoteDataSource): TaskListRepositoryImpl =
        TaskListRepositoryImpl(
            networkDataSource = dataSource,
            dispatcher = Dispatchers.Unconfined,
            directoryChangeNotifier = NoOpDirectoryChangeNotifier()
        )

    // -- Generators --

    val arbDomainSubTask = arbitrary {
        SubTask(
            id = Arb.string(0..50).bind(),
            description = Arb.string(0..100).bind(),
            isDone = Arb.boolean().bind()
        )
    }

    val arbDomainMainTask = arbitrary {
        MainTask(
            id = Arb.string(0..50).bind(),
            description = Arb.string(0..100).bind(),
            isDone = Arb.boolean().bind(),
            dueDate = Arb.string(0..50).bind(),
            recurrence = Arb.string(0..50).bind(),
            subTasks = Arb.list(arbDomainSubTask, 0..3).bind()
        )
    }

    val arbCreateTaskListParams = arbitrary {
        CreateTaskListParams(
            name = Arb.string(1..100).bind(),
            parentDir = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..3).bind(),
            isAutoDelete = Arb.boolean().bind()
        )
    }

    val arbUpdateTaskListParams = arbitrary {
        UpdateTaskListParams(
            id = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            tasks = Arb.list(arbDomainMainTask, 0..3).bind(),
            isAutoDelete = Arb.boolean().bind()
        )
    }

    val arbException = arbitrary {
        RuntimeException(Arb.string(1..200).bind())
    }

    // -- Fake data source that always throws --

    class ThrowingDataSource(private val exception: Exception) : TaskListRemoteDataSource {
        override suspend fun createTaskList(request: CreateTaskListRequest): CreateTaskListResponse =
            throw exception
        override suspend fun getTaskList(request: GetTaskListRequest): GetTaskListResponse =
            throw exception
        override suspend fun getMainTask(request: GetMainTaskRequest): GetMainTaskResponse =
            throw exception
        override suspend fun listTaskLists(request: ListTaskListsRequest): ListTaskListsResponse =
            throw exception
        override suspend fun listMainTaskPeek(
            request: tasks.v1.ListMainTaskPeekRequest
        ): tasks.v1.ListMainTaskPeekResponse = throw exception
        override suspend fun listUpcomingMainTasks(
            request: tasks.v1.ListUpcomingMainTasksRequest
        ): tasks.v1.ListUpcomingMainTasksResponse = throw exception
        override suspend fun updateTaskList(request: UpdateTaskListRequest): UpdateTaskListResponse =
            throw exception
        override suspend fun deleteTaskList(request: DeleteTaskListRequest): DeleteTaskListResponse =
            throw exception
    }

    // -- Property 7: Error propagation --

    test("Feature: proto-api-update, Property 7: createTaskList propagates exception as Result.failure") {
        checkAll(PropTestConfig(iterations = 100), arbCreateTaskListParams, arbException) { params, ex ->
            val repo = newRepo(ThrowingDataSource(ex))
            val result = repo.createTaskList(params)
            result.shouldBeFailure { it shouldBe ex }
        }
    }

    test("Feature: proto-api-update, Property 7: getTaskList propagates exception as Result.failure") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100), arbException) { taskListId, ex ->
            val repo = newRepo(ThrowingDataSource(ex))
            val result = repo.getTaskList(taskListId)
            result.shouldBeFailure { it shouldBe ex }
        }
    }

    test("Feature: proto-api-update, Property 7: listTaskLists propagates exception as Result.failure") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100), arbException) { path, ex ->
            val repo = newRepo(ThrowingDataSource(ex))
            val result = repo.listTaskLists(path)
            result.shouldBeFailure { it shouldBe ex }
        }
    }

    test("Feature: proto-api-update, Property 7: listMainTaskPeek propagates exception as Result.failure") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100), arbException) { path, ex ->
            val repo = newRepo(ThrowingDataSource(ex))
            val result = repo.listMainTaskPeek(path)
            result.shouldBeFailure { it shouldBe ex }
        }
    }

    test("Feature: proto-api-update, Property 7: updateTaskList propagates exception as Result.failure") {
        checkAll(PropTestConfig(iterations = 100), arbUpdateTaskListParams, arbException) { params, ex ->
            val repo = newRepo(ThrowingDataSource(ex))
            val result = repo.updateTaskList(params)
            result.shouldBeFailure { it shouldBe ex }
        }
    }

    test("Feature: proto-api-update, Property 7: deleteTaskList propagates exception as Result.failure") {
        checkAll(PropTestConfig(iterations = 100), Arb.string(1..100), arbException) { taskListId, ex ->
            val repo = newRepo(ThrowingDataSource(ex))
            val result = repo.deleteTaskList(taskListId)
            result.shouldBeFailure { it shouldBe ex }
        }
    }
})