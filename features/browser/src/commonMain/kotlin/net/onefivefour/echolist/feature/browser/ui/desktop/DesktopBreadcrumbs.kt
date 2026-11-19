package net.onefivefour.echolist.feature.browser.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DesktopBreadcrumbs(
    breadcrumbs: List<BreadcrumbItem>,
    onFolderClick: (String) -> Unit
) {
    val colors = EchoListTheme.materialColors
    Column(
        modifier = Modifier.padding(horizontal = EchoListTheme.dimensions.s).testTag("browser-location"),
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
    ) {
        breadcrumbs.forEachIndexed { index, crumb ->
            val isCurrent = index == breadcrumbs.lastIndex
            val icon = when {
                index == 0 -> Res.drawable.browser_location_home
                isCurrent -> Res.drawable.browser_location_current
                else -> Res.drawable.browser_location_folder
            }
            Box {
                if (index > 0) {
                    Box(
                        Modifier.offset(x = 10.dp, y = (-7).dp)
                            .size(width = EchoListTheme.dimensions.borderWidth, height = 10.dp)
                            .background(colors.outlineVariant)
                    )
                }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 34.dp)
                        .background(
                            if (isCurrent) colors.surfaceVariant else colors.surface,
                            EchoListTheme.shapes.small
                        )
                        .clip(EchoListTheme.shapes.small)
                        .then(
                            if (isCurrent) Modifier else Modifier.clickable(role = Role.Button) {
                                onFolderClick(crumb.parentDir)
                            }
                        )
                        .semantics { selected = isCurrent }
                        .testTag("browser-breadcrumb-${crumb.parentDir}")
                        .padding(EchoListTheme.dimensions.s),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
                ) {
                    Icon(
                        painterResource(icon), null, Modifier.size(14.dp),
                        tint = if (isCurrent) colors.onSurface else colors.onSurfaceVariant
                    )
                    Text(
                        crumb.label,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = EchoListTheme.typography.labelMedium.copy(
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isCurrent) colors.onSurface else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun DesktopBreadcrumbsPreview() {
    EchoListTheme(modifier = Modifier) {
        DesktopBreadcrumbs(browserPreviewState.breadcrumbs) {}
    }
}
