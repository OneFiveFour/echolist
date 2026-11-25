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

internal class TaskSettingsChannels : ViewModel() {

    private class Entry {
        val channel = Channel<TaskSettingsChanges>(Channel.UNLIMITED)
        val results = channel.receiveAsFlow()
    }
    private val entries = mutableMapOf<String, Entry>()

    fun resultsFor(editorId: String): Flow<TaskSettingsChanges> = entry(editorId).results

    fun sinkFor(editorId: String): TaskSettingsResultSink {
        val destination = entry(editorId).channel
        // A late permission response after the editor closed must not reopen its queue.
        return TaskSettingsResultSink { destination.trySend(it) }
    }

    fun retainEditors(editorIds: Set<String>) {
        (entries.keys - editorIds).forEach { entries.remove(it)?.channel?.close() }
    }

    fun clearEditors() = retainEditors(emptySet())

    private fun entry(editorId: String): Entry = entries.getOrPut(editorId) { Entry() }

    override fun onCleared() {
        clearEditors()
    }
}

/** Channels that live exactly as long as their editors are on [backStack]. */
@Composable
internal fun rememberTaskSettingsChannels(backStack: List<NavKey>): TaskSettingsChannels {
    val settingsChannels = koinViewModel<TaskSettingsChannels>()
    val editorIds = backStack.filterIsInstance<EditTaskListRoute>().map { it.editorId }.toSet()

    LaunchedEffect(editorIds) {
        settingsChannels.retainEditors(editorIds)
    }

    return settingsChannels
}
