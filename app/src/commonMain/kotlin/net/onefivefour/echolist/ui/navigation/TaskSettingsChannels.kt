package net.onefivefour.echolist.ui.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import net.onefivefour.echolist.core.tasks.domain.TaskSettingsResultSink
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

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