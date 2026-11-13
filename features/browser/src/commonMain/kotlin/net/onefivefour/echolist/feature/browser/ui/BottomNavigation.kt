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
import net.onefivefour.echolist.feature.browser.resources.create_folder
import net.onefivefour.echolist.feature.browser.resources.create_note
import net.onefivefour.echolist.feature.browser.resources.create_task_list
import net.onefivefour.echolist.feature.browser.resources.ic_arrow_back
import net.onefivefour.echolist.feature.browser.resources.ic_create_folder
import net.onefivefour.echolist.feature.browser.resources.ic_create_note
import net.onefivefour.echolist.feature.browser.resources.ic_create_task_list
import net.onefivefour.echolist.feature.browser.resources.ic_task_peek
import net.onefivefour.echolist.feature.browser.resources.navigate_back
import net.onefivefour.echolist.feature.browser.resources.task_notifications
import net.onefivefour.echolist.feature.browser.resources.task_peek
import org.jetbrains.compose.resources.stringResource
import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes

@Composable
internal fun BottomNavigation(
    createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks(),
    onOpenTaskNotifications: () -> Unit = {},
    onTaskPeekClick: () -> Unit = {},
    canNavigateBack: Boolean = false,
    onBackClick: () -> Unit = {}
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
        RoundIconButton(
            iconRes = Res.drawable.ic_create_folder,
            onClick = createItemCallbacks.onCreateFolder,
            contentDescription = stringResource(Res.string.create_folder)
        )
        RoundIconButton(
            iconRes = Res.drawable.ic_create_note,
            onClick = createItemCallbacks.onCreateNote,
            contentDescription = stringResource(Res.string.create_note)
        )
        RoundIconButton(
            iconRes = Res.drawable.ic_create_task_list,
            onClick = createItemCallbacks.onCreateTaskList,
            contentDescription = stringResource(Res.string.create_task_list)
        )
        RoundIconButton(
            iconRes = Res.drawable.ic_task_peek,
            onClick = onTaskPeekClick,
            contentDescription = stringResource(Res.string.task_peek)
        )
        if (canNavigateBack) {
            RoundIconButton(
                iconRes = Res.drawable.ic_arrow_back,
                onClick = onBackClick,
                containerColor = EchoListTheme.materialColors.primary,
                contentColor = EchoListTheme.materialColors.onPrimary,
                contentDescription = stringResource(Res.string.navigate_back)
            )
        }
    }
}
