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
    onCancelFolder: () -> Unit = {}
) {
    if (!isLoading && error == null && fileEntries.isEmpty() && folderDraft == null) {
        EmptyFolderContent(createItemCallbacks, isDesktop, modifier)
        return
    }

    LaunchedEffect(folderDraft?.id, folderDraft?.focusRequest) {
        if (folderDraft != null) gridState.scrollToItem(0)
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
        if (folderDraft != null) {
            item(key = "folder-draft-${folderDraft.id}") {
                FolderDraftItem(folderDraft, onFolderNameChange, onConfirmFolder, onCancelFolder, isDesktop)
            }
        }
        when {
            isLoading -> item(key = "loading", span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
            error != null && fileEntries.isEmpty() -> item(key = "error", span = { GridItemSpan(maxLineSpan) }) { ErrorContainer(message = error) }
            else -> {
                val folders = fileEntries.filter { it.itemType == ItemType.FOLDER }
                items(folders, key = { "entry:${it.path}" }) { entry ->
                    FolderItem(entry.title, entry.metadata as? FileMetadata.Folder, { onFolderClick(entry.path) }, isDesktop = isDesktop)
                }
                val files = fileEntries.filter { it.itemType == ItemType.NOTE || it.itemType == ItemType.TASK_LIST }
                // Fill the unused folder cell so files begin on a new row.
                val folderCount = folders.size + if (folderDraft != null) 1 else 0
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
