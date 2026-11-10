package net.onefivefour.echolist.core.notifications.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.TaskListNotificationTarget

/** Explicit activity intents keep notification navigation independent of the app's route types. */
object TaskListNotificationIntent {
    private const val ACTION = "net.onefivefour.echolist.action.OPEN_TASK_LIST"

    internal fun create(context: Context, reminder: Reminder): Intent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            action = ACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Full IDs give each notification its own PendingIntent, including across accounts.
            data = Uri.Builder().scheme("echolist").authority("task-list")
                .appendPath(reminder.accountId).appendPath(reminder.taskListId)
                .appendPath(reminder.taskId).build()
        }

    fun target(intent: Intent): TaskListNotificationTarget? {
        if (intent.action != ACTION) return null
        val uri = intent.data ?: return null
        if (uri.scheme != "echolist" || uri.authority != "task-list") return null
        val parts = uri.pathSegments
        if (parts.size != 3 || parts.any { it.isBlank() }) return null
        return TaskListNotificationTarget(accountId = parts[0], taskListId = parts[1])
    }
}
