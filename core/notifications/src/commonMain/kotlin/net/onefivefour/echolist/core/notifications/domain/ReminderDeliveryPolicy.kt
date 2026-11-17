package net.onefivefour.echolist.core.notifications.domain

import kotlinx.datetime.LocalDate

internal enum class ReminderDeliveryAction { CANCEL_PENDING, DELIVER, SCHEDULE }

/** Delivery decisions only; permission checks, posting and history writes stay in the coordinator. */
internal object ReminderDeliveryPolicy {
    fun action(state: ReminderState, reminder: Reminder, today: () -> LocalDate): ReminderDeliveryAction = when {
        !isPending(state, reminder) -> ReminderDeliveryAction.CANCEL_PENDING
        LocalDate.parse(reminder.dueDate) <= today() -> ReminderDeliveryAction.DELIVER
        else -> ReminderDeliveryAction.SCHEDULE
    }

    fun canDeliver(
        state: ReminderState,
        reminder: Reminder,
        currentAccount: String?,
        today: () -> LocalDate
    ): Boolean = currentAccount == reminder.accountId && state.activeAccount == reminder.accountId &&
        action(state, reminder, today) == ReminderDeliveryAction.DELIVER

    private fun isPending(state: ReminderState, reminder: Reminder): Boolean =
        reminder.active && state.preferences[reminder.key] != false && reminder.dueDate !in reminder.deliveredDates
}
