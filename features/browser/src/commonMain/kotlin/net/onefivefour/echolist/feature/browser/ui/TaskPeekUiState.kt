package net.onefivefour.echolist.feature.browser.ui

import kotlinx.datetime.LocalDate

internal data class TasksSummaryUiState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val groups: List<TasksSummaryGroup> = emptyList(),
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && groups.isEmpty()
}

/**
 * A task list and its visible (not-done) peek rows, preserving server order.
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