package net.onefivefour.echolist.feature.browser.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewDesktopLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.BrowserUiState
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState

@Composable
internal fun DesktopBrowserNavigation(
    browserUiState: BrowserUiState,
    navToTasksSummary: () -> Unit,
    navToRemindersSummary: () -> Unit,
    onFolderClick: (String) -> Unit
) {
    Row {
        DesktopNavRail(
            colors = EchoListTheme.materialColors,
            navToTasksSummary = navToTasksSummary,
            navToRemindersSummary = navToRemindersSummary,
            browserUiState = browserUiState,
            onFolderClick = onFolderClick
        )
        Box(
            Modifier.width(EchoListTheme.dimensions.borderWidth)
                .fillMaxHeight().background(EchoListTheme.materialColors.outlineVariant)
        )
    }
}

@PreviewDesktopLightDark
@Composable
private fun DesktopBrowserNavigationPreview() {
    EchoListTheme {
        DesktopBrowserNavigation(
            browserUiState = browserPreviewState,
            navToTasksSummary = {},
            navToRemindersSummary = {},
            onFolderClick = {})
    }
}
