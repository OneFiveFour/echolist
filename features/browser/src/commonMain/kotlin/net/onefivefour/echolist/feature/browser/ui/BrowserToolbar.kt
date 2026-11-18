package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.core.designsystem.resources.ic_delete
import net.onefivefour.echolist.core.designsystem.resources.ic_edit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes

@Composable
internal fun BrowserToolbar(
    browserUiState: BrowserUiState,
    heading: @Composable () -> Unit,
    onRefresh: () -> Unit,
    onRenameCurrentFolderClick: () -> Unit,
    onDeleteCurrentFolderClick: () -> Unit
) {
    val colors = EchoListTheme.materialColors
    Row(verticalAlignment = Alignment.Bottom) {
        Box(Modifier.weight(1f)) {
            heading()
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(Res.string.refresh_browser),
                tint = colors.onSurfaceVariant
            )
        }

        if (browserUiState.canRenameCurrentFolder) {
            IconButton(onClick = onRenameCurrentFolderClick) {
                Icon(
                    painter = painterResource(resource = SharedRes.drawable.ic_edit),
                    contentDescription = stringResource(Res.string.rename_folder),
                    tint = colors.primary
                )
            }
        }

        if (browserUiState.canDeleteCurrentFolder) {
            IconButton(onClick = onDeleteCurrentFolderClick, enabled = !browserUiState.isDeletingFolder) {
                Icon(
                    painter = painterResource(resource = SharedRes.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.delete_folder),
                    tint = colors.primary
                )
            }
        }
    }
}
