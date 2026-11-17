package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
    onRefresh: () -> Unit = {},
    onOpenRemindersSummary: () -> Unit = {},
    onFolderClick: (path: String) -> Unit = {},
    onNoteClick: (noteId: String) -> Unit = {},
    onTaskClick: (taskListId: String) -> Unit = {},
    onDeleteCurrentFolderClick: () -> Unit = {},
    onRenameCurrentFolderClick: () -> Unit = {},
    navigateToTasksSummary: () -> Unit = {},
    canNavigateBack: Boolean = false,
    onBackClick: () -> Unit = {},
    createItemCallbacks: CreateItemCallbacks = CreateItemCallbacks(),
    createFolderUiState: CreateFolderUiState = CreateFolderUiState(),
    onFolderNameChange: (String) -> Unit = {},
    onConfirmCreateFolder: () -> Unit = {},
    onDismissCreateFolder: () -> Unit = {},
    renameFolderUiState: RenameFolderUiState = RenameFolderUiState(),
    onRenameFolderNameChange: (String) -> Unit = {},
    onConfirmRenameFolder: () -> Unit = {},
    onDismissRenameFolder: () -> Unit = {}
) {
    val isWide = EchoListTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val colors = EchoListTheme.materialColors

    Row(
        Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        if (isWide) {
            Surface(
                color = colors.surface,
                modifier = Modifier
                    .width(264.dp)
                    .fillMaxHeight()
                    .testTag("browser-sidebar")
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(40.dp))
                    BrowserNavButton(
                        label = stringResource(resource = Res.string.nav_browse),
                        icon = Res.drawable.browser_browse,
                        isSelected = true,
                        isWide = true,
                        onClick = {})
                    Spacer(Modifier.height(EchoListTheme.dimensions.xs))
                    BrowserNavButton(
                        label = stringResource(resource = Res.string.nav_due),
                        icon = Res.drawable.browser_due,
                        isSelected = false,
                        isWide = true,
                        onClick = navigateToTasksSummary,
                        hasDueBadge = true
                    )
                    Spacer(Modifier.height(EchoListTheme.dimensions.xs))
                    BrowserNavButton(
                        label = stringResource(resource = Res.string.nav_reminders),
                        icon = Res.drawable.browser_remind,
                        isSelected = false,
                        isWide = true,
                        onClick = onOpenRemindersSummary
                    )
                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = stringResource(resource = Res.string.location),
                        style = EchoListTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                        color = colors.outline
                    )
                    Spacer(Modifier.height(EchoListTheme.dimensions.m))
                    browserUiState.breadcrumbs.forEach { crumb ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(), shape = EchoListTheme.shapes.small,
                            color = if (crumb == browserUiState.breadcrumbs.lastOrNull()) colors.surfaceVariant else colors.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .heightIn(min = 34.dp)
                                    .padding(EchoListTheme.dimensions.s),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(Res.drawable.browser_browse),
                                    null,
                                    Modifier.size(14.dp),
                                    tint = colors.onSurface
                                )
                                Spacer(Modifier.width(EchoListTheme.dimensions.s))
                                Text(
                                    crumb.label, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = EchoListTheme.typography.labelSmall, color = colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Box(Modifier
                .width(EchoListTheme.dimensions.borderWidth)
                .fillMaxHeight()
                .background(colors.outlineVariant)
            )
        }

        Box(Modifier.weight(1f).fillMaxHeight()) {
            Column(
                Modifier.fillMaxSize().padding(
                    horizontal = if (isWide) 80.dp else 20.dp,
                    vertical = if (isWide) 64.dp else EchoListTheme.dimensions.l
                )
            ) {
                if (isWide) {
                    Text(
                        stringResource(Res.string.your_space),
                        style = EchoListTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(EchoListTheme.dimensions.s))
                } else if (browserUiState.breadcrumbs.size > 1) {
                    BreadcrumbBar(browserUiState.breadcrumbs)
                    Spacer(Modifier.height(EchoListTheme.dimensions.s))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canNavigateBack) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(Res.string.navigate_back),
                                tint = colors.onSurface
                            )
                        }
                    } else {
                        Icon(
                            painterResource(Res.drawable.browser_home),
                            null,
                            Modifier.size(25.dp),
                            tint = colors.onSurface
                        )
                        Spacer(Modifier.width(EchoListTheme.dimensions.s))
                    }
                    Text(
                        browserUiState.breadcrumbs.lastOrNull()?.label.orEmpty(),
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = EchoListTheme.typography.titleLarge.copy(
                            fontSize = if (isWide) 40.sp else 26.sp,
                            lineHeight = if (isWide) 48.sp else 31.sp
                        ), color = colors.onSurface
                    )
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh,
                            stringResource(Res.string.refresh_browser),
                            tint = colors.onSurfaceVariant
                        )
                    }
                    if (browserUiState.canRenameCurrentFolder) {
                        IconButton(onClick = onRenameCurrentFolderClick) {
                            Icon(
                                painterResource(SharedRes.drawable.ic_edit),
                                stringResource(Res.string.rename_folder),
                                tint = colors.primary
                            )
                        }
                    }
                    if (browserUiState.canDeleteCurrentFolder) {
                        IconButton(onClick = onDeleteCurrentFolderClick, enabled = !browserUiState.isDeletingFolder) {
                            Icon(
                                painterResource(SharedRes.drawable.ic_delete),
                                stringResource(Res.string.delete_folder),
                                tint = colors.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(if (isWide) 56.dp else EchoListTheme.dimensions.xl))
                Box(Modifier.weight(1f)) {
                    PullToRefreshBox(isRefreshing = browserUiState.isRefreshing, onRefresh = onRefresh) {
                        FileOverview(
                            browserUiState.fileEntries, browserUiState.isLoading, browserUiState.error,
                            onFolderClick, onNoteClick, onTaskClick, isWide = isWide
                        )
                    }
                }
                if (!isWide) Spacer(Modifier.height(80.dp))
            }
            if (isWide) {
                Box(Modifier.align(Alignment.BottomEnd).padding(end = 68.dp, bottom = EchoListTheme.dimensions.xl)) {
                    CreateItemButton(createItemCallbacks, true)
                }
            } else {
                Box(
                    Modifier.align(Alignment.BottomCenter)
                        .padding(horizontal = EchoListTheme.dimensions.m, vertical = EchoListTheme.dimensions.m)
                ) {
                    BottomNavigation(
                        createItemCallbacks = createItemCallbacks,
                        onOpenRemindersSummary = onOpenRemindersSummary,
                        navigateToTasksSummary = navigateToTasksSummary
                    )
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
