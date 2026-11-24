package net.onefivefour.echolist.feature.browser.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme.shapes
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState
import org.jetbrains.compose.resources.painterResource
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_location_current
import net.onefivefour.echolist.feature.browser.resources.browser_location_folder
import net.onefivefour.echolist.feature.browser.resources.browser_location_home
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

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
                        Modifier
                            .offset(
                                x = EchoListTheme.dimensions.m,
                                y = -EchoListTheme.dimensions.s
                            )
                            .size(
                                width = EchoListTheme.dimensions.borderWidth,
                                height = EchoListTheme.dimensions.m
                            )
                            .background(colors.outlineVariant)
                    )
                }

                val backgroundColor = when {
                    isCurrent -> colors.surfaceVariant
                    else -> Color.Transparent
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = EchoListTheme.dimensions.iconSmall)
                        .background(
                            color = backgroundColor,
                            shape = shapes.small
                        )
                        .clip(shapes.small)
                        .then(
                            when {
                                isCurrent -> Modifier
                                else -> Modifier.clickable(role = Role.Button) {
                                    onFolderClick(crumb.parentDir)
                                }
                            }
                        )
                        .semantics { selected = isCurrent }
                        .testTag("browser-breadcrumb-${crumb.parentDir}")
                        .padding(EchoListTheme.dimensions.s),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
                ) {
                    Icon(
                        painter = painterResource(resource = icon),
                        contentDescription = null,
                        modifier = Modifier.size(EchoListTheme.dimensions.l),
                        tint = if (isCurrent) colors.onSurface else colors.onSurfaceVariant
                    )

                    Text(
                        text = crumb.label,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = EchoListTheme.typography.labelMedium.copy(
                            fontWeight = when {
                                isCurrent -> FontWeight.SemiBold
                                else -> FontWeight.Normal
                            }
                        ),
                        color = when {
                            isCurrent -> colors.onSurface
                            else -> colors.onSurfaceVariant
                        }
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