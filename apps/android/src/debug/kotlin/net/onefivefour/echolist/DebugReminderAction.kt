package net.onefivefour.echolist

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.tasks.domain.previewTaskNotification
import org.koin.android.ext.android.get

/** Compiled only into the debug APK; release never receives a long-press handler. */
internal fun ComponentActivity.debugReminderAction(): ((String, String) -> Unit)? = { taskListId, taskId ->
    lifecycleScope.launch {
        val shown = try {
            previewTaskNotification(get(), get<NotificationCoordinator>(), taskListId, taskId)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w("EchoListReminders", "Debug reminder failed (${error.javaClass.simpleName})")
            false
        }
        Toast.makeText(
            this@debugReminderAction,
            if (shown) "Test-Erinnerung gesendet." else
                "Keine Erinnerung gesendet. Task speichern, Erinnerung aktivieren und Berechtigung prüfen.",
            Toast.LENGTH_LONG
        ).show()
    }
}
