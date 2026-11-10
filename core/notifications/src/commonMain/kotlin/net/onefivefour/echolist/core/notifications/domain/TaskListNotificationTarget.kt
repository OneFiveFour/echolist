package net.onefivefour.echolist.core.notifications.domain

/** Navigation payload only; opening a reminder never completes its task. */
data class TaskListNotificationTarget(val accountId: String, val taskListId: String)
