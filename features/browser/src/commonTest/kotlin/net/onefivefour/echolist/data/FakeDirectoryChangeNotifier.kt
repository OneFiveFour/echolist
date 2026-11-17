package net.onefivefour.echolist.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier

internal class FakeDirectoryChangeNotifier : DirectoryChangeNotifier {
    override val directoryChanged: SharedFlow<String>
        field = MutableSharedFlow<String>()

    val emissions = mutableListOf<String>()

    override suspend fun notifyChanged(path: String) {
        emissions.add(path)
        directoryChanged.emit(path)
    }
}