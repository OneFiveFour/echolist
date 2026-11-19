package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
    onNavigationStateChange: (isBlocked: Boolean, discardDraft: () -> Unit) -> Unit = { _, _ -> },
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

    val isSavingFolder = browserUiState.folderDraft?.isSaving == true
    SideEffect { onNavigationStateChange(isSavingFolder, browserViewModel::dismissFolderDraft) }
    DisposableEffect(browserViewModel) {
        onDispose {
            onNavigationStateChange(false, {})
        }
    }
    fun leaveBrowser(action: () -> Unit) {
        if (browserViewModel.uiState.value.folderDraft?.isSaving == true) return
        browserViewModel.dismissFolderDraft()
        action()
    }

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
        renameFolderUiState = renameFolderUiState,

        canNavigateBack = canNavigateBack,
        onBack = { leaveBrowser(onBack) },

        createItemCallbacks = CreateItemCallbacks(
            onCreateFolder = browserViewModel::startFolderDraft,
            onCreateNote = { leaveBrowser { navToNote(null) } },
            onCreateTaskList = { leaveBrowser { navToTaskList(null) } }
        ),

        onConfirmCreateFolder = browserViewModel::confirmFolderDraft,
        onDismissCreateFolder = browserViewModel::dismissFolderDraft,

        onConfirmRenameFolder = renameFolderViewModel::onConfirm,
        onDismissRenameFolder = renameFolderViewModel::onDismissDialog,
        onRenameCurrentFolderClick = {
            if (!isSavingFolder) {
                browserViewModel.dismissFolderDraft()
                renameFolderViewModel.showDialog()
            }
        },
        onRenameFolderNameChange = renameFolderViewModel::onNameChange,
        onFolderNameChange = browserViewModel::onFolderNameChange,

        onDeleteCurrentFolderClick = browserViewModel::onDeleteCurrentFolderClick,

        onFolderClick = { path -> leaveBrowser { navToFolder(path) } },
        onNoteClick = { id -> leaveBrowser { navToNote(id) } },
        onTaskClick = { id -> leaveBrowser { navToTaskList(id) } },

        navToRemindersSummary = { leaveBrowser(navToRemindersSummary) },
        navToTasksSummary = { leaveBrowser(navToTasksSummary) },

        onRefresh = browserViewModel::refresh,
    )
}
