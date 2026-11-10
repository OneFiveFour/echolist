package net.onefivefour.echolist.core.tasks.domain

import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.UpcomingReminder
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository

/** Resolve a saved task directly; a preview must not depend on the upcoming-task sync index. */
suspend fun previewTaskNotification(
    repository: TaskListRepository,
    notifications: NotificationCoordinator,
    taskListId: String,
    taskId: String
): Boolean = notifications.preview(taskId) {
    repository.getTaskList(taskListId).map { list ->
        val task = list.tasks.firstOrNull { it.id == taskId }
        val date = task?.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (list.id != taskListId || task == null || task.isDone || !task.isNotificationEnabled || date == null) {
            null
        } else {
            UpcomingReminder(task.id, list.id, task.description, date)
        }
    }
}
