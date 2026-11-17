package net.onefivefour.echolist.core.notifications.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.TaskCompletionHandler
import org.koin.core.context.GlobalContext

internal class ReminderCompletionWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
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