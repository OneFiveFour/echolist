package net.onefivefour.echolist.core.notifications.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationSyncHandler
import org.koin.core.context.GlobalContext

internal class ReminderRefreshWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
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