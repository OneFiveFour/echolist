package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes
import net.onefivefour.echolist.core.designsystem.resources.ic_delete
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BrowserToolbar(
    browserUiState: BrowserUiState,
    heading: @Composable () -> Unit,
    onDeleteCurrentFolderClick: () -> Unit,
    onLogoutClick: (() -> Unit)? = null
) {
    val colors = EchoListTheme.materialColors

    Row(verticalAlignment = Alignment.CenterVertically) {

        Box(Modifier
            .weight(1f)
            .padding(start = EchoListTheme.dimensions.l + EchoListTheme.dimensions.borderWidth)
        ) {
            heading()
        }

        if (browserUiState.canDeleteCurrentFolder) {
            IconButton(
                onClick = onDeleteCurrentFolderClick,
                enabled = !browserUiState.isDeletingFolder && browserUiState.folderDraft == null
            ) {
                Icon(
                    painter = painterResource(resource = SharedRes.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.delete_folder),
                    tint = colors.primary
                )
            }
        }

        if (onLogoutClick != null) {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_logout),
                    contentDescription = stringResource(Res.string.logout),
                    tint = colors.primary
                )
            }
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun BrowserToolbarPreview() {
    EchoListTheme(modifier = Modifier) {
        BrowserToolbar(
            browserUiState = browserPreviewState,
            heading = {
                BrowserHeading(
                    breadcrumbs = browserPreviewState.breadcrumbs,
                    style = EchoListTheme.typography.headlineSmall
                )
            },
            onDeleteCurrentFolderClick = {},
            onLogoutClick = {}
        )
    }
}
