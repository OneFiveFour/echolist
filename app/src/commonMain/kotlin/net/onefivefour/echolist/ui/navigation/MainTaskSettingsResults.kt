package net.onefivefour.echolist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import net.onefivefour.echolist.core.tasks.domain.TaskSettingsResultSink
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import org.koin.compose.viewmodel.koinViewModel

/** Carries MainTask settings back to the task-list screen that opened them, one queue per screen. */
internal class MainTaskSettingsResults : ViewModel() {

    private class Entry {
        val channel = Channel<TaskSettingsChanges>(Channel.UNLIMITED)
        val results = channel.receiveAsFlow()
    }
    private val entries = mutableMapOf<String, Entry>()

    fun receiveFor(taskListScreenId: String): Flow<TaskSettingsChanges> = entry(taskListScreenId).results

    fun senderTo(taskListScreenId: String): TaskSettingsResultSink {
        val destination = entry(taskListScreenId).channel
        // A late permission response after its task-list screen closed must not reopen its queue.
        return TaskSettingsResultSink { destination.trySend(it) }
    }

    fun discardExcept(taskListScreenIds: Set<String>) {
        (entries.keys - taskListScreenIds).forEach { entries.remove(it)?.channel?.close() }
    }

    fun discardAll() = discardExcept(emptySet())

    private fun entry(taskListScreenId: String): Entry = entries.getOrPut(taskListScreenId) { Entry() }

    override fun onCleared() {
        discardAll()
    }
}

/** Result queues that live exactly as long as their task-list screens are on [backStack]. */
@Composable
internal fun rememberMainTaskSettingsResults(backStack: List<NavKey>): MainTaskSettingsResults {
    val mainTaskSettingsResults = koinViewModel<MainTaskSettingsResults>()
    val taskListScreenIds = backStack.filterIsInstance<EditTaskListRoute>().map { it.taskListScreenId }.toSet()

    LaunchedEffect(taskListScreenIds) {
        mainTaskSettingsResults.discardExcept(taskListScreenIds)
    }

    return mainTaskSettingsResults
}
