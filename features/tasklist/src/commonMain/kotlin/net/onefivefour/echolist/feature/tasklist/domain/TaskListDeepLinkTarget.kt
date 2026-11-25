package net.onefivefour.echolist.feature.tasklist.domain

import net.onefivefour.echolist.core.deeplinks.domain.DeepLink
import net.onefivefour.echolist.core.deeplinks.domain.DeepLinkDestination
import net.onefivefour.echolist.core.deeplinks.domain.DeepLinkTarget
import net.onefivefour.echolist.core.notifications.domain.ReminderDeepLink
import net.onefivefour.echolist.core.tasks.domain.model.OpenTaskListDestination

/** Accepts reminder links (`<accountId>/<taskListId>/<taskId>`) and asks to open the existing task list. */
class TaskListDeepLinkTarget : DeepLinkTarget {

    override fun resolve(link: DeepLink): DeepLinkDestination? {
        if (link.host != ReminderDeepLink.HOST) return null
        if (link.segments.size != 3 || link.segments.any { it.isBlank() }) return null
        return OpenTaskListDestination(accountId = link.segments[0], taskListId = link.segments[1])
    }
}
