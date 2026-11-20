package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_home
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BrowserHeading(breadcrumbs: List<BreadcrumbItem>, style: TextStyle) {

    val isHome = breadcrumbs.size == 1 && breadcrumbs.first().parentDir.isEmpty()

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isHome) {
            Icon(
                painterResource(resource = Res.drawable.browser_home),
                contentDescription = null,
                modifier = Modifier.size(EchoListTheme.dimensions.xl).testTag("browser-home-icon"),
                tint = EchoListTheme.materialColors.onSurface
            )
            Spacer(Modifier.width(EchoListTheme.dimensions.s))
        }
        Text(
            text = breadcrumbs.lastOrNull()?.label.orEmpty(),
            modifier = Modifier.weight(1f).testTag("browser-heading"),
            style = style,
            color = EchoListTheme.materialColors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
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
