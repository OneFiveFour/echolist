package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FileOverview(
    fileEntries: List<FileEntry>, isLoading: Boolean, error: String?,
    onFolderClick: (String) -> Unit, onNoteClick: (String) -> Unit, onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier, isDesktop: Boolean = false
) {
    val spacing = if (isDesktop) EchoListTheme.dimensions.m else EchoListTheme.dimensions.s
    val hasTwoColumns = isDesktop && EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (hasTwoColumns) 2 else 1),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(bottom = EchoListTheme.dimensions.l)
    ) {
        when {
            isLoading -> item(key = "loading", span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
            error != null -> item(key = "error", span = { GridItemSpan(maxLineSpan) }) { ErrorContainer(message = error) }
            fileEntries.isEmpty() -> item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(Res.string.browser_empty), style = EchoListTheme.typography.bodyMedium,
                    color = EchoListTheme.materialColors.onSurfaceVariant)
            }
            else -> {
                val folders = fileEntries.filter { it.itemType == ItemType.FOLDER }
                items(folders, key = { it.path }) { entry ->
                    FolderItem(entry.title, entry.metadata as? FileMetadata.Folder, { onFolderClick(entry.path) }, isDesktop = isDesktop)
                }
                val files = fileEntries.filter { it.itemType == ItemType.NOTE || it.itemType == ItemType.TASK_LIST }
                // Fill the unused folder cell so files begin on a new row.
                if (hasTwoColumns && folders.size % 2 != 0 && files.isNotEmpty()) {
                    item(key = "folder-boundary") { Spacer(Modifier) }
                }
                items(files, key = { it.path }) { entry ->
                    when (val metadata = entry.metadata) {
                        is FileMetadata.Note -> NoteItem(metadata.id, entry.title, metadata.preview, onNoteClick, isDesktop = isDesktop)
                        is FileMetadata.TaskList -> TaskItem(metadata.id, entry.title, metadata.doneTaskCount, metadata.totalTaskCount, onTaskClick, isDesktop = isDesktop)
                        else -> Unit
                    }
                }
            }
        }
    }
}
