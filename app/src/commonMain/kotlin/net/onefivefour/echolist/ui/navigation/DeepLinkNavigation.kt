package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import net.onefivefour.echolist.core.deeplinks.domain.DeepLinkDestination
import net.onefivefour.echolist.core.tasks.domain.model.OpenTaskListDestination

/** Maps a resolved deep link onto the back stack. Links for another account or unknown destinations are ignored. */
internal fun MutableList<NavKey>.open(destination: DeepLinkDestination, currentAccountId: String?) {
    if (destination.accountId.isNullOrBlank() || destination.accountId != currentAccountId) return
    when (destination) {
        is OpenTaskListDestination -> openTaskList(destination.taskListId)
    }
}

/** Reuse the existing task-list screen (and its settings results) when this list is already open. */
private fun MutableList<NavKey>.openTaskList(taskListId: String) {
    if (taskListId.isBlank()) return
    val index = indexOfLast { it is EditTaskListRoute && it.taskListId == taskListId }
    if (index >= 0) {
        while (size > index + 1) removeAt(lastIndex)
    } else {
        // Existing lists load their own metadata by ID; parentDir is only used for creation.
        add(EditTaskListRoute(parentDir = "", taskListId = taskListId))
    }
}
