package net.onefivefour.echolist.core.tasks.data.source.network

import tasks.v1.CreateTaskListRequest
import tasks.v1.CreateTaskListResponse
import tasks.v1.DeleteTaskListRequest
import tasks.v1.DeleteTaskListResponse
import tasks.v1.GetMainTaskRequest
import tasks.v1.GetMainTaskResponse
import tasks.v1.GetTaskListRequest
import tasks.v1.GetTaskListResponse
import tasks.v1.ListMainTasksSummaryRequest
import tasks.v1.ListMainTasksSummaryResponse
import tasks.v1.ListTaskListsRequest
import tasks.v1.ListTaskListsResponse
import tasks.v1.ListUpcomingMainTasksRequest
import tasks.v1.ListUpcomingMainTasksResponse
import tasks.v1.UpdateTaskListRequest
import tasks.v1.UpdateTaskListResponse

internal interface TaskListRemoteDataSource {
    suspend fun createTaskList(request: CreateTaskListRequest): CreateTaskListResponse
    suspend fun getTaskList(request: GetTaskListRequest): GetTaskListResponse
    suspend fun getMainTask(request: GetMainTaskRequest): GetMainTaskResponse
    suspend fun listTaskLists(request: ListTaskListsRequest): ListTaskListsResponse
    suspend fun listMainTasksSummary(request: ListMainTasksSummaryRequest): ListMainTasksSummaryResponse
    suspend fun listUpcomingMainTasks(request: ListUpcomingMainTasksRequest): ListUpcomingMainTasksResponse
    suspend fun updateTaskList(request: UpdateTaskListRequest): UpdateTaskListResponse
    suspend fun deleteTaskList(request: DeleteTaskListRequest): DeleteTaskListResponse
}