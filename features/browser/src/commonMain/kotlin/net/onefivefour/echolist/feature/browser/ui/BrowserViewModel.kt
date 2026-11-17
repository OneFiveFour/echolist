package net.onefivefour.echolist.feature.browser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.home_title
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.ItemType
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository
import org.jetbrains.compose.resources.getString

internal class BrowserViewModel(
    private val parentDir: String,
    private val fileRepository: FileRepository,
    private val directoryChangeNotifier: DirectoryChangeNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BrowserUiState(
            breadcrumbs = emptyList(),
            isLoading = true
        )
    )
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _navigateToFolder = MutableSharedFlow<String>()
    val navigateToFolder: SharedFlow<String> = _navigateToFolder.asSharedFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
        viewModelScope.launch {
            directoryChangeNotifier.directoryChanged.collect { changedPath ->
                if (changedPath == parentDir) {
                    loadData()
                }
            }
        }
    }

    /**
     * Clears any stale error and reloads data.
     * Called when the composable re-enters composition (e.g. after re-authentication)
     * to ensure a reused ViewModel does not show errors from a previous session.
     */
    fun clearErrorAndReload() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            loadData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                loadData()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onDeleteCurrentFolderClick() {
        if (!_uiState.value.canDeleteCurrentFolder || _uiState.value.isDeletingFolder) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingFolder = true, error = null) }
            val result = fileRepository.deleteFolder(DeleteFolderParams(folderPath = parentDir))
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isDeletingFolder = false) }
                    _navigateToFolder.emit(parentPath(parentDir))
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isDeletingFolder = false,
                            error = exception.message
                        )
                    }
                }
            )
        }
    }

    private suspend fun loadData() {
        val breadcrumbs = resolveBreadcrumbs()
        _uiState.update { current ->
            current.copy(
                breadcrumbs = breadcrumbs
            )
        }
        val result = fileRepository.listFiles(parentDir)
        _uiState.update { current ->
            result.fold(
                onSuccess = { entries ->
                    current.copy(
                        breadcrumbs = breadcrumbs,
                        fileEntries = sortBrowserEntries(entries),
                        isLoading = false,
                        canDeleteCurrentFolder = parentDir.isNotBlank() && entries.isEmpty(),
                        canRenameCurrentFolder = parentDir.isNotBlank(),
                        error = null
                    )
                },
                onFailure = { exception ->
                    current.copy(
                        breadcrumbs = breadcrumbs,
                        fileEntries = emptyList(),
                        isLoading = false,
                        canDeleteCurrentFolder = false,
                        canRenameCurrentFolder = parentDir.isNotBlank(),
                        error = exception.message
                    )
                }
            )
        }
    }

    private suspend fun resolveBreadcrumbs(): List<BreadcrumbItem> =
        buildBreadcrumbs(
            parentDir = parentDir,
            homeTitle = getString(Res.string.home_title)
        )

    private fun parentPath(path: String): String =
        path.trimEnd('/').substringBeforeLast('/', "")
}

internal fun buildBreadcrumbs(parentDir: String, homeTitle: String): List<BreadcrumbItem> {
    val breadcrumbs = mutableListOf(BreadcrumbItem(label = homeTitle, parentDir = ""))
    if (parentDir.isEmpty()) return breadcrumbs

    val segments = parentDir.trimStart('/').trimEnd('/').split('/')
    var accumulated = ""
    for (segment in segments) {
        accumulated = if (accumulated.isEmpty()) segment else "$accumulated/$segment"
        breadcrumbs.add(BreadcrumbItem(label = segment, parentDir = accumulated))
    }
    return breadcrumbs
}

/** Folders sort by name; files share a chronological order regardless of their type.
 * Zero is the protocol fallback for a missing updated_at; equal dates retain server order.
 */
internal fun sortBrowserEntries(entries: List<FileEntry>) =
    entries.sortedWith(compareBy<FileEntry> {
        it.itemType != ItemType.FOLDER
    }.thenBy {
        if (it.itemType == ItemType.FOLDER) it.title.lowercase() else ""
    }.thenBy { it.lastModifiedAt })
