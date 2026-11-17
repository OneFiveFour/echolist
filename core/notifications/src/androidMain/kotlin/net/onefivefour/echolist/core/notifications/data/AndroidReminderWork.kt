package net.onefivefour.echolist.core.notifications.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.reminderKey
import net.onefivefour.echolist.core.notifications.domain.nextReminderMidnight

internal class AndroidReminderBackgroundScheduler(private val context: Context) : ReminderBackgroundScheduler {

    override fun start() {
        enqueuePeriodic()
    }

    fun enqueuePeriodic(): Operation {
        val zone = TimeZone.currentSystemDefault()
        val nextMidnight = nextReminderMidnight(Clock.System.now(), zone)

        val work = PeriodicWorkRequestBuilder<ReminderSyncWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setNextScheduleTimeOverride(nextMidnight.toEpochMilliseconds()).build()

        return WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                uniqueWorkName = PERIODIC,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = work
            )
    }

    override fun requestSync() {
        enqueueRefresh()
    }

    fun enqueueRefresh(): Operation = WorkManager
        .getInstance(context)
        .enqueueUniqueWork(
            uniqueWorkName = IMMEDIATE,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = OneTimeWorkRequestBuilder<ReminderRefreshWorker>().build()
        )

    override fun stop() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(PERIODIC)
        workManager.cancelUniqueWork(IMMEDIATE)
        workManager.cancelAllWorkByTag(ACTIONS)
    }

    companion object {

        const val PERIODIC = "echolist-reminder-daily"
        const val IMMEDIATE = "echolist-reminder-refresh"
        const val ACTIONS = "echolist-reminder-actions"

        fun complete(
            context: Context,
            account: String,
            taskId: String,
            taskListId: String,
            dueDate: String
        ): Operation {
            val request = OneTimeWorkRequestBuilder<ReminderCompletionWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(
                    workDataOf(
                        "account" to account,
                        "task" to taskId,
                        "list" to taskListId,
                        "date" to dueDate
                    )
                )
                .addTag(ACTIONS)
                .build()

            return WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    uniqueWorkName = "done:${reminderKey(account, taskId)}:$dueDate",
                    existingWorkPolicy = ExistingWorkPolicy.KEEP,
                    request = request
                )
        }
    }
}

