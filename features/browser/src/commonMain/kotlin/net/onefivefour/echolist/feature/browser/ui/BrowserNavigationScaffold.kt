package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveComponentOverrideApi
import androidx.compose.material3.adaptive.navigationsuite.LocalNavigationSuiteScaffoldOverride
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldOverride
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldOverrideScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewPhoneLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.phone.PhoneBrowserNavigation

/** Keeps EchoList's custom navigation visuals within the official adaptive scaffold layout. */
@OptIn(ExperimentalMaterial3AdaptiveComponentOverrideApi::class)
@Composable
internal fun BrowserNavigationScaffold(
    isDesktop: Boolean,
    navigation: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scaffoldOverride = object : NavigationSuiteScaffoldOverride {
        @Composable
        override fun NavigationSuiteScaffoldOverrideScope.NavigationSuiteScaffold() {
            Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
                NavigationSuiteScaffoldLayout(
                    navigationSuite = navigation,
                    navigationSuiteType = layoutType,
                    state = state,
                    content = content
                )
            }
        }
    }
    CompositionLocalProvider(LocalNavigationSuiteScaffoldOverride provides scaffoldOverride) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {},
            modifier = modifier.fillMaxSize(),
            // Preserve Expanded as the boundary, regardless of height or fold posture.
            layoutType = if (isDesktop) NavigationSuiteType.NavigationRail else NavigationSuiteType.NavigationBar,
            containerColor = EchoListTheme.materialColors.background,
            contentColor = EchoListTheme.materialColors.onBackground,
            content = content
        )
    }
}

@PreviewPhoneLightDark
@Composable
private fun BrowserNavigationScaffoldPreview() {
    EchoListTheme {
        BrowserNavigationScaffold(
            isDesktop = false,
            navigation = { PhoneBrowserNavigation(
                createItemCallbacks = CreateItemCallbacks(),
                navToTasksSummary = {},
                navToRemindersSummary = {}) },
            content = { FileOverview(
                fileEntries = browserPreviewState.fileEntries,
                isLoading = false,
                error = null,
                onFolderClick = {},
                onNoteClick = {},
                onTaskClick = {}) },
        )
    }
}
