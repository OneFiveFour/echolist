package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.CreateItemCallbacks

@Composable
internal fun PhoneBrowserNavigation(
    createItemCallbacks: CreateItemCallbacks,
    navToTasksSummary: () -> Unit,
    navToRemindersSummary: () -> Unit
) {
    Box(
        Modifier
            .padding(horizontal = EchoListTheme.dimensions.m)
    ) {
        BottomNavigation(
            createItemCallbacks = createItemCallbacks,
            onOpenRemindersSummary = navToRemindersSummary,
            navigateToTasksSummary = navToTasksSummary
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun PhoneBrowserNavigationPreview() {
    EchoListTheme(modifier = Modifier) {
        PhoneBrowserNavigation(
            createItemCallbacks = CreateItemCallbacks(),
            navToTasksSummary = {},
            navToRemindersSummary = {}
        )
    }
}