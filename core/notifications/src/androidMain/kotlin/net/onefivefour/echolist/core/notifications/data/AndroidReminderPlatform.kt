package net.onefivefour.echolist.core.notifications.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.data.notification.TaskDoneReceiver
import net.onefivefour.echolist.data.notification.TaskReminderReceiver

internal class AndroidReminderPlatform(private val context: Context) : ReminderPlatform {
    private val alarms get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notifications get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun schedule(reminder: Reminder) {
        cancelLegacy(reminder.taskId)
        val pending = PendingIntent.getBroadcast(
            context,
            0,
            reminderIntent(reminder),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarms.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            LocalDate.parse(reminder.dueDate).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            pending
        )
    }

    override suspend fun cancelPending(reminder: Reminder) {
        PendingIntent.getBroadcast(
            context,
            0,
            reminderIntent(reminder),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let {
            alarms.cancel(it)
            it.cancel()
        }
        cancelLegacy(reminder.taskId)
    }

    override suspend fun dismiss(reminder: Reminder) {
        notifications.cancel(reminder.key, 0)
        notifications.cancel(reminder.taskId.hashCode())
    }

    override suspend fun show(reminder: Reminder): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifications.createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            val channel = notifications.getNotificationChannel(CHANNEL)
            if (channel.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val done = Intent(context, TaskDoneReceiver::class.java).apply {
            action = TaskDoneReceiver.ACTION_MARK_DONE
            data = identity("done", reminder)
            putExtra(TaskReminderReceiver.EXTRA_ACCOUNT_ID, reminder.accountId)
            putExtra(TaskReminderReceiver.EXTRA_DUE_DATE, reminder.dueDate)
            putExtra(TaskDoneReceiver.EXTRA_TASK_ID, reminder.taskId)
            putExtra(TaskDoneReceiver.EXTRA_TASK_LIST_ID, reminder.taskListId)
        }
        val donePending = PendingIntent.getBroadcast(
            context,
            0,
            done,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openPending = TaskListNotificationIntent.create(context, reminder)?.let { intent ->
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val publicVersion = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("EchoList")
            .setContentText("You have a task reminder").build()
        val sameOccurrence = notifications.activeNotifications.any {
            it.tag == reminder.key && it.id == 0 && it.notification.extras.getString(DUE_MARKER) == reminder.dueDate
        }
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminder.title.take(100)).setContentText(reminder.body.take(300))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(publicVersion)
            .setContentIntent(openPending)
            .setAutoCancel(true).setOnlyAlertOnce(sameOccurrence).setGroup("echolist_task_reminders")
            .addExtras(Bundle().apply { putString(DUE_MARKER, reminder.dueDate) })
            .addAction(android.R.drawable.checkbox_on_background, "Done", donePending).build()
        return try {
            notifications.notify(reminder.key, 0, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun reminderIntent(reminder: Reminder) = Intent(context, TaskReminderReceiver::class.java).apply {
        data = identity("reminder", reminder)
        putExtra(TaskReminderReceiver.EXTRA_ACCOUNT_ID, reminder.accountId)
        putExtra(TaskReminderReceiver.EXTRA_TASK_ID, reminder.taskId)
        putExtra(TaskReminderReceiver.EXTRA_DUE_DATE, reminder.dueDate)
    }

    private fun identity(kind: String, reminder: Reminder): Uri = Uri.Builder().scheme("echolist")
        .authority(kind).appendPath(reminder.accountId).appendPath(reminder.taskId).build()

    private fun cancelLegacy(taskId: String) {
        PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            Intent(context, TaskReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let {
            alarms.cancel(it)
            it.cancel()
        }
    }

    companion object {
        const val CHANNEL = "echolist_task_reminders"
        private const val DUE_MARKER = "echolist_due_date"
    }
}
