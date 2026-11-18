package net.onefivefour.echolist.feature.browser.ui.desktop

import net.onefivefour.echolist.feature.browser.ui.BrowserNavButton
import net.onefivefour.echolist.feature.browser.ui.BrowserUiState

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_browse
import net.onefivefour.echolist.feature.browser.resources.browser_due
import net.onefivefour.echolist.feature.browser.resources.browser_remind
import net.onefivefour.echolist.feature.browser.resources.location
import net.onefivefour.echolist.feature.browser.resources.nav_browse
import net.onefivefour.echolist.feature.browser.resources.nav_due
import net.onefivefour.echolist.feature.browser.resources.nav_reminders
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopNavRail(
    colors: ColorScheme,
    navToTasksSummary: () -> Unit,
    navToRemindersSummary: () -> Unit,
    browserUiState: BrowserUiState
) {
    Surface(
        color = colors.surface,
        modifier = Modifier
            .width(264.dp)
            .fillMaxHeight()
            .testTag("browser-sidebar")
    ) {
        Column(
            Modifier
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(40.dp))
            BrowserNavButton(
                label = stringResource(resource = Res.string.nav_browse),
                icon = Res.drawable.browser_browse,
                isSelected = true,
                isDesktop = true,
                onClick = {}
            )
            Spacer(Modifier.height(EchoListTheme.dimensions.xs))
            BrowserNavButton(
                label = stringResource(resource = Res.string.nav_due),
                icon = Res.drawable.browser_due,
                isSelected = false,
                isDesktop = true,
                onClick = navToTasksSummary,
                hasDueBadge = true
            )
            Spacer(Modifier.height(EchoListTheme.dimensions.xs))
            BrowserNavButton(
                label = stringResource(resource = Res.string.nav_reminders),
                icon = Res.drawable.browser_remind,
                isSelected = false,
                isDesktop = true,
                onClick = navToRemindersSummary
            )
            Spacer(Modifier.height(40.dp))

            Text(
                text = stringResource(resource = Res.string.location),
                style = EchoListTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                color = colors.outline
            )
            Spacer(Modifier.height(EchoListTheme.dimensions.m))

            browserUiState.breadcrumbs.forEach { crumb ->
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = EchoListTheme.shapes.small,
                    color = when (crumb) {
                        browserUiState.breadcrumbs.lastOrNull() -> colors.surfaceVariant
                        else -> colors.surface
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 34.dp)
                            .padding(EchoListTheme.dimensions.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.browser_browse),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.onSurface
                        )
                        Spacer(Modifier.width(EchoListTheme.dimensions.s))
                        Text(
                            text = crumb.label,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = EchoListTheme.typography.labelSmall,
                            color = colors.onSurface
                        )
                    }
                }
            }
        }
    }
}