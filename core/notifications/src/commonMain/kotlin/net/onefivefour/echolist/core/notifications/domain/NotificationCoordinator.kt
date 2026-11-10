package net.onefivefour.echolist.core.notifications.domain

import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class NotificationCoordinator(
    private val store: ReminderStore,
    private val platform: ReminderPlatform,
    private val permission: NotificationPermissionChecker,
    private val accounts: NotificationAccountProvider,
    private val background: ReminderBackgroundScheduler,
    private val clock: Clock = Clock.System,
    private val zone: () -> TimeZone = { TimeZone.currentSystemDefault() }
) {
    private val mutex = Mutex()
    private var pendingMutations = 0

    fun currentAccountId(): String? = accounts.currentAccountId()

    suspend fun beginMutation(): String? = mutex.withLock {
        val state = store.read()
        store.write(state.copy(revision = state.revision + 1))
        pendingMutations++
        accounts.currentAccountId()
    }

    suspend fun endMutation() = withContext(NonCancellable) {
        mutex.withLock {
            pendingMutations--
            val state = store.read()
            store.write(state.copy(revision = state.revision + 1))
            if (pendingMutations == 0) restoreLocked()
        }
        background.requestSync()
    }

    suspend fun refreshSession(): Unit = mutex.withLock {
        val state = store.read()
        val account = accounts.currentAccountId()
        if (state.activeAccount != account) {
            // Cancel both old alarms and visible notifications before publishing the new session.
            state.reminders.filter { it.accountId == state.activeAccount }.forEach {
                platform.cancelPending(it)
                platform.dismiss(it)
            }
            store.write(state.copy(activeAccount = account, revision = state.revision + 1))
        }
        if (account == null) background.stop() else background.start()
    }

    suspend fun isEnabled(taskId: String): Boolean = mutex.withLock {
        val account = accounts.currentAccountId() ?: return@withLock false
        store.read().preferences[reminderKey(account, taskId)] ?: true
    }

    suspend fun setEnabled(taskId: String, enabled: Boolean): Unit = mutex.withLock {
        val account = accounts.currentAccountId() ?: return@withLock
        val state = store.read()
        val key = reminderKey(account, taskId)
        store.write(state.copy(preferences = state.preferences + (key to enabled), revision = state.revision + 1))
        if (!enabled) {
            state.reminders.firstOrNull { it.key == key }?.let {
                platform.cancelPending(it)
                platform.dismiss(it)
            }
        }
    }

    /** An authoritative task-list mutation immediately invalidates in-flight server snapshots. */
    suspend fun replaceTaskList(
        taskListId: String,
        tasks: List<UpcomingReminder>,
        preferences: Map<String, Boolean> = emptyMap(),
        expectedAccount: String? = accounts.currentAccountId()
    ): Unit = mutex.withLock {
        val account = accounts.currentAccountId() ?: return@withLock
        val state = store.read()
        if (state.activeAccount != account || expectedAccount != account) return@withLock
        val desired = tasks.associateBy { it.taskId }
        val records = state.reminders.map { old ->
            if (old.accountId == account && old.taskListId == taskListId && old.taskId !in desired) {
                platform.cancelPending(old)
                platform.dismiss(old)
                old.copy(active = false)
            } else {
                old
            }
        }.toMutableList()
        tasks.forEach { merge(records, account, it) }
        val prefs = state.preferences + preferences.mapKeys { reminderKey(account, it.key) }
        store.write(state.copy(reminders = records, preferences = prefs, revision = state.revision + 1))
        restoreLocked()
        background.requestSync()
    }

    suspend fun schedule(taskId: String, taskListId: String, title: String, body: String, dueDate: String) {
        LocalDate.parse(dueDate)
        mutex.withLock {
            val account = accounts.currentAccountId() ?: return@withLock
            val state = store.read()
            if (state.activeAccount != account) return@withLock
            val records = state.reminders.toMutableList()
            val old = records.firstOrNull { it.key == reminderKey(account, taskId) }
            if (old != null) {
                // Repository mutations and complete server snapshots own existing metadata. A late
                // editor callback must neither resurrect a removed task nor restore an older due date.
                restoreLocked()
                return@withLock
            }
            val record = Reminder(
                account,
                taskId,
                taskListId,
                title,
                body,
                dueDate
            )
            records.removeAll { it.key == record.key }
            records.add(record)
            store.write(state.copy(reminders = records, revision = state.revision + 1))
            restoreLocked()
        }
    }

    suspend fun cancel(taskId: String): Unit = mutex.withLock {
        val account = accounts.currentAccountId() ?: return@withLock
        val state = store.read()
        val old = state.reminders.firstOrNull { it.key == reminderKey(account, taskId) } ?: return@withLock
        platform.cancelPending(old)
        platform.dismiss(old)
        store.write(
            state.copy(
                reminders = state.reminders.map { if (it.key == old.key) it.copy(active = false) else it },
                revision = state.revision + 1
            )
        )
        background.requestSync()
    }

    suspend fun cancelWhenIneligible(taskId: String): Unit = mutex.withLock {
        val account = accounts.currentAccountId() ?: return@withLock
        val state = store.read()
        val record = state.reminders.firstOrNull { it.key == reminderKey(account, taskId) } ?: return@withLock
        if (!record.active || state.preferences[record.key] == false) {
            platform.cancelPending(record)
            platform.dismiss(record)
        }
    }

    suspend fun restore(): Unit = mutex.withLock { restoreLocked() }

    /**
     * Preview a reminder through normal platform delivery, including its actions.
     * Only debug launchers expose this operation. It ignores timing/deduplication without
     * changing occurrence history or cancelling the real alarm.
     */
    suspend fun preview(
        taskId: String,
        resolve: (suspend () -> Result<UpcomingReminder?>)? = null
    ): Boolean {
        val snapshot = mutex.withLock {
            if (pendingMutations != 0) return false
            val state = store.read()
            if (state.activeAccount == null || state.activeAccount != accounts.currentAccountId()) return false
            state
        }
        // Network access runs outside the coordinator lock. Reject stale responses below.
        val task = if (resolve != null) {
            resolve().getOrElse {
                if (it is CancellationException) throw it
                return false
            } ?: return false
        } else {
            null
        }
        return mutex.withLock {
            val state = store.read()
            val account = accounts.currentAccountId() ?: return@withLock false
            if (pendingMutations != 0 || state.revision != snapshot.revision ||
                state.activeAccount != snapshot.activeAccount || state.activeAccount != account
            ) return@withLock false
            if (state.preferences[reminderKey(account, taskId)] == false || !permission.isGranted()) {
                return@withLock false
            }
            val records = state.reminders.toMutableList()
            if (task != null) {
                if (task.taskId != taskId || task.taskListId.isBlank()) return@withLock false
                merge(records, account, task)
            }
            val reminder = records.firstOrNull {
                it.accountId == account && it.taskId == taskId && it.active
            } ?: return@withLock false
            if (task != null) {
                // Done validates its occurrence against this metadata, even before the first sync.
                // Keep delivered dates and all other tasks; do not consume the real reminder.
                store.write(state.copy(reminders = records, revision = state.revision + 1))
            }
            platform.show(reminder)
        }
    }

    suspend fun deliver(account: String, taskId: String, dueDate: String): Unit = mutex.withLock {
        val state = store.read()
        val reminder = state.reminders.firstOrNull {
            it.accountId == account && it.taskId == taskId && it.dueDate == dueDate
        } ?: return@withLock
        deliverLocked(state, reminder)
    }

    suspend fun isCurrentOccurrence(account: String, taskId: String, dueDate: String): Boolean = mutex.withLock {
        val state = store.read()
        account == accounts.currentAccountId() && account == state.activeAccount &&
            state.reminders.any { it.accountId == account && it.taskId == taskId && it.dueDate == dueDate }
    }

    suspend fun synchronize(fetch: suspend (LocalDate) -> Result<List<UpcomingReminder>>): Result<Unit> = try {
        refreshSession()
        val snapshot = mutex.withLock {
            check(pendingMutations == 0) { "Task mutation is in progress" }
            store.read()
        }
        val account = snapshot.activeAccount
        if (account == null) {
            Result.success(Unit)
        } else {
            // The backend excludes completed tasks. Fetch the complete open-task snapshot, including
            // backdated tasks created on another device before our last successful synchronization.
            // A moving "today"/checkpoint cutoff cannot discover those tasks reliably.
            val todayUtc = clock.todayIn(TimeZone.UTC)
            val from = LocalDate(0, 1, 1)
            val response = fetch(from)
            response.fold(
                onSuccess = { tasks ->
                    mutex.withLock {
                        val state = store.read()
                        check(
                            accounts.currentAccountId() == account && state.activeAccount == account &&
                                state.revision == snapshot.revision && pendingMutations == 0
                        ) {
                            "Notification state changed during synchronization"
                        }
                        require(tasks.map { it.taskId }.distinct().size == tasks.size) { "Duplicate task IDs" }
                        require(
                            tasks.all { it.taskId.isNotBlank() && it.taskListId.isNotBlank() && it.dueDate >= from }
                        ) {
                            "Incomplete upcoming-task snapshot"
                        }
                        val ids = tasks.map { it.taskId }.toSet()
                        val records = state.reminders.map { old ->
                            if (old.accountId == account && old.active && LocalDate.parse(old.dueDate) >= from &&
                                old.taskId !in ids
                            ) {
                                platform.cancelPending(old)
                                // Keep already displayed reminders until the user handles them.
                                old.copy(active = false)
                            } else {
                                old
                            }
                        }.toMutableList()
                        tasks.forEach { merge(records, account, it) }
                        store.write(
                            state.copy(
                                reminders = records,
                                syncDates = state.syncDates + (account to todayUtc.toString()),
                                lastSuccessUtc = state.lastSuccessUtc + (account to clock.now().toString()),
                                lastError = null,
                                revision = state.revision + 1
                            )
                        )
                        restoreLocked()
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    mutex.withLock {
                        val state = store.read()
                        store.write(state.copy(lastError = "Notification synchronization failed"))
                        restoreLocked() // Offline catch-up uses known reminders without deleting anything.
                    }
                    Result.failure(error)
                }
            )
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun merge(records: MutableList<Reminder>, account: String, task: UpcomingReminder) {
        val old = records.firstOrNull { it.accountId == account && it.taskId == task.taskId }
        records.removeAll { it.accountId == account && it.taskId == task.taskId }
        records.add(
            Reminder(
                account,
                task.taskId,
                task.taskListId,
                "Task due",
                task.title,
                task.dueDate.toString(),
                deliveredDates = old?.deliveredDates.orEmpty()
            )
        )
    }

    private suspend fun restoreLocked() {
        if (pendingMutations != 0) return
        val state = store.read()
        val account = accounts.currentAccountId()
        if (account == null || state.activeAccount != account) return
        state.reminders.filter { it.accountId == account }.forEach { reminder ->
            if (!reminder.active || state.preferences[reminder.key] == false ||
                reminder.dueDate in reminder.deliveredDates
            ) {
                platform.cancelPending(reminder)
            } else if (LocalDate.parse(reminder.dueDate) <= clock.todayIn(zone())) {
                deliverLocked(store.read(), reminder)
            } else {
                platform.schedule(reminder)
            }
        }
    }

    private suspend fun deliverLocked(state: ReminderState, reminder: Reminder) {
        if (pendingMutations != 0) return
        if (accounts.currentAccountId() != reminder.accountId || state.activeAccount != reminder.accountId ||
            !reminder.active || state.preferences[reminder.key] == false ||
            reminder.dueDate in reminder.deliveredDates ||
            LocalDate.parse(reminder.dueDate) > clock.todayIn(zone())
        ) {
            return
        }
        if (!permission.isGranted()) return // Keep the occurrence pending for permission restoration.
        if (platform.show(reminder)) {
            store.write(
                state.copy(
                    reminders = state.reminders.map {
                        if (it.key == reminder.key) {
                            it.copy(
                                deliveredDates = it.deliveredDates + reminder.dueDate
                            )
                        } else {
                            it
                        }
                    }
                )
            )
            platform.cancelPending(reminder)
        }
    }
}
