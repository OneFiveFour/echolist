package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.feature.browser.ui.BrowserNavButton
import net.onefivefour.echolist.feature.browser.ui.CreateItemButton
import net.onefivefour.echolist.feature.browser.ui.CreateItemCallbacks
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BottomNavigation(
    createItemCallbacks: CreateItemCallbacks,
    onOpenRemindersSummary: () -> Unit,
    navigateToTasksSummary: () -> Unit
) {
    Surface(
        shape = EchoListTheme.shapes.extraLarge,
        color = EchoListTheme.materialColors.surface,
        shadowElevation = EchoListTheme.dimensions.l,
        border = BorderStroke(
            width = EchoListTheme.dimensions.borderWidth,
            color = EchoListTheme.materialColors.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = EchoListTheme.dimensions.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
        ) {
            BrowserNavButton(
                label = stringResource(resource = Res.string.nav_browse),
                icon = Res.drawable.browser_browse,
                isSelected = true,
                isDesktop = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            BrowserNavButton(
                label = stringResource(resource = Res.string.nav_due),
                icon = Res.drawable.browser_due,
                isSelected = false,
                isDesktop = false,
                onClick = navigateToTasksSummary,
                modifier = Modifier.weight(1f)
            )
            BrowserNavButton(
                label = stringResource(resource = Res.string.nav_remind),
                icon = Res.drawable.browser_remind,
                isSelected = false,
                isDesktop = false,
                onClick = onOpenRemindersSummary,
                modifier = Modifier.weight(1f)
            )
            CreateItemButton(createItemCallbacks)
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun BottomNavigationPreview() {
    EchoListTheme(modifier = Modifier) {
        BottomNavigation(
            createItemCallbacks = CreateItemCallbacks(),
            onOpenRemindersSummary = {},
            navigateToTasksSummary = {})
    }
}
