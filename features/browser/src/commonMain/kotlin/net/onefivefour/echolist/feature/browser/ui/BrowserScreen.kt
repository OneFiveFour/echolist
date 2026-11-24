package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewDesktopLightDark
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewPhoneLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.desktop.DesktopBrowserHeading
import net.onefivefour.echolist.feature.browser.ui.desktop.DesktopBrowserNavigation
import net.onefivefour.echolist.feature.browser.ui.phone.PhoneBrowserHeading
import net.onefivefour.echolist.feature.browser.ui.phone.PhoneBrowserNavigation

@Composable
internal fun BrowserScreen(
    browserUiState: BrowserUiState,
    createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks(),
    canNavigateBack: Boolean = false,
    navToTasksSummary: () -> Unit = {},
    navToRemindersSummary: () -> Unit = {},
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onConfirmFolder: () -> Unit = {},
    onDismissFolder: () -> Unit = {},
    onFolderNameChange: (String) -> Unit = {},
    onDeleteCurrentFolderClick: () -> Unit = {},
    onFolderClick: (path: String) -> Unit = {},
    onFolderLongClick: (path: String) -> Unit = {},
    onNoteClick: (noteId: String) -> Unit = {},
    onTaskClick: (taskListId: String) -> Unit = {},
    onLogoutClick: (() -> Unit)? = null,
) {
    val isDesktop = EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val gridState = rememberLazyGridState()

    val toolbar: @Composable () -> Unit = {
        BrowserToolbar(
            browserUiState = browserUiState,
            heading = {
                when {
                    isDesktop -> DesktopBrowserHeading(browserUiState.breadcrumbs)
                    else -> PhoneBrowserHeading(browserUiState.breadcrumbs, onFolderClick)
                }
            },
            onDeleteCurrentFolderClick = onDeleteCurrentFolderClick,
            onLogoutClick = onLogoutClick
        )
    }

    val files: @Composable (PaddingValues) -> Unit = { contentPadding ->
        PullToRefreshBox(
            isRefreshing = browserUiState.isRefreshing,
            onRefresh = onRefresh
        ) {
            FileOverview(
                fileEntries = browserUiState.fileEntries,
                isLoading = browserUiState.isLoading,
                errorMessage = browserUiState.error,
                onFolderClick = onFolderClick,
                onFolderLongClick = onFolderLongClick,
                onNoteClick = onNoteClick,
                onTaskClick = onTaskClick,
                isDesktop = isDesktop,
                gridState = gridState,
                contentPadding = contentPadding,
                createItemCallbacks = createItemCallbacks,
                folderDraft = browserUiState.folderDraft,
                onFolderNameChange = onFolderNameChange,
                onConfirmFolder = onConfirmFolder,
                onCancelFolder = onDismissFolder
            )
        }
    }

    val isInFolderDraft = browserUiState.folderDraft != null
    BrowserNavigationScaffold(
        isDesktop = isDesktop,
        modifier = when {
            isInFolderDraft -> Modifier.imePadding()
            else -> Modifier
        },
        navigation = {
            if (isDesktop) {
                DesktopBrowserNavigation(
                    browserUiState = browserUiState,
                    onFolderClick = onFolderClick,
                    navToTasksSummary = navToTasksSummary,
                    navToRemindersSummary = navToRemindersSummary
                )
            } else {
                PhoneBrowserNavigation(
                    createItemCallbacks = createItemCallbacks,
                    navToTasksSummary = navToTasksSummary,
                    navToRemindersSummary = navToRemindersSummary
                )
            }
        }
    ) {
        BrowserLayout(
            isDesktop = isDesktop,
            createItemCallbacks = createItemCallbacks,
            canNavigateBack = canNavigateBack,
            onBack = onBack,
            toolbar = toolbar,
            content = files
        )
    }
}

@PreviewPhoneLightDark
@Composable
private fun BrowserScreenPhonePreview() {
    EchoListTheme {
        BrowserScreen(
            browserUiState = browserPreviewState,
            canNavigateBack = true
        )
    }
}

@PreviewDesktopLightDark
@Composable
private fun BrowserScreenDesktopPreview() {
    EchoListTheme {
        BrowserScreen(
            browserUiState = browserPreviewState,
            canNavigateBack = true
        )
    }
}
