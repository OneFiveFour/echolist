package net.onefivefour.echolist.feature.browser.ui

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
                else PhoneBrowserHeading(browserUiState.breadcrumbs, onFolderClick)
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
                gridState = gridState,
                createItemCallbacks = createItemCallbacks,
                folderDraft = browserUiState.folderDraft,
                onFolderNameChange = onFolderNameChange,
                onConfirmFolder = onConfirmCreateFolder,
                onCancelFolder = onDismissCreateFolder
            )
        }
    }

    BrowserNavigationScaffold(
        isDesktop = isDesktop,
        modifier = if (browserUiState.folderDraft != null) Modifier.imePadding() else Modifier,
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

    RenameFolderDialog(
        uiState = renameFolderUiState,
        onNameChange = onRenameFolderNameChange,
        onConfirm = onConfirmRenameFolder,
        onDismiss = onDismissRenameFolder
    )
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
