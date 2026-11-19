package net.onefivefour.echolist.feature.browser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.folder_create_failed
import net.onefivefour.echolist.feature.browser.resources.home_title
import org.jetbrains.compose.resources.getString

internal class BrowserViewModel(
    private val parentDir: String,
    private val fileRepository: FileRepository,
    private val directoryChangeNotifier: DirectoryChangeNotifier
) : ViewModel() {

    val uiState: StateFlow<BrowserUiState>
        field = MutableStateFlow(
            BrowserUiState(
                breadcrumbs = emptyList(),
                isLoading = true
            )
        )

    val navigateBack: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

    private var nextDraftId = 0L
    private var loadRevision = 0L
    // Keep successful local creations until a server listing acknowledges them.
    private val unacknowledgedFolders = mutableMapOf<String, FileEntry>()

    fun startFolderDraft() {
        val current = uiState.value
        if (current.isDeletingFolder || current.folderDraft?.isSaving == true) return
        uiState.update {
            it.copy(folderDraft = it.folderDraft?.let { draft ->
                draft.copy(focusRequest = draft.focusRequest + 1)
            } ?: FolderDraft(id = ++nextDraftId))
        }
    }

    fun dismissFolderDraft() {
        if (uiState.value.folderDraft?.isSaving == true) return
        uiState.update { it.copy(folderDraft = null) }
    }

    fun onFolderNameChange(name: String) {
        uiState.update { current ->
            val draft = current.folderDraft ?: return@update current
            if (draft.isSaving) current
            else current.copy(folderDraft = draft.copy(name = name, error = null))
        }
    }

    fun confirmFolderDraft() {
        val draft = uiState.value.folderDraft ?: return
        if (!draft.canConfirm) return
        val name = draft.name.trim()
        uiState.update { it.copy(folderDraft = draft.copy(name = name, isSaving = true)) }
        viewModelScope.launch {
            try {
                fileRepository.createFolder(CreateFolderParams(parentDir, name)).fold(
                    onSuccess = { folder ->
                        val entry = FileEntry(folder.path, folder.name, ItemType.FOLDER, FileMetadata.Folder(0))
                        unacknowledgedFolders[entry.path] = entry
                        // Invalidate reads begun before this mutation completed.
                        loadRevision++
                        uiState.update { current ->
                            current.copy(
                                folderDraft = null,
                                fileEntries = sortBrowserEntries(current.fileEntries.filterNot { it.path == entry.path } + entry),
                                isLoading = false,
                                canDeleteCurrentFolder = false,
                                error = null
                            )
                        }
                        loadData()
                    },
                    onFailure = { exception ->
                        if (exception is CancellationException) throw exception
                        val message = exception.message ?: getString(Res.string.folder_create_failed)
                        uiState.update { current ->
                            current.copy(
                                folderDraft = current.folderDraft?.copy(isSaving = false, error = message)
                            )
                        }
                    }
                )
            } finally {
                uiState.update { current ->
                    if (current.folderDraft?.id == draft.id) {
                        current.copy(folderDraft = current.folderDraft.copy(isSaving = false))
                    } else current
                }
            }
        }
    }

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

    fun clearErrorAndReload() {
        viewModelScope.launch {
            uiState.update { it.copy(error = null) }
            loadData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            uiState.update { it.copy(isRefreshing = true) }
            try {
                loadData()
            } finally {
                uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onDeleteCurrentFolderClick() {
        if (!uiState.value.canDeleteCurrentFolder || uiState.value.isDeletingFolder || uiState.value.folderDraft != null) return

        viewModelScope.launch {
            uiState.update { it.copy(isDeletingFolder = true, error = null) }
            val result = fileRepository.deleteFolder(DeleteFolderParams(folderPath = parentDir))
            result.fold(
                onSuccess = {
                    uiState.update { it.copy(isDeletingFolder = false) }
                    navigateBack.emit(Unit)
                },
                onFailure = { exception ->
                    uiState.update {
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
        val revision = ++loadRevision
        val breadcrumbs = resolveBreadcrumbs()
        uiState.update { current ->
            current.copy(
                breadcrumbs = breadcrumbs
            )
        }
        val result = fileRepository.listFiles(parentDir)
        if (revision != loadRevision) return
        val entries = result.getOrNull()?.let { remoteEntries ->
            remoteEntries.forEach { unacknowledgedFolders.remove(it.path) }
            remoteEntries + unacknowledgedFolders.values.filter { local -> remoteEntries.none { it.path == local.path } }
        }
        uiState.update { current ->
            result.fold(
                onSuccess = { _ ->
                    val loadedEntries = requireNotNull(entries)
                    current.copy(
                        breadcrumbs = breadcrumbs,
                        fileEntries = sortBrowserEntries(loadedEntries),
                        isLoading = false,
                        canDeleteCurrentFolder = parentDir.isNotBlank() && loadedEntries.isEmpty(),
                        canRenameCurrentFolder = parentDir.isNotBlank(),
                        error = null
                    )
                },
                onFailure = { exception ->
                    current.copy(
                        breadcrumbs = breadcrumbs,
                        fileEntries = current.fileEntries,
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

internal fun sortBrowserEntries(entries: List<FileEntry>) =
    entries.sortedWith(compareBy<FileEntry> {
        it.itemType != ItemType.FOLDER
    }.thenBy {
        if (it.itemType == ItemType.FOLDER) it.title.lowercase() else ""
    }.thenBy { it.lastModified })
