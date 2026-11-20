package net.onefivefour.echolist.feature.browser.ui.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem
import net.onefivefour.echolist.feature.browser.ui.BrowserHeading
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState

@Composable
internal fun DesktopBrowserHeading(breadcrumbs: List<BreadcrumbItem>) {
    BrowserHeading(
        breadcrumbs = breadcrumbs,
        style = EchoListTheme.typography.displaySmall
    )
}

@PreviewComponentLightDark
@Composable
private fun DesktopBrowserHeadingPreview() {
    EchoListTheme(modifier = Modifier) {
        DesktopBrowserHeading(browserPreviewState.breadcrumbs)
    }
}
