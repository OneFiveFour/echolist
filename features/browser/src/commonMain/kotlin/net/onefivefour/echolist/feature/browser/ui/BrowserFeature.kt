package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BrowserFeature(
    selectedFolder: String,
    navToFolder: (String) -> Unit,
    navToNote: (String?) -> Unit,
    navToTaskList: (String?) -> Unit,
    navToRemindersSummary: () -> Unit,
    navToTasksSummary: () -> Unit,
    onLogout: (() -> Unit)? = null,
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

    val isSaving = browserUiState.folderDraft?.isSaving == true

    // Swallow system back while a folder is being saved so the entry is not popped mid-request.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = isSaving,
        onBackCompleted = {}
    )

    // A covered or removed browser must not keep an open folder draft.
    DisposableEffect(browserViewModel) {
        onDispose { browserViewModel.dismissFolderDraft() }
    }

    fun leaveBrowserWith(action: () -> Unit) {
        if (browserViewModel.uiState.value.folderDraft?.isSaving == true) return
        browserViewModel.dismissFolderDraft()
        action()
    }

    BrowserScreen(
        browserUiState = browserUiState,

        canNavigateBack = selectedFolder.isNotEmpty(),
        onBack = { leaveBrowserWith(onBack) },

        createItemCallbacks = CreateItemCallbacks(
            onCreateFolder = browserViewModel::startFolderDraft,
            onCreateNote = { leaveBrowserWith { navToNote(null) } },
            onCreateTaskList = { leaveBrowserWith { navToTaskList(null) } }
        ),

        onConfirmFolder = browserViewModel::confirmFolderDraft,
        onDismissFolder = browserViewModel::dismissFolderDraft,

        onFolderNameChange = browserViewModel::onFolderNameChange,
        onDeleteCurrentFolderClick = browserViewModel::onDeleteCurrentFolderClick,
        onFolderLongClick = browserViewModel::startFolderRename,
        onFolderClick = { path -> leaveBrowserWith { navToFolder(path) } },
        onNoteClick = { noteId -> leaveBrowserWith { navToNote(noteId) } },
        onTaskListClick = { taskListId -> leaveBrowserWith { navToTaskList(taskListId) } },

        navToRemindersSummary = { leaveBrowserWith(navToRemindersSummary) },
        navToTasksSummary = { leaveBrowserWith(navToTasksSummary) },

        onRefresh = browserViewModel::refresh,

        onLogoutClick = onLogout?.let { logout -> { leaveBrowserWith(logout) } }
    )
}