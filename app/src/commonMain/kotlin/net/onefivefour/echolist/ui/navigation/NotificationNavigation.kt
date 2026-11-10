package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import net.onefivefour.echolist.core.notifications.domain.TaskListNotificationTarget

/** Reuse the existing editor (and its settings channel) when this list is already open. */
internal fun MutableList<NavKey>.openNotificationTaskList(
    target: TaskListNotificationTarget,
    currentAccountId: String?
) {
    if (target.accountId.isBlank() || target.taskListId.isBlank() || target.accountId != currentAccountId) return
    val index = indexOfLast { it is EditTaskListRoute && it.taskListId == target.taskListId }
    if (index >= 0) {
        while (size > index + 1) removeAt(lastIndex)
    } else {
        // Existing lists load their own metadata by ID; parentDir is only used for creation.
        add(EditTaskListRoute(parentDir = "", taskListId = target.taskListId))
    }
}
