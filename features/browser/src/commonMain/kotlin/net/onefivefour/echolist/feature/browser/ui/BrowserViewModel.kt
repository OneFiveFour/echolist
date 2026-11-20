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
import net.onefivefour.echolist.core.files.domain.normalizePath
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.folder_create_failed
import net.onefivefour.echolist.feature.browser.resources.folder_rename_failed
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

    private val unpersistedFolders = mutableMapOf<String, FileEntry>()
    private val renamedFolderPaths = mutableMapOf<String, String>()

    fun startFolderDraft() = startFolderEditing(null)

    fun startFolderRename(path: String) {
        val entry = uiState.value.fileEntries.firstOrNull { it.path == path && it.itemType == ItemType.FOLDER }
        val wasEntryFound = entry != null
        val isInFolderDraft = uiState.value.folderDraft != null
        if (isInFolderDraft || wasEntryFound) {
            startFolderEditing(entry)
        }
    }

    private fun startFolderEditing(entry: FileEntry?) {
        val currentUiState = uiState.value

        if (currentUiState.isDeletingFolder || currentUiState.folderDraft?.isSaving == true) {
            return
        }

        uiState.update { currentUiState ->

            val newFolderDraft = currentUiState.folderDraft?.let { draft ->
                draft.copy(focusRequest = draft.focusRequest + 1)
            } ?: FolderDraft(
                id = ++nextDraftId,
                name = entry?.title.orEmpty(),
                originalEntry = entry,
                originalIndex = currentUiState.fileEntries
                    .filter { file -> file.itemType == ItemType.FOLDER }
                    .indexOf(entry)
                    .coerceAtLeast(0)
            )

            currentUiState.copy(
                folderDraft = newFolderDraft
            )
        }
    }

    fun dismissFolderDraft() {
        if (uiState.value.folderDraft?.isSaving == true) {
            return
        }
        uiState.update { it.copy(folderDraft = null) }
    }

    fun onFolderNameChange(name: String) {
        uiState.update { currentUiState ->
            val draft = currentUiState.folderDraft ?: return@update currentUiState
            when {
                draft.isSaving -> currentUiState
                else -> currentUiState.copy(folderDraft = draft.copy(name = name, error = null))
            }
        }
    }

    fun confirmFolderDraft() {
        val draft = uiState.value.folderDraft ?: return
        if (!draft.canConfirm) return

        val name = draft.name.trim()
        val original = draft.originalEntry

        if (original != null && name == original.title) {
            dismissFolderDraft()
            return
        }

        uiState.update { it.copy(folderDraft = draft.copy(name = name, isSaving = true)) }

        viewModelScope.launch {
            try {

                val result = if (original == null) {
                    fileRepository.createFolder(
                        CreateFolderParams(
                            parentDir = parentDir,
                            name = name
                        )
                    )
                } else {
                    fileRepository.updateFolder(
                        UpdateFolderParams(
                            folderPath = normalizePath(path = original.path),
                            newName = name
                        )
                    )
                }

                result.fold(
                    onSuccess = { folder ->

                        val metadata = if (original != null) {
                            uiState.value.fileEntries
                                .firstOrNull { it.path == original.path }
                                ?.metadata
                                ?: original.metadata
                        } else {
                            FileMetadata.Folder(0)
                        }

                        val entry = FileEntry(
                            path = folder.path,
                            title = folder.name,
                            itemType = ItemType.FOLDER,
                            metadata = metadata
                        )

                        if (original != null && original.path != entry.path) {
                            unpersistedFolders.remove(original.path)
                            renamedFolderPaths.entries.forEach { pending ->
                                if (pending.value == original.path) {
                                    pending.setValue(entry.path)
                                }
                            }
                            renamedFolderPaths[original.path] = entry.path
                        }
                        unpersistedFolders[entry.path] = entry

                        loadRevision++
                        uiState.update { current ->
                            current.copy(
                                folderDraft = null,
                                fileEntries = sortBrowserEntries(
                                    current.fileEntries
                                    .filterNot {
                                        it.path == entry.path || it.path == original?.path
                                    } + entry
                                ),
                                isLoading = false,
                                canDeleteCurrentFolder = false,
                                error = null
                            )
                        }

                        loadData()
                    },

                    onFailure = { exception ->
                        if (exception is CancellationException) throw exception
                        val message = exception.message ?: getString(
                            when {
                                draft.isRenaming -> Res.string.folder_rename_failed
                                else -> Res.string.folder_create_failed
                            }
                        )
                        uiState.update { current ->
                            current.copy(
                                folderDraft = current.folderDraft?.copy(isSaving = false, error = message)
                            )
                        }
                    }
                )

            } finally {
                uiState.update { current ->
                    when (current.folderDraft?.id) {
                        draft.id -> current.copy(folderDraft = current.folderDraft.copy(isSaving = false))
                        else -> current
                    }
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
            current.copy(breadcrumbs = breadcrumbs)
        }

        val result = fileRepository.listFiles(parentDir)

        if (revision != loadRevision) return

        val entries = result.getOrNull()?.let { remoteEntries ->
            val remotePaths = remoteEntries.map { it.path }.toSet()
            unpersistedFolders.keys.removeAll { path ->
                path in remotePaths && renamedFolderPaths.none { (oldPath, newPath) ->
                    newPath == path && oldPath in remotePaths
                }
            }
            renamedFolderPaths.entries.removeAll { (oldPath, newPath) ->
                oldPath !in remotePaths && newPath in remotePaths
            }
            remoteEntries.filterNot { it.path in renamedFolderPaths || it.path in unpersistedFolders } +
                unpersistedFolders.values
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
                        error = null
                    )
                },
                onFailure = { exception ->
                    current.copy(
                        breadcrumbs = breadcrumbs,
                        fileEntries = current.fileEntries,
                        isLoading = false,
                        canDeleteCurrentFolder = false,
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
