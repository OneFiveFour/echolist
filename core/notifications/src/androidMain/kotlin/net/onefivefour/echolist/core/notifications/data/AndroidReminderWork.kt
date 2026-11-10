package net.onefivefour.echolist.core.notifications.data

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.await
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.TimeZone
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationSyncHandler
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.TaskCompletionHandler
import net.onefivefour.echolist.core.notifications.domain.reminderKey
import net.onefivefour.echolist.core.notifications.domain.nextReminderMidnight
import org.koin.core.context.GlobalContext

internal class AndroidReminderBackgroundScheduler(private val context: Context) : ReminderBackgroundScheduler {
    override fun start() {
        enqueuePeriodic()
    }

    fun enqueuePeriodic(): Operation {
        val zone = TimeZone.currentSystemDefault()
        val nextMidnight = nextReminderMidnight(Clock.System.now(), zone)
        val work = PeriodicWorkRequestBuilder<ReminderSyncWorker>(24, TimeUnit.HOURS)
            .setNextScheduleTimeOverride(nextMidnight.toEpochMilliseconds()).build()
        return WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, work)
    }

    override fun requestSync() {
        enqueueRefresh()
    }

    fun enqueueRefresh(): Operation = WorkManager.getInstance(context).enqueueUniqueWork(
        IMMEDIATE,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<ReminderRefreshWorker>().build()
    )

    override fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC)
        WorkManager.getInstance(context).cancelUniqueWork(IMMEDIATE)
        WorkManager.getInstance(context).cancelAllWorkByTag(ACTIONS)
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
                    workDataOf("account" to account, "task" to taskId, "list" to taskListId, "date" to dueDate)
                )
                .addTag(ACTIONS).build()
            return WorkManager.getInstance(context).enqueueUniqueWork(
                "done:${reminderKey(account, taskId)}:$dueDate",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}

class ReminderSyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        val koin = GlobalContext.get()
        val coordinator = koin.get<NotificationCoordinator>()
        coordinator.refreshSession()
        if (coordinator.currentAccountId() != null) {
            AndroidReminderBackgroundScheduler(applicationContext).enqueueRefresh().await()
        }
        // Daily dispatch and network retry are separate work requests: a midnight schedule override
        // must never override the backoff of a failed network refresh.
        Result.success()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        Result.retry()
    }
}

class ReminderRefreshWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        val koin = GlobalContext.get()
        val coordinator = koin.get<NotificationCoordinator>()
        coordinator.refreshSession()
        val outcome = koin.get<NotificationSyncHandler>().synchronize()
        if (outcome.isSuccess) {
            Result.success()
        } else {
            coordinator.refreshSession()
            coordinator.restore()
            Result.retry()
        }
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        Result.retry()
    }
}

class ReminderCompletionWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        Log.i("EchoListReminders", "Task completion worker started (attempt $runAttemptCount)")
        return try {
            val account = inputData.getString("account") ?: return Result.failure()
            val task = inputData.getString("task") ?: return Result.failure()
            val list = inputData.getString("list") ?: return Result.failure()
            val date = inputData.getString("date") ?: return Result.failure()
            val koin = GlobalContext.get()
            if (!koin.get<NotificationCoordinator>().isCurrentOccurrence(account, task, date)) {
                Log.i("EchoListReminders", "Task completion skipped: account or occurrence changed")
                Result.success()
            } else {
                val outcome = koin.get<TaskCompletionHandler>().completeOccurrence(list, task, date)
                if (outcome.isSuccess) {
                    Log.i("EchoListReminders", "Task completion handler succeeded")
                    Result.success()
                } else {
                    Log.w(
                        "EchoListReminders",
                        "Task completion failed; retrying (${outcome.exceptionOrNull()?.javaClass?.simpleName})"
                    )
                    Result.retry()
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w("EchoListReminders", "Task completion worker failed; retrying (${error.javaClass.simpleName})")
            Result.retry()
        }
    }
}
