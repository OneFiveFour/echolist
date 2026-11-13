package net.onefivefour.echolist.core.tasks.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.UpcomingReminder
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.core.files.domain.normalizePath
import net.onefivefour.echolist.core.tasks.data.mapper.TaskListMapper
import net.onefivefour.echolist.core.tasks.data.source.network.TaskListRemoteDataSource
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.MainTaskPeek
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository
import tasks.v1.DeleteTaskListRequest
import tasks.v1.GetMainTaskRequest
import tasks.v1.GetTaskListRequest
import tasks.v1.ListMainTaskPeekRequest
import tasks.v1.ListTaskListsRequest
import tasks.v1.ListUpcomingMainTasksRequest

internal class TaskListRepositoryImpl(
    private val networkDataSource: TaskListRemoteDataSource,
    private val dispatcher: CoroutineDispatcher,
    private val directoryChangeNotifier: DirectoryChangeNotifier,
    private val notifications: NotificationCoordinator? = null
) : TaskListRepository {

    override suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList> =
        withContext(dispatcher) {
            val accountId = notifications?.beginMutation()
            try {
                val request = TaskListMapper.toProto(params)
                val response = networkDataSource.createTaskList(request)
                val taskList = remember(TaskListMapper.toDomain(response), params.tasks, accountId)
                directoryChangeNotifier.notifyChanged(normalizePath(params.parentDir))
                Result.success(taskList)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                notifications?.endMutation()
            }
        }

    override suspend fun getTaskList(taskListId: String): Result<TaskList> =
        withContext(dispatcher) {
            try {
                val request = GetTaskListRequest(id = taskListId)
                val accountId = notifications?.currentAccountId()
                val response = networkDataSource.getTaskList(request)
                Result.success(withPreferences(TaskListMapper.toDomain(response), accountId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getMainTask(mainTaskId: String): Result<MainTask> =
        withContext(dispatcher) {
            try {
                val request = GetMainTaskRequest(id = mainTaskId)
                val accountId = notifications?.currentAccountId()
                val response = networkDataSource.getMainTask(request)
                val task = TaskListMapper.toDomain(response)
                checkAccount(accountId)
                Result.success(
                    task.copy(isNotificationEnabled = notifications?.isEnabled(task.id) ?: task.isNotificationEnabled)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>> =
        withContext(dispatcher) {
            try {
                val request = ListTaskListsRequest(parent_dir = parentDir)
                val response = networkDataSource.listTaskLists(request)
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listMainTaskPeek(parentDir: String): Result<List<MainTaskPeek>> =
        withContext(dispatcher) {
            try {
                val request = ListMainTaskPeekRequest(parent_dir = parentDir)
                val response = networkDataSource.listMainTaskPeek(request)
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>> =
        withContext(dispatcher) {
            try {
                val date = fromDate.toString()
                require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(date)) {
                    "fromDate must be representable as YYYY-MM-DD"
                }
                val response = networkDataSource.listUpcomingMainTasks(
                    ListUpcomingMainTasksRequest(from_date = date)
                )
                Result.success(TaskListMapper.toDomain(response))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList> =
        withContext(dispatcher) {
            val accountId = notifications?.beginMutation()
            try {
                val request = TaskListMapper.toProto(params)
                val response = networkDataSource.updateTaskList(request)
                val taskList = remember(TaskListMapper.toDomain(response), params.tasks, accountId)
                directoryChangeNotifier.notifyChanged(
                    normalizePath(taskList.parentDir)
                )
                Result.success(taskList)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                notifications?.endMutation()
            }
        }

    override suspend fun deleteTaskList(taskListId: String): Result<Unit> =
        withContext(dispatcher) {
            val accountId = notifications?.beginMutation()
            try {
                val taskListResponse = networkDataSource.getTaskList(GetTaskListRequest(id = taskListId))
                val parentDir = TaskListMapper.toDomain(taskListResponse).parentDir
                val request = DeleteTaskListRequest(id = taskListId)
                networkDataSource.deleteTaskList(request)
                notifications?.replaceTaskList(taskListId, emptyList(), expectedAccount = accountId)
                directoryChangeNotifier.notifyChanged(
                    normalizePath(parentDir)
                )
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                notifications?.endMutation()
            }
        }

    private fun checkAccount(accountId: String?) {
        if (notifications != null && notifications.currentAccountId() != accountId) {
            throw CancellationException("Account changed during task request")
        }
    }

    private suspend fun withPreferences(taskList: TaskList, accountId: String?): TaskList {
        checkAccount(accountId)
        return taskList.copy(
            tasks = taskList.tasks.map {
                it.copy(isNotificationEnabled = notifications?.isEnabled(it.id) ?: it.isNotificationEnabled)
            }
        )
    }

    private suspend fun remember(taskList: TaskList, sent: List<MainTask>, accountId: String?): TaskList {
        checkAccount(accountId)
        val preferences = sent.associate { it.id to it.isNotificationEnabled }
        val result = taskList.copy(
            tasks = taskList.tasks.map {
                // Device preferences may have changed while this server mutation was in flight.
                it.copy(
                    isNotificationEnabled = notifications?.isEnabled(it.id)
                        ?: preferences[it.id] ?: it.isNotificationEnabled
                )
            }
        )
        notifications?.replaceTaskList(
            taskList.id,
            result.tasks.mapNotNull { task ->
                val date = runCatching { LocalDate.parse(task.dueDate) }.getOrNull()
                if (task.isDone || date == null) {
                    null
                } else {
                    UpcomingReminder(task.id, taskList.id, task.description, date)
                }
            },
            expectedAccount = accountId
        )
        return result
    }
}