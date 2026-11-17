package net.onefivefour.echolist.core.notifications.domain

/** Preview metadata ignores timing/history but preserves the occurrence used by notification actions. */
internal data class ReminderPreview(val reminders: List<Reminder>, val reminder: Reminder) {
    companion object {
        fun prepare(
            reminders: List<Reminder>,
            account: String,
            taskId: String,
            task: UpcomingReminder?
        ): ReminderPreview? {
            if (task != null && (task.taskId != taskId || task.taskListId.isBlank())) return null
            val records = if (task == null) reminders else ReminderReconciler.merge(reminders, account, listOf(task))
            val reminder = records.firstOrNull {
                it.accountId == account && it.taskId == taskId && it.active
            } ?: return null
            return ReminderPreview(records, reminder)
        }
    }
}
