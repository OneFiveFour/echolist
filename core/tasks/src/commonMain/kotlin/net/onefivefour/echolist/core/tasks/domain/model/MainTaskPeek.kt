package net.onefivefour.echolist.core.tasks.domain.model

import kotlinx.datetime.LocalDate

/**
 * A flattened main-task entry aggregated across all task lists in a single
 * directory. Subtasks are intentionally omitted. Entries are returned grouped
 * per task list and, within each task list, in stored task order.
 */
data class MainTasksSummary(
    val mainTaskId: String,
    val description: String,
    val isDone: Boolean,
    /** Null when no due date is set. */
    val dueDate: LocalDate?,
    val taskListId: String,
    val taskListName: String
)