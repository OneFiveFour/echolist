package net.onefivefour.echolist.core.notifications.domain

import kotlinx.datetime.LocalDate

/** Pure metadata reconciliation. The coordinator applies side effects under its shared lock. */
internal object ReminderReconciler {
    fun replaceSnapshot(
        reminders: List<Reminder>,
        account: String,
        tasks: List<UpcomingReminder>,
        from: LocalDate
    ): ReminderChanges {
        require(tasks.map { it.taskId }.distinct().size == tasks.size) { "Duplicate task IDs" }
        require(tasks.all { it.taskId.isNotBlank() && it.taskListId.isNotBlank() && it.dueDate >= from }) {
            "Incomplete upcoming-task snapshot"
        }
        val ids = tasks.map { it.taskId }.toSet()
        return reconcile(reminders, account, tasks) {
            it.accountId == account && it.active && LocalDate.parse(it.dueDate) >= from && it.taskId !in ids
        }
    }

    fun replaceTaskList(
        reminders: List<Reminder>,
        account: String,
        taskListId: String,
        tasks: List<UpcomingReminder>
    ): ReminderChanges {
        val ids = tasks.map { it.taskId }.toSet()
        return reconcile(reminders, account, tasks) {
            it.accountId == account && it.taskListId == taskListId && it.taskId !in ids
        }
    }

    fun merge(reminders: List<Reminder>, account: String, tasks: List<UpcomingReminder>): List<Reminder> {
        val records = reminders.toMutableList()
        tasks.forEach { task ->
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
        return records
    }

    private fun reconcile(
        reminders: List<Reminder>,
        account: String,
        tasks: List<UpcomingReminder>,
        shouldRemove: (Reminder) -> Boolean
    ): ReminderChanges {
        val removed = mutableListOf<Reminder>()
        val retained = reminders.map {
            if (shouldRemove(it)) {
                removed.add(it)
                it.copy(active = false)
            } else {
                it
            }
        }
        return ReminderChanges(merge(retained, account, tasks), removed)
    }
}

internal data class ReminderChanges(val reminders: List<Reminder>, val removed: List<Reminder>)
