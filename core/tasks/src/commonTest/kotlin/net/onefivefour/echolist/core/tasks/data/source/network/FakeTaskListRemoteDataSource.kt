package net.onefivefour.echolist.core.tasks.data.source.network

import tasks.v1.CreateTaskListRequest
import tasks.v1.CreateTaskListResponse
import tasks.v1.DeleteTaskListRequest
import tasks.v1.DeleteTaskListResponse
import tasks.v1.GetMainTaskRequest
import tasks.v1.GetMainTaskResponse
import tasks.v1.GetTaskListRequest
import tasks.v1.GetTaskListResponse
import tasks.v1.ListMainTaskPeekReqeust
import tasks.v1.ListMainTaskPeekResponse
import tasks.v1.ListTaskListsRequest
import tasks.v1.ListTaskListsResponse
import tasks.v1.ListUpcomingMainTasksRequest
import tasks.v1.ListUpcomingMainTasksResponse
import tasks.v1.UpdateTaskListRequest
import tasks.v1.UpdateTaskListResponse

internal class FakeTaskListRemoteDataSource : TaskListRemoteDataSource {

    var createTaskListResult: Result<CreateTaskListResponse> = Result.success(CreateTaskListResponse())
    var getTaskListResult: Result<GetTaskListResponse> = Result.success(GetTaskListResponse())
    var getMainTaskResult: Result<GetMainTaskResponse> = Result.success(GetMainTaskResponse())
    var listTaskListsResult: Result<ListTaskListsResponse> = Result.success(ListTaskListsResponse())
    var listMainTasksSummaryResult: Result<ListMainTasksSummaryResponse> = Result.success(ListMainTasksSummaryResponse())
    var listUpcomingMainTasksResult: Result<ListUpcomingMainTasksResponse> =
        Result.success(ListUpcomingMainTasksResponse())
    var updateTaskListResult: Result<UpdateTaskListResponse> = Result.success(UpdateTaskListResponse())
    var deleteTaskListResult: Result<DeleteTaskListResponse> = Result.success(DeleteTaskListResponse())

    var lastCreateRequest: CreateTaskListRequest? = null
    var lastGetRequest: GetTaskListRequest? = null
    var lastGetMainTaskRequest: GetMainTaskRequest? = null
    var lastListRequest: ListTaskListsRequest? = null
    var lastPeekRequest: ListMainTasksSummaryRequest? = null
    var lastUpcomingRequest: ListUpcomingMainTasksRequest? = null
    var lastUpdateRequest: UpdateTaskListRequest? = null
    var lastDeleteRequest: DeleteTaskListRequest? = null

    override suspend fun createTaskList(request: CreateTaskListRequest): CreateTaskListResponse {
        lastCreateRequest = request
        return createTaskListResult.getOrThrow()
    }

    override suspend fun getTaskList(request: GetTaskListRequest): GetTaskListResponse {
        lastGetRequest = request
        return getTaskListResult.getOrThrow()
    }

    override suspend fun getMainTask(request: GetMainTaskRequest): GetMainTaskResponse {
        lastGetMainTaskRequest = request
        return getMainTaskResult.getOrThrow()
    }

    override suspend fun listTaskLists(request: ListTaskListsRequest): ListTaskListsResponse {
        lastListRequest = request
        return listTaskListsResult.getOrThrow()
    }

    override suspend fun listMainTasksSummary(request: ListMainTasksSummaryRequest): ListMainTasksSummaryResponse {
        lastPeekRequest = request
        return listMainTasksSummaryResult.getOrThrow()
    }

    override suspend fun listUpcomingMainTasks(request: ListUpcomingMainTasksRequest): ListUpcomingMainTasksResponse {
        lastUpcomingRequest = request
        return listUpcomingMainTasksResult.getOrThrow()
    }

    override suspend fun updateTaskList(request: UpdateTaskListRequest): UpdateTaskListResponse {
        lastUpdateRequest = request
        return updateTaskListResult.getOrThrow()
    }

    override suspend fun deleteTaskList(request: DeleteTaskListRequest): DeleteTaskListResponse {
        lastDeleteRequest = request
        return deleteTaskListResult.getOrThrow()
    }
}