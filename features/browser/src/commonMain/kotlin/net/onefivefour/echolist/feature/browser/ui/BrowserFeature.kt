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
    onFolderRenamed: (String) -> Unit,
    navToFolder: (String) -> Unit,
    navToNote: (String?) -> Unit,
    navToTaskList: (String?) -> Unit,
    navToRemindersSummary: () -> Unit,
    navToTasksSummary: () -> Unit,
    onBack: () -> Unit = {}
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
            onBack()
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
        onBack = onBack,

        createItemCallbacks = CreateItemCallbacks(
            onCreateFolder = createFolderViewModel::showDialog,
            onCreateNote = { navToNote(null) },
            onCreateTaskList = { navToTaskList(null) }
        ),

        onConfirmCreateFolder = createFolderViewModel::onConfirm,
        onDismissCreateFolder = createFolderViewModel::onDismissDialog,

        onConfirmRenameFolder = renameFolderViewModel::onConfirm,
        onDismissRenameFolder = renameFolderViewModel::onDismissDialog,
        onRenameCurrentFolderClick = renameFolderViewModel::showDialog,
        onRenameFolderNameChange = renameFolderViewModel::onNameChange,
        onFolderNameChange = createFolderViewModel::onNameChange,

        onDeleteCurrentFolderClick = browserViewModel::onDeleteCurrentFolderClick,

        onFolderClick = navToFolder,
        onNoteClick = { navToNote(it) },
        onTaskClick = { navToTaskList(it) },

        navToRemindersSummary = navToRemindersSummary,
        navToTasksSummary = navToTasksSummary,

        onRefresh = browserViewModel::refresh,
    )
}