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
    fileEntries: List<FileEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    onFolderClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDesktop: Boolean = false,
    gridState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(bottom = EchoListTheme.dimensions.l),
    folderDraft: FolderDraft? = null,
    onFolderNameChange: (String) -> Unit = {},
    onConfirmFolder: () -> Unit = {},
    onCancelFolder: () -> Unit = {},
    onFolderLongClick: (String) -> Unit = {}
) {

    if (!isLoading && errorMessage == null && fileEntries.isEmpty() && folderDraft == null) {
        EmptyFolderContent(modifier.padding(contentPadding))
        return
    }

    val folders = fileEntries.filter { it.itemType == ItemType.FOLDER }.toMutableList()
    val original = folderDraft?.originalEntry

    if (original != null && folders.none { it.path == original.path }) {
        folders.add(folderDraft.originalIndex.coerceIn(0, folders.size), original)
    }
    val isCreatingFolder = folderDraft != null && !folderDraft.isRenaming

    LaunchedEffect(folderDraft?.id, folderDraft?.focusRequest) {
        if (folderDraft != null) {
            val index = when (original) {
                null -> 0
                else -> folders
                    .indexOfFirst { it.path == original.path }
                    .coerceAtLeast(0)
            }
            when {
                original == null || gridState.layoutInfo.visibleItemsInfo.none { it.index == index } -> {
                    gridState.scrollToItem(index)
                }
            }
        }
    }

    val spacing = EchoListTheme.dimensions.m
    val hasTwoColumns = isDesktop && EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

    val columnCount = when {
        hasTwoColumns -> 2
        else -> 1
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = modifier.fillMaxSize(),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = contentPadding
    ) {

        if (isCreatingFolder) {
            item(key = "folder-draft-${folderDraft.id}") {
                FolderDraftItem(folderDraft, onFolderNameChange, onConfirmFolder, onCancelFolder)
            }
        }

        val isLoading = isLoading && original == null
        val hasError = errorMessage != null && fileEntries.isEmpty() && original == null
        when {

            isLoading -> item(
                key = "loading",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                LoadingIndicator()
            }

            hasError -> item(
                key = "error",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                ErrorContainer(message = errorMessage)
            }

            else -> {
                items(folders, key = { "entry:${it.path}" }) { entry ->
                    if (folderDraft != null && entry.path == original?.path) {
                        FolderDraftItem(
                            draft = folderDraft,
                            onNameChange = onFolderNameChange,
                            onConfirm = onConfirmFolder,
                            onCancel = onCancelFolder
                        )
                    } else {
                        FolderItem(
                            title = entry.title,
                            metadata = entry.metadata as? FileMetadata.Folder,
                            onClick = { onFolderClick(entry.path) },
                            onLongClick = { onFolderLongClick(entry.path) }
                        )
                    }
                }

                val files = fileEntries
                    .filter { it.itemType == ItemType.NOTE || it.itemType == ItemType.TASK_LIST }

                val folderCount = folders.size + if (isCreatingFolder) 1 else 0
                if (hasTwoColumns && folderCount % 2 != 0 && files.isNotEmpty()) {
                    item(key = "folder-boundary") { Spacer(Modifier) }
                }

                items(files, key = { "entry:${it.path}" }) { entry ->
                    when (val metadata = entry.metadata) {
                        is FileMetadata.Note -> NoteItem(
                            id = metadata.id,
                            title = entry.title,
                            preview = metadata.preview,
                            onClick = onNoteClick
                        )
                        is FileMetadata.TaskList -> TaskListItem(
                            id = metadata.id,
                            title = entry.title,
                            doneTaskCount = metadata.doneTaskCount,
                            totalTaskCount = metadata.totalTaskCount,
                            overdueTaskCount = metadata.overdueTaskCount,
                            dueSoonTaskCount = metadata.dueSoonTaskCount,
                            onClick = onTaskClick
                        )

                        else -> Unit
                    }
                }

                if (errorMessage != null) {
                    item(
                        key = "error",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        ErrorContainer(message = errorMessage)
                    }
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
            errorMessage = null,
            onFolderClick = {},
            onNoteClick = {},
            onTaskClick = {})
    }
}
