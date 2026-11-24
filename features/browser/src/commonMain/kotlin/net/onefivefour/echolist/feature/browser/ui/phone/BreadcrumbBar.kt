package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_breadcrumb_separator
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BreadcrumbBar(
    breadcrumbs: List<BreadcrumbItem>,
    onFolderClick: (String) -> Unit
) {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(color = EchoListTheme.materialColors.onSurfaceVariant)
    )

    val path = buildAnnotatedString {
        breadcrumbs
            .dropLast(1)
            .forEach { crumb ->
                withLink(
                    LinkAnnotation.Clickable(
                        tag = crumb.parentDir,
                        styles = linkStyles,
                        linkInteractionListener = { onFolderClick(crumb.parentDir) }
                    )
                ) {
                    append(crumb.label)
                }
                append(" ")
                appendInlineContent("separator", "›")
                append(" ")
            }
    }

    Text(
        text = path,
        modifier = Modifier.fillMaxWidth().testTag("browser-breadcrumbs"),
        style = EchoListTheme.typography.labelMedium,
        color = EchoListTheme.materialColors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        inlineContent = mapOf(
            "separator" to InlineTextContent(
                Placeholder(11.sp, 11.sp, PlaceholderVerticalAlign.Center)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.browser_breadcrumb_separator),
                    contentDescription = null,
                    tint = EchoListTheme.materialColors.onSurfaceVariant
                )
            }
        )
    )
}

@PreviewComponentLightDark
@Composable
private fun BreadcrumbBarPreview() {
    EchoListTheme(modifier = Modifier) {
        BreadcrumbBar(
            breadcrumbs = browserPreviewState.breadcrumbs,
            onFolderClick = {}
        )
    }
}