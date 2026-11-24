package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import org.jetbrains.compose.resources.stringResource
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_folder
import net.onefivefour.echolist.feature.browser.resources.folder_empty
import net.onefivefour.echolist.feature.browser.resources.folder_item_count
import net.onefivefour.echolist.feature.browser.resources.rename_folder

@Composable
internal fun FolderItem(
    title: String,
    metadata: FileMetadata.Folder?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    val color = EchoListTheme.echoListColorScheme.folderColor

    BrowserItemCard(
        title = title,
        icon = Res.drawable.browser_folder,
        accent = color,
        isFolder = true,
        onClick = onClick,
        modifier = modifier,
        onLongClick = onLongClick,
        onLongClickLabel = stringResource(Res.string.rename_folder)
    ) {
        metadata?.let {
            Text(
                when (it.childCount) {
                    0 -> stringResource(Res.string.folder_empty)
                    else -> stringResource(Res.string.folder_item_count, it.childCount)
                },
                modifier = Modifier.padding(top = EchoListTheme.dimensions.xxs),
                style = EchoListTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun FolderItemPreview() {
    EchoListTheme(modifier = Modifier) {
        FolderItem(
            title = "Design",
            metadata = FileMetadata.Folder(3),
            onClick = {}
        )
    }
}