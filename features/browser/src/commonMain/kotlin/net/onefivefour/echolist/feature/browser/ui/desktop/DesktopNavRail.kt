package net.onefivefour.echolist.feature.browser.ui.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewDesktopLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_browse
import net.onefivefour.echolist.feature.browser.resources.browser_due
import net.onefivefour.echolist.feature.browser.resources.browser_remind
import net.onefivefour.echolist.feature.browser.resources.nav_browse
import net.onefivefour.echolist.feature.browser.resources.nav_due
import net.onefivefour.echolist.feature.browser.resources.nav_reminders
import net.onefivefour.echolist.feature.browser.ui.BrowserNavButton
import net.onefivefour.echolist.feature.browser.ui.BrowserUiState
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopNavRail(
    colors: ColorScheme,
    navToTasksSummary: () -> Unit,
    navToRemindersSummary: () -> Unit,
    browserUiState: BrowserUiState,
    onFolderClick: (String) -> Unit
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
                .padding(
                    horizontal = EchoListTheme.dimensions.xl,
                    vertical = EchoListTheme.dimensions.xxl
                )
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(EchoListTheme.dimensions.xxl))
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
                // The badge stays hidden until the backend supplies a global due-task count.
                onClick = navToTasksSummary
            )
            Spacer(Modifier.height(EchoListTheme.dimensions.xs))
            BrowserNavButton(
                label = stringResource(resource = Res.string.nav_reminders),
                icon = Res.drawable.browser_remind,
                isSelected = false,
                isDesktop = true,
                onClick = navToRemindersSummary
            )
            Spacer(Modifier.height(EchoListTheme.dimensions.xxl))

            DesktopBreadcrumbs(
                breadcrumbs = browserUiState.breadcrumbs,
                onFolderClick = onFolderClick
            )
        }
    }
}

@PreviewDesktopLightDark
@Composable
private fun DesktopNavRailPreview() {
    EchoListTheme {
        DesktopNavRail(
            colors = EchoListTheme.materialColors,
            navToTasksSummary = {},
            navToRemindersSummary = {},
            browserUiState = browserPreviewState,
            onFolderClick = {}
        )
    }
}