package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.core.designsystem.resources.ic_delete
import net.onefivefour.echolist.core.designsystem.resources.ic_edit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes

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
    val colors = EchoListTheme.materialColors

    BrowserNavigationScaffold(
        isDesktop = isDesktop,
        navigation = {
            if (isDesktop) {
                Row {
                    DesktopNavRail(
                        colors = colors,
                        navToTasksSummary = navToTasksSummary,
                        navToRemindersSummary = navToRemindersSummary,
                        browserUiState = browserUiState
                    )
                    Box(
                        Modifier.width(EchoListTheme.dimensions.borderWidth)
                            .fillMaxHeight().background(colors.outlineVariant)
                    )
                }
            } else {
                Box(Modifier.padding(EchoListTheme.dimensions.m)) {
                    BottomNavigation(
                        createItemCallbacks = createItemCallbacks,
                        onOpenRemindersSummary = navToRemindersSummary,
                        navigateToTasksSummary = navToTasksSummary
                    )
                }
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(
                    horizontal = if (isDesktop) 80.dp else 20.dp,
                    vertical = if (isDesktop) 64.dp else EchoListTheme.dimensions.l
                )
            ) {
                if (isDesktop) {
                    Text(
                        stringResource(Res.string.your_space),
                        style = EchoListTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                        color = colors.onSurfaceVariant
                    )
                } else if (browserUiState.breadcrumbs.size > 1) {
                    BreadcrumbBar(browserUiState.breadcrumbs)
                }

                Spacer(Modifier.height(EchoListTheme.dimensions.s))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (canNavigateBack) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.navigate_back),
                                tint = colors.onSurface
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(resource = Res.drawable.browser_home),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            tint = colors.onSurface
                        )
                        Spacer(Modifier.width(EchoListTheme.dimensions.s))
                    }

                    val fontSize = when {
                        isDesktop -> 40.sp
                        else -> 26.sp
                    }

                    val lineHeight = when {
                        isDesktop -> 48.sp
                        else -> 31.sp
                    }

                    Text(
                        text = browserUiState.breadcrumbs.lastOrNull()?.label.orEmpty(),
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = EchoListTheme.typography.titleLarge.copy(
                            fontSize = fontSize,
                            lineHeight = lineHeight
                        ),
                        color = colors.onSurface
                    )

                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.refresh_browser),
                            tint = colors.onSurfaceVariant
                        )
                    }

                    if (browserUiState.canRenameCurrentFolder) {
                        IconButton(onClick = onRenameCurrentFolderClick) {
                            Icon(
                                painter = painterResource(resource = SharedRes.drawable.ic_edit),
                                contentDescription = stringResource(Res.string.rename_folder),
                                tint = colors.primary
                            )
                        }
                    }

                    if (browserUiState.canDeleteCurrentFolder) {
                        IconButton(onClick = onDeleteCurrentFolderClick, enabled = !browserUiState.isDeletingFolder) {
                            Icon(
                                painter = painterResource(resource = SharedRes.drawable.ic_delete),
                                contentDescription = stringResource(Res.string.delete_folder),
                                tint = colors.primary
                            )
                        }
                    }
                }

                val spaceHeight = when {
                    isDesktop -> 56.dp
                    else -> EchoListTheme.dimensions.xl
                }
                Spacer(Modifier.height(spaceHeight))

                Box(Modifier.weight(1f)) {
                    PullToRefreshBox(isRefreshing = browserUiState.isRefreshing, onRefresh = onRefresh) {
                        FileOverview(
                            browserUiState.fileEntries, browserUiState.isLoading, browserUiState.error,
                            onFolderClick, onNoteClick, onTaskClick, isDesktop = isDesktop
                        )
                    }
                }

            }

            if (isDesktop) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 68.dp, bottom = EchoListTheme.dimensions.xl)
                ) {
                    CreateItemButton(createItemCallbacks, true)
                }
            }
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

