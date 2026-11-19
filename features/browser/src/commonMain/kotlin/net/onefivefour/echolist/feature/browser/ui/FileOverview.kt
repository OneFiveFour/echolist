package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewPhoneLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType

@Composable
internal fun FileOverview(
    fileEntries: List<FileEntry>, isLoading: Boolean, error: String?,
    onFolderClick: (String) -> Unit, onNoteClick: (String) -> Unit, onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier, isDesktop: Boolean = false,
    gridState: LazyGridState = rememberLazyGridState(),
    createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks(),
    folderDraft: FolderDraft? = null,
    onFolderNameChange: (String) -> Unit = {},
    onConfirmFolder: () -> Unit = {},
    onCancelFolder: () -> Unit = {},
    onFolderLongClick: (String) -> Unit = {}
) {
    if (!isLoading && error == null && fileEntries.isEmpty() && folderDraft == null) {
        EmptyFolderContent(createItemCallbacks, isDesktop, modifier)
        return
    }

    val folders = fileEntries.filter { it.itemType == ItemType.FOLDER }.toMutableList()
    val original = folderDraft?.originalEntry
    // A remote refresh must not remove an active editor, even if the original entry disappears.
    if (original != null && folders.none { it.path == original.path }) {
        folders.add(folderDraft.originalIndex.coerceIn(0, folders.size), original)
    }
    val isCreatingFolder = folderDraft != null && !folderDraft.isRenaming

    LaunchedEffect(folderDraft?.id, folderDraft?.focusRequest) {
        if (folderDraft != null) {
            val index = if (original == null) 0 else folders.indexOfFirst { it.path == original.path }.coerceAtLeast(0)
            if (original == null || gridState.layoutInfo.visibleItemsInfo.none { it.index == index }) {
                gridState.scrollToItem(index)
            }
        }
    }

    val spacing = if (isDesktop) EchoListTheme.dimensions.m else EchoListTheme.dimensions.s
    val hasTwoColumns = isDesktop && EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

    LazyVerticalGrid(
        columns = GridCells.Fixed(if (hasTwoColumns) 2 else 1),
        modifier = modifier.fillMaxSize(),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(bottom = EchoListTheme.dimensions.l)
    ) {
        if (isCreatingFolder) {
            val draft = requireNotNull(folderDraft)
            item(key = "folder-draft-${draft.id}") {
                FolderDraftItem(draft, onFolderNameChange, onConfirmFolder, onCancelFolder, isDesktop)
            }
        }
        when {
            isLoading && original == null -> item(key = "loading", span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
            error != null && fileEntries.isEmpty() && original == null -> item(key = "error", span = { GridItemSpan(maxLineSpan) }) { ErrorContainer(message = error) }
            else -> {
                items(folders, key = { "entry:${it.path}" }) { entry ->
                    if (folderDraft != null && entry.path == original?.path) {
                        FolderDraftItem(folderDraft, onFolderNameChange, onConfirmFolder, onCancelFolder, isDesktop)
                    } else {
                        FolderItem(
                            entry.title,
                            entry.metadata as? FileMetadata.Folder,
                            { onFolderClick(entry.path) },
                            isDesktop = isDesktop,
                            onLongClick = { onFolderLongClick(entry.path) }
                        )
                    }
                }
                val files = fileEntries.filter { it.itemType == ItemType.NOTE || it.itemType == ItemType.TASK_LIST }
                // Fill the unused folder cell so files begin on a new row.
                val folderCount = folders.size + if (isCreatingFolder) 1 else 0
                if (hasTwoColumns && folderCount % 2 != 0 && files.isNotEmpty()) {
                    item(key = "folder-boundary") { Spacer(Modifier) }
                }
                items(files, key = { "entry:${it.path}" }) { entry ->
                    when (val metadata = entry.metadata) {
                        is FileMetadata.Note -> NoteItem(metadata.id, entry.title, metadata.preview, onNoteClick, isDesktop = isDesktop)
                        is FileMetadata.TaskList -> TaskItem(metadata.id, entry.title, metadata.doneTaskCount, metadata.totalTaskCount, onTaskClick, isDesktop = isDesktop)
                        else -> Unit
                    }
                }
                if (error != null) {
                    item(key = "error", span = { GridItemSpan(maxLineSpan) }) { ErrorContainer(message = error) }
                }
            }
        }
    }
}

@PreviewPhoneLightDark
@Composable
private fun FileOverviewPreview() {
    EchoListTheme {
        FileOverview(
            fileEntries = browserPreviewState.fileEntries,
            isLoading = false,
            error = null,
            onFolderClick = {},
            onNoteClick = {},
            onTaskClick = {})
    }
}
