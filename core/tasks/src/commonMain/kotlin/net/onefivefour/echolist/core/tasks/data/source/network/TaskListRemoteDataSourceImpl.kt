package net.onefivefour.echolist.core.tasks.data.source.network

import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import tasks.v1.CreateTaskListRequest
import tasks.v1.CreateTaskListResponse
import tasks.v1.DeleteTaskListRequest
import tasks.v1.DeleteTaskListResponse
import tasks.v1.GetMainTaskRequest
import tasks.v1.GetMainTaskResponse
import tasks.v1.GetTaskListRequest
import tasks.v1.GetTaskListResponse
import tasks.v1.ListMainTaskPeekRequest
import tasks.v1.ListMainTaskPeekResponse
import tasks.v1.ListTaskListsRequest
import tasks.v1.ListTaskListsResponse
import tasks.v1.ListUpcomingMainTasksRequest
import tasks.v1.ListUpcomingMainTasksResponse
import tasks.v1.UpdateTaskListRequest
import tasks.v1.UpdateTaskListResponse

internal class TaskListRemoteDataSourceImpl(
    private val client: ConnectRpcClient
) : TaskListRemoteDataSource {

    override suspend fun createTaskList(request: CreateTaskListRequest): CreateTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/CreateTaskList",
            request = request,
            requestSerializer = { CreateTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { CreateTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun getTaskList(request: GetTaskListRequest): GetTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/GetTaskList",
            request = request,
            requestSerializer = { GetTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { GetTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun getMainTask(request: GetMainTaskRequest): GetMainTaskResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/GetMainTask",
            request = request,
            requestSerializer = { GetMainTaskRequest.ADAPTER.encode(it) },
            responseDeserializer = { GetMainTaskResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun listTaskLists(request: ListTaskListsRequest): ListTaskListsResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/ListTaskLists",
            request = request,
            requestSerializer = { ListTaskListsRequest.ADAPTER.encode(it) },
            responseDeserializer = { ListTaskListsResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun listMainTaskPeek(request: ListMainTaskPeekRequest): ListMainTaskPeekResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/ListMainTaskPeek",
            request = request,
            requestSerializer = { ListMainTaskPeekRequest.ADAPTER.encode(it) },
            responseDeserializer = { ListMainTaskPeekResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun listUpcomingMainTasks(request: ListUpcomingMainTasksRequest): ListUpcomingMainTasksResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/ListUpcomingMainTasks",
            request = request,
            requestSerializer = { ListUpcomingMainTasksRequest.ADAPTER.encode(it) },
            responseDeserializer = { ListUpcomingMainTasksResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun updateTaskList(request: UpdateTaskListRequest): UpdateTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/UpdateTaskList",
            request = request,
            requestSerializer = { UpdateTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { UpdateTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun deleteTaskList(request: DeleteTaskListRequest): DeleteTaskListResponse {
        return client.call(
            path = "/tasks.v1.TaskListService/DeleteTaskList",
            request = request,
            requestSerializer = { DeleteTaskListRequest.ADAPTER.encode(it) },
            responseDeserializer = { DeleteTaskListResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }
}