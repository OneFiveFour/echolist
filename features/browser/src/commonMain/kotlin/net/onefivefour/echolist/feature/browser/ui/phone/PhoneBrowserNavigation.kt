package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.CreateItemCallbacks

@Composable
internal fun PhoneBrowserNavigation(
    createItemCallbacks: CreateItemCallbacks,
    navToTasksSummary: () -> Unit,
    navToRemindersSummary: () -> Unit
) {
    Box(Modifier.padding(EchoListTheme.dimensions.m)) {
        BottomNavigation(
            createItemCallbacks = createItemCallbacks,
            onOpenRemindersSummary = navToRemindersSummary,
            navigateToTasksSummary = navToTasksSummary
        )
    }
}
