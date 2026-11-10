package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BrowserFeature(
    parentDir: String,
    onBreadcrumbClick: (String) -> Unit,
    onReplaceFolder: (String) -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenNote: (String?) -> Unit,
    onOpenTaskList: (String?) -> Unit,
    onOpenTaskNotifications: () -> Unit
) {
    val browserViewModel =
        koinViewModel<BrowserViewModel>(key = parentDir) { parametersOf(parentDir) }

    val browserUiState by browserViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        browserViewModel.clearErrorAndReload()
    }

    LaunchedEffect(browserViewModel) {
        browserViewModel.navigateToFolder.collect { parentDir ->
            onReplaceFolder(parentDir)
        }
    }

    val createFolderViewModel =
        koinViewModel<CreateFolderViewModel>(
            key = "createFolder-$parentDir"
        ) { parametersOf(parentDir) }

    val createFolderUiState by createFolderViewModel.uiState.collectAsStateWithLifecycle()

    val renameFolderViewModel =
        koinViewModel<RenameFolderViewModel>(
            key = "renameFolder-$parentDir"
        ) { parametersOf(parentDir) }

    val renameFolderUiState by renameFolderViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(renameFolderViewModel) {
        renameFolderViewModel.navigateToFolder.collect { newPath ->
            onReplaceFolder(newPath)
        }
    }

    BrowserScreen(
        uiState = browserUiState,
        createFolderUiState = createFolderUiState,
        onBreadcrumbClick = onBreadcrumbClick,
        onRefresh = browserViewModel::refresh,
        createItemCallbacks = CreateItemCallbacks(
            onCreateFolder = createFolderViewModel::showDialog,
            onCreateNote = { onOpenNote(null) },
            onCreateTaskList = { onOpenTaskList(null) }
        ),
        onFolderClick = onOpenFolder,
        onNoteClick = { onOpenNote(it) },
        onTaskClick = { onOpenTaskList(it) },
        onOpenTaskNotifications = onOpenTaskNotifications,
        onDeleteCurrentFolderClick = browserViewModel::onDeleteCurrentFolderClick,
        onRenameCurrentFolderClick = renameFolderViewModel::showDialog,
        onFolderNameChange = createFolderViewModel::onNameChange,
        onConfirmCreateFolder = createFolderViewModel::onConfirm,
        onDismissCreateFolder = createFolderViewModel::dismissDialog,
        renameFolderUiState = renameFolderUiState,
        onRenameFolderNameChange = renameFolderViewModel::onNameChange,
        onConfirmRenameFolder = renameFolderViewModel::onConfirm,
        onDismissRenameFolder = renameFolderViewModel::dismissDialog
    )
}