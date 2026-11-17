package net.onefivefour.echolist.core.tasks.domain.repository

import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.MainTasksSummary
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams

interface TaskListRepository {
    suspend fun createTaskList(params: CreateTaskListParams): Result<TaskList>
    suspend fun getTaskList(taskListId: String): Result<TaskList>
    suspend fun getMainTask(mainTaskId: String): Result<MainTask>
    suspend fun listTaskLists(parentDir: String): Result<List<TaskListEntry>>
    suspend fun listMainTasksSummary(parentDir: String): Result<List<MainTasksSummary>>
    suspend fun listUpcomingMainTasks(fromDate: LocalDate): Result<List<UpcomingMainTask>>
    suspend fun updateTaskList(params: UpdateTaskListParams): Result<TaskList>
    suspend fun deleteTaskList(taskListId: String): Result<Unit>
}