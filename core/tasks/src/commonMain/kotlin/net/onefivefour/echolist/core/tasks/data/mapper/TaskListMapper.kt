package net.onefivefour.echolist.core.tasks.data.mapper

import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.tasks.domain.model.CreateTaskListParams
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.SubTask
import net.onefivefour.echolist.core.tasks.domain.model.TaskList
import net.onefivefour.echolist.core.tasks.domain.model.TaskListEntry
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import tasks.v1.CreateTaskListRequest
import tasks.v1.CreateTaskListResponse
import tasks.v1.GetMainTaskResponse
import tasks.v1.GetTaskListResponse
import tasks.v1.ListTaskListsResponse
import tasks.v1.ListUpcomingMainTasksResponse
import tasks.v1.UpdateTaskListRequest
import tasks.v1.UpdateTaskListResponse

/**
 * Maps between Wire-generated task list proto models and domain models.
 */
@Suppress("TooManyFunctions")
internal object TaskListMapper {

    // Proto -> Domain

    fun toDomain(proto: tasks.v1.SubTask): SubTask = SubTask(
        id = proto.id,
        description = proto.description,
        isDone = proto.is_done
    )

    fun toDomain(proto: tasks.v1.MainTask): MainTask = MainTask(
        id = proto.id,
        description = proto.description,
        isDone = proto.is_done,
        dueDate = proto.due_date,
        recurrence = proto.recurrence,
        subTasks = proto.sub_tasks.map { toDomain(it) }
    )

    fun toDomain(proto: tasks.v1.TaskList): TaskList = TaskList(
        id = proto.id,
        parentDir = proto.parent_dir,
        name = proto.title,
        tasks = proto.tasks.map { toDomain(it) },
        updatedAt = proto.updated_at,
        isAutoDelete = proto.is_auto_delete
    )

    fun toDomain(proto: CreateTaskListResponse): TaskList {
        val taskList = requireTaskList(proto.task_list, "CreateTaskListResponse")
        return toDomain(taskList)
    }

    fun toDomain(proto: GetTaskListResponse): TaskList {
        val taskList = requireTaskList(proto.task_list, "GetTaskListResponse")
        return toDomain(taskList)
    }

    fun toDomain(proto: GetMainTaskResponse): MainTask {
        val mainTask = requireNotNull(proto.main_task) {
            "GetMainTaskResponse did not include a main task payload"
        }
        return toDomain(mainTask)
    }

    fun toDomain(proto: ListTaskListsResponse): List<TaskListEntry> =
        proto.task_lists.map { toEntry(it) }

    fun toDomain(proto: tasks.v1.UpcomingMainTask): UpcomingMainTask {
        val dueDate = try {
            require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(proto.due_date)) {
                "Expected YYYY-MM-DD"
            }
            LocalDate.parse(proto.due_date)
        } catch (e: IllegalArgumentException) {
            throw NetworkException.SerializationError("UpcomingMainTask.due_date is not a valid YYYY-MM-DD date", e)
        }
        return UpcomingMainTask(
            taskId = proto.task_id,
            title = proto.title,
            dueDate = dueDate,
            taskListId = proto.tasklist_id,
            isRecurring = proto.is_recurring
        )
    }

    fun toDomain(proto: ListUpcomingMainTasksResponse): List<UpcomingMainTask> =
        proto.tasks.map { toDomain(it) }

    fun toEntry(proto: tasks.v1.TaskList): TaskListEntry = TaskListEntry(
        id = proto.id,
        parentDir = proto.parent_dir,
        name = proto.title,
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: UpdateTaskListResponse): TaskList {
        val taskList = requireTaskList(proto.task_list, "UpdateTaskListResponse")
        return toDomain(taskList)
    }

    // Domain -> Proto

    fun toProto(params: CreateTaskListParams): CreateTaskListRequest = CreateTaskListRequest(
        title = params.name,
        parent_dir = params.parentDir,
        tasks = params.tasks.map { toProto(it) },
        is_auto_delete = params.isAutoDelete
    )

    fun toProto(params: UpdateTaskListParams): UpdateTaskListRequest = UpdateTaskListRequest(
        id = params.id,
        title = params.title,
        tasks = params.tasks.map { toProto(it) },
        is_auto_delete = params.isAutoDelete
    )

    fun toProto(domain: MainTask): tasks.v1.MainTask = tasks.v1.MainTask(
        id = domain.id,
        description = domain.description,
        is_done = domain.isDone,
        due_date = domain.dueDate,
        recurrence = domain.recurrence,
        sub_tasks = domain.subTasks.map { toProto(it) }
    )

    fun toProto(domain: SubTask): tasks.v1.SubTask = tasks.v1.SubTask(
        id = domain.id,
        description = domain.description,
        is_done = domain.isDone
    )

    private fun requireTaskList(taskList: tasks.v1.TaskList?, responseType: String): tasks.v1.TaskList =
        requireNotNull(taskList) { "$responseType did not include a task list payload" }
}