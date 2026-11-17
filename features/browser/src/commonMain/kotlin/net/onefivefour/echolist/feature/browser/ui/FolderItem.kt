package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FolderItem(title: String, metadata: FileMetadata.Folder?, onClick: () -> Unit,
    modifier: Modifier = Modifier, isDesktop: Boolean = false) {
    val color = EchoListTheme.echoListColorScheme.folderColor
    BrowserItemCard(title, Res.drawable.browser_folder, color, true, isDesktop, onClick, modifier) {
        metadata?.let {
            Text(if (it.childCount == 0) stringResource(Res.string.folder_empty)
                else stringResource(Res.string.folder_item_count, it.childCount),
                modifier = Modifier.padding(top = EchoListTheme.dimensions.xxs),
                style = EchoListTheme.typography.labelSmall.copy(letterSpacing = 0.sp, fontSize = 10.sp, lineHeight = 15.sp), color = color)
        }
    }
}
