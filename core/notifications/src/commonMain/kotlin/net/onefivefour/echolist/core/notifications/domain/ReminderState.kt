package net.onefivefour.echolist.core.notifications.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ReminderState(
    val activeAccount: String? = null,
    val revision: Long = 0,
    val reminders: List<Reminder> = emptyList(),
    val preferences: Map<String, Boolean> = emptyMap(),
    val syncDates: Map<String, String> = emptyMap(),
    val lastSuccessUtc: Map<String, String> = emptyMap(),
    val lastError: String? = null
)

@Serializable
data class Reminder(
    val accountId: String,
    val taskId: String,
    val taskListId: String,
    val title: String,
    val body: String,
    val dueDate: String,
    val isActive: Boolean = true,
    val deliveredDates: Set<String> = emptySet()
) {
    val key: String get() = reminderKey(accountId, taskId)
}

/** Length-prefixing avoids delimiter and String.hashCode collisions. */
fun reminderKey(accountId: String, taskId: String): String = "${accountId.length}:$accountId$taskId"

interface ReminderStore {
    suspend fun read(): ReminderState
    suspend fun write(state: ReminderState)
}

fun interface NotificationAccountProvider {
    fun currentAccountId(): String?
}

fun interface NotificationSyncHandler {
    suspend fun synchronize(): Result<Unit>
}

interface ReminderBackgroundScheduler {
    fun start()
    fun requestSync()
    fun stop()
}

/** Platform delivery is always checked against persisted state before displaying anything. */
interface ReminderPlatform {
    suspend fun schedule(reminder: Reminder)
    suspend fun cancelPending(reminder: Reminder)
    suspend fun dismiss(reminder: Reminder)
    suspend fun show(reminder: Reminder): Boolean
}

data class UpcomingReminder(val taskId: String, val taskListId: String, val title: String, val dueDate: LocalDate)