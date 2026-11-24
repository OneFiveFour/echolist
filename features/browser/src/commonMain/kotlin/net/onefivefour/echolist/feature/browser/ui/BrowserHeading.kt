package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun BrowserHeading(breadcrumbs: List<BreadcrumbItem>, style: TextStyle) {
    Text(
        text = breadcrumbs.lastOrNull()?.label.orEmpty(),
        modifier = Modifier.testTag("browser-heading"),
        style = style,
        color = EchoListTheme.materialColors.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@PreviewComponentLightDark
@Composable
private fun BrowserHeadingHomePreview() {
    EchoListTheme(modifier = Modifier) {
        BrowserHeading(
            breadcrumbs = browserPreviewState.breadcrumbs.take(1),
            style = EchoListTheme.typography.headlineSmall
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun BrowserHeadingPreview() {
    EchoListTheme(modifier = Modifier) {
        BrowserHeading(
            breadcrumbs = browserPreviewState.breadcrumbs,
            style = EchoListTheme.typography.headlineSmall
        )
    }
}
