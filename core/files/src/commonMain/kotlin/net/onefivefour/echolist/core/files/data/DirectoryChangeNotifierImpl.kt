package net.onefivefour.echolist.core.files.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier

internal class DirectoryChangeNotifierImpl : DirectoryChangeNotifier {
    override val directoryChanged: SharedFlow<String>
        field = MutableSharedFlow<String>()

    override suspend fun notifyChanged(path: String) {
        directoryChanged.emit(path)
        // Also notify all ancestor directories so parent ViewModels refresh
        var remaining = path
        while (remaining.isNotEmpty()) {
            val lastSep = remaining.lastIndexOf('/')
            remaining = if (lastSep >= 0) remaining.substring(0, lastSep) else ""
            directoryChanged.emit(remaining)
        }
    }
}