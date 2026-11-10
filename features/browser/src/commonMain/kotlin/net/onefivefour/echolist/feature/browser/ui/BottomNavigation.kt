package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.resources.ic_notifications
import net.onefivefour.echolist.core.designsystem.ui.components.RoundIconButton
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.task_notifications
import org.jetbrains.compose.resources.stringResource
import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes

@Composable
internal fun BottomNavigation(
    createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks(),
    onOpenTaskNotifications: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(
            iconRes = SharedRes.drawable.ic_notifications,
            onClick = onOpenTaskNotifications,
            contentDescription = stringResource(Res.string.task_notifications)
        )
        CreateItemPills(createItemCallbacks = createItemCallbacks)
    }
}
