package net.onefivefour.echolist.feature.taskssummary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.tasks.domain.model.MainTasksSummary
import net.onefivefour.echolist.core.tasks.domain.repository.TaskListRepository

internal class TasksSummaryViewModel(
    private val parentDir: String,
    private val taskListRepository: TaskListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksSummaryUiState())
    val uiState: StateFlow<TasksSummaryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = taskListRepository.listMainTasksSummary(parentDir)
            _uiState.update { current ->
                result.fold(
                    onSuccess = { tasks ->
                        current.copy(
                            isLoading = false,
                            groups = groupVisibleTasks(tasks),
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        current.copy(
                            isLoading = false,
                            groups = emptyList(),
                            error = exception.message
                        )
                    }
                )
            }
        }
    }
}

/**
 * Groups summary entries by task list, preserving the server's grouping and order,
 * hides done tasks, and drops task lists that have no visible tasks left.
 */
internal fun groupVisibleTasks(tasks: List<MainTasksSummary>): List<TasksSummaryGroup> {
    val visible = tasks.filterNot { it.isDone }
    val groups = mutableListOf<TasksSummaryGroup>()
    val indexByListId = mutableMapOf<String, Int>()
    for (task in visible) {
        val row = TasksSummaryRow(
            mainTaskId = task.mainTaskId,
            description = task.description,
            isDone = task.isDone,
            dueDate = task.dueDate
        )
        val existingIndex = indexByListId[task.taskListId]
        if (existingIndex == null) {
            indexByListId[task.taskListId] = groups.size
            groups.add(
                TasksSummaryGroup(
                    taskListId = task.taskListId,
                    taskListName = task.taskListName,
                    tasks = listOf(row)
                )
            )
        } else {
            val group = groups[existingIndex]
            groups[existingIndex] = group.copy(tasks = group.tasks + row)
        }
    }
    return groups
}