package net.onefivefour.echolist.feature.taskssummary.ui

import kotlinx.datetime.LocalDate

internal data class TasksSummaryUiState(
    val isLoading: Boolean = true,
    val groups: List<TasksSummaryGroup> = emptyList(),
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && groups.isEmpty()
}

/**
 * A task list and its visible (not-done) summary rows, preserving server order.
 */
internal data class TasksSummaryGroup(
    val taskListId: String,
    val taskListName: String,
    val tasks: List<TasksSummaryRow>
)

internal data class TasksSummaryRow(
    val mainTaskId: String,
    val description: String,
    val isDone: Boolean,
    val dueDate: LocalDate?
)