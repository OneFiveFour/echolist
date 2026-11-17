package net.onefivefour.echolist.core.notifications.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.await
import kotlinx.coroutines.CancellationException
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import org.koin.core.context.GlobalContext

internal class ReminderSyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
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