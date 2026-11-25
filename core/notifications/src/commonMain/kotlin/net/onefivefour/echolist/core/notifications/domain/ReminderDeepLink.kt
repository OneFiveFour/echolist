package net.onefivefour.echolist.core.notifications.domain

import net.onefivefour.echolist.core.deeplinks.domain.DeepLink

/**
 * Link issued by a reminder notification: `<host>/<accountId>/<taskListId>/<taskId>`.
 * Full IDs keep each notification's PendingIntent distinct, including across accounts.
 * Interpreting the link is up to the module that owns the destination; opening it never completes the task.
 */
object ReminderDeepLink {
    const val HOST = "task-list"

    fun create(reminder: Reminder): DeepLink = DeepLink(
        host = HOST,
        segments = listOf(reminder.accountId, reminder.taskListId, reminder.taskId)
    )
}
