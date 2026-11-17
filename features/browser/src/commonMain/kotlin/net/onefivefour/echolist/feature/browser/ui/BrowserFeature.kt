package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BrowserFeature(
    selectedFolder: String,
    canNavigateBack: Boolean = false,
    onOpenFolder: (String) -> Unit,
    onFolderRenamed: (String) -> Unit,
    onOpenNote: (String?) -> Unit,
    onOpenTaskList: (String?) -> Unit,
    onOpenRemindersSummary: () -> Unit,
    navigateToTasksSummary: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val browserViewModel = koinViewModel<BrowserViewModel>(key = selectedFolder) {
        parametersOf(selectedFolder)
    }

    val browserUiState by browserViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        browserViewModel.clearErrorAndReload()
    }

    LaunchedEffect(browserViewModel) {
        browserViewModel.navigateBack.collect {
            onBackClick()
        }
    }

    val createFolderViewModel = koinViewModel<CreateFolderViewModel>(key = "createFolder-$selectedFolder") {
        parametersOf(selectedFolder)
    }

    val createFolderUiState by createFolderViewModel.uiState.collectAsStateWithLifecycle()

    val renameFolderViewModel = koinViewModel<RenameFolderViewModel>(key = "renameFolder-$selectedFolder") {
        parametersOf(selectedFolder)
    }

    val renameFolderUiState by renameFolderViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(renameFolderViewModel) {
        renameFolderViewModel.navigateToFolder.collect { newPath ->
            onFolderRenamed(newPath)
        }
    }

    BrowserScreen(
        browserUiState = browserUiState,
        createFolderUiState = createFolderUiState,
        renameFolderUiState = renameFolderUiState,

        canNavigateBack = canNavigateBack,
        onBackClick = onBackClick,

        createItemCallbacks = CreateItemCallbacks(
            onCreateFolder = createFolderViewModel::showDialog,
            onCreateNote = { onOpenNote(null) },
            onCreateTaskList = { onOpenTaskList(null) }
        ),

        onConfirmCreateFolder = createFolderViewModel::onConfirm,
        onDismissCreateFolder = createFolderViewModel::dismissDialog,

        onConfirmRenameFolder = renameFolderViewModel::onConfirm,
        onDismissRenameFolder = renameFolderViewModel::dismissDialog,
        onRenameCurrentFolderClick = renameFolderViewModel::showDialog,
        onRenameFolderNameChange = renameFolderViewModel::onNameChange,
        onFolderNameChange = createFolderViewModel::onNameChange,

        onDeleteCurrentFolderClick = browserViewModel::onDeleteCurrentFolderClick,

        onFolderClick = onOpenFolder,
        onNoteClick = { onOpenNote(it) },
        onTaskClick = { onOpenTaskList(it) },

        onOpenRemindersSummary = onOpenRemindersSummary,
        navigateToTasksSummary = navigateToTasksSummary,

        onRefresh = browserViewModel::refresh,
    )
}