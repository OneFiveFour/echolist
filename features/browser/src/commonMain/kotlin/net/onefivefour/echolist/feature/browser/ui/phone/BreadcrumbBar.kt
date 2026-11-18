package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_breadcrumb_separator
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BreadcrumbBar(breadcrumbs: List<BreadcrumbItem>) {
    val path = buildAnnotatedString {
        breadcrumbs.dropLast(1).forEach { crumb ->
            append(crumb.label)
            append(" ")
            appendInlineContent("separator", "›")
            append(" ")
        }
    }
    Text(
        text = path,
        modifier = Modifier.fillMaxWidth().testTag("browser-breadcrumbs"),
        style = EchoListTheme.typography.bodySmall.copy(
            fontSize = 9.sp, lineHeight = 13.sp, letterSpacing = 0.sp
        ),
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
