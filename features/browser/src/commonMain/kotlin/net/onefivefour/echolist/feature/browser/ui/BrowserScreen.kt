package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.desktop.DesktopBrowserHeading
import net.onefivefour.echolist.feature.browser.ui.desktop.DesktopBrowserLayout
import net.onefivefour.echolist.feature.browser.ui.desktop.DesktopBrowserNavigation
import net.onefivefour.echolist.feature.browser.ui.phone.PhoneBrowserHeading
import net.onefivefour.echolist.feature.browser.ui.phone.PhoneBrowserLayout
import net.onefivefour.echolist.feature.browser.ui.phone.PhoneBrowserNavigation

@Composable
internal fun BrowserScreen(
    browserUiState: BrowserUiState,
    createFolderUiState: CreateFolderUiState = CreateFolderUiState(),
    createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks(),
    renameFolderUiState: RenameFolderUiState = RenameFolderUiState(),
    canNavigateBack: Boolean = false,
    navToTasksSummary: () -> Unit = {},
    navToRemindersSummary: () -> Unit = {},
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onConfirmCreateFolder: () -> Unit = {},
    onDismissCreateFolder: () -> Unit = {},
    onConfirmRenameFolder: () -> Unit = {},
    onDismissRenameFolder: () -> Unit = {},
    onRenameCurrentFolderClick: () -> Unit = {},
    onRenameFolderNameChange: (String) -> Unit = {},
    onFolderNameChange: (String) -> Unit = {},
    onDeleteCurrentFolderClick: () -> Unit = {},
    onFolderClick: (path: String) -> Unit = {},
    onNoteClick: (noteId: String) -> Unit = {},
    onTaskClick: (taskListId: String) -> Unit = {},
) {
    val isDesktop = EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val gridState = rememberLazyGridState()
    val toolbar: @Composable () -> Unit = {
        BrowserToolbar(
            browserUiState = browserUiState,
            heading = {
                if (isDesktop) DesktopBrowserHeading(browserUiState.breadcrumbs)
                else PhoneBrowserHeading(browserUiState.breadcrumbs)
            },
            onRefresh = onRefresh,
            onRenameCurrentFolderClick = onRenameCurrentFolderClick,
            onDeleteCurrentFolderClick = onDeleteCurrentFolderClick
        )
    }
    val files: @Composable () -> Unit = {
        PullToRefreshBox(
            isRefreshing = browserUiState.isRefreshing,
            onRefresh = onRefresh
        ) {
            FileOverview(
                fileEntries = browserUiState.fileEntries,
                isLoading = browserUiState.isLoading,
                error = browserUiState.error,
                onFolderClick = onFolderClick,
                onNoteClick = onNoteClick,
                onTaskClick = onTaskClick,
                isDesktop = isDesktop,
                gridState = gridState
            )
        }
    }

    BrowserNavigationScaffold(
        isDesktop = isDesktop,
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
        if (isDesktop) {
            DesktopBrowserLayout(
                createItemCallbacks = createItemCallbacks,
                canNavigateBack = canNavigateBack,
                onBack = onBack,
                toolbar = toolbar,
                content = files
            )
        } else {
            PhoneBrowserLayout(
                canNavigateBack = canNavigateBack,
                onBack = onBack,
                toolbar = toolbar,
                content = files
            )
        }
    }

    CreateFolderDialog(
        uiState = createFolderUiState,
        onNameChange = onFolderNameChange,
        onConfirm = onConfirmCreateFolder,
        onDismiss = onDismissCreateFolder
    )

    RenameFolderDialog(
        uiState = renameFolderUiState,
        onNameChange = onRenameFolderNameChange,
        onConfirm = onConfirmRenameFolder,
        onDismiss = onDismissRenameFolder
    )
}

