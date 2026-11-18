package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        color = EchoListTheme.materialColors.surface, shadowElevation = 16.dp,
        border = BorderStroke(EchoListTheme.dimensions.borderWidth, EchoListTheme.materialColors.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)) {
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
            CreateItemButton(createItemCallbacks, false)
        }
    }
}
