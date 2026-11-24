package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem
import net.onefivefour.echolist.feature.browser.ui.BrowserHeading
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState

@Composable
internal fun PhoneBrowserHeading(
    breadcrumbs: List<BreadcrumbItem>,
    onFolderClick: (String) -> Unit
) {
    Column {
        if (breadcrumbs.size > 1) {
            BreadcrumbBar(breadcrumbs, onFolderClick)
        }
        BrowserHeading(
            breadcrumbs,
            EchoListTheme.typography.headlineSmall
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun PhoneBrowserHeadingPreview() {
    EchoListTheme(modifier = Modifier) {
        PhoneBrowserHeading(
            breadcrumbs = browserPreviewState.breadcrumbs,
            onFolderClick = {}
        )
    }
}