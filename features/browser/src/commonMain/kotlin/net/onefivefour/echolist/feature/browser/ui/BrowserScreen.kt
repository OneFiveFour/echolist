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
    onConfirmFolderDraft: () -> Unit = {},
    onDismissFolderDraft: () -> Unit = {},
    onFolderNameChange: (String) -> Unit = {},
    onDeleteCurrentFolder: () -> Unit = {},
    onFolderClick: (path: String) -> Unit = {},
    onFolderLongClick: (path: String) -> Unit = {},
    onNoteClick: (noteId: String) -> Unit = {},
    onTaskListClick: (taskListId: String) -> Unit = {},
    onLogout: (() -> Unit)? = null
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
            onDeleteCurrentFolderClick = onDeleteCurrentFolder,
            onLogoutClick = onLogout
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
                errorMessage = browserUiState.error?.let { browserErrorMessage(it) },
                onFolderClick = onFolderClick,
                onFolderLongClick = onFolderLongClick,
                onNoteClick = onNoteClick,
                onTaskClick = onTaskListClick,
                isDesktop = isDesktop,
                gridState = gridState,
                contentPadding = contentPadding,
                folderDraft = browserUiState.folderDraft,
                onFolderNameChange = onFolderNameChange,
                onConfirmFolder = onConfirmFolderDraft,
                onCancelFolder = onDismissFolderDraft
            )
        }
    }

    BrowserNavigationScaffold(
        isDesktop = isDesktop,
        modifier = when {
            browserUiState.folderDraft != null -> Modifier.imePadding()
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
            canNavigateBack = true,
            onLogout = { }
        )
    }
}

@PreviewPhoneLightDark
@Composable
private fun BrowserScreenPhoneHomePreview() {
    EchoListTheme {
        BrowserScreen(
            browserUiState = browserPreviewState.copy(
                breadcrumbs = listOf(
                    BreadcrumbItem("Home", "")
                )
            ),
            canNavigateBack = true,
            onLogout = { }
        )
    }
}

@PreviewDesktopLightDark
@Composable
private fun BrowserScreenDesktopPreview() {
    EchoListTheme {
        BrowserScreen(
            browserUiState = browserPreviewState,
            canNavigateBack = true,
            onLogout = { }
        )
    }
}