package net.onefivefour.echolist.data.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.notifications.data.AndroidReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.reminderKey

/** The persistent worker survives process death and waits for connectivity. */
class TaskDoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_DONE) return
        val account = intent.getStringExtra(TaskReminderReceiver.EXTRA_ACCOUNT_ID)
        val date = intent.getStringExtra(TaskReminderReceiver.EXTRA_DUE_DATE)
        val task = intent.getStringExtra(EXTRA_TASK_ID)
        val list = intent.getStringExtra(EXTRA_TASK_LIST_ID)
        if (account == null || date == null || task == null || list == null) {
            Log.w("EchoListReminders", "Done action ignored: missing account, occurrence or task data")
            return
        }
        Log.i("EchoListReminders", "Done action received")
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                AndroidReminderBackgroundScheduler.complete(context, account, task, list, date).await()
                Log.i("EchoListReminders", "Task completion durably enqueued")
                // Dismiss only after the action is durably enqueued; a failed enqueue remains actionable.
                manager.cancel(reminderKey(account, task), 0)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w("EchoListReminders", "Unable to enqueue task completion (${error.javaClass.simpleName})")
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "net.onefivefour.echolist.action.MARK_TASK_DONE"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_LIST_ID = "task_list_id"
    }
}
