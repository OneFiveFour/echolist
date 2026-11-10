package net.onefivefour.echolist.core.tasks.domain.model

import kotlinx.datetime.LocalDate

data class UpcomingMainTask(
    val taskId: String,
    val title: String,
    val dueDate: LocalDate,
    val taskListId: String,
    /** Null when an older backend omits the optional field. */
    val isRecurring: Boolean? = null
)
