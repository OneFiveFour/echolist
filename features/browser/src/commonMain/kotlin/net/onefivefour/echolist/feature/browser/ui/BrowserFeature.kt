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
    navToFolder: (String) -> Unit,
    navToNote: (String?) -> Unit,
    navToTaskList: (String?) -> Unit,
    navToRemindersSummary: () -> Unit,
    navToTasksSummary: () -> Unit,
    onLogout: (() -> Unit)? = null,
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

    val isSaving = browserUiState.folderDraft?.isSaving == true

    SideEffect {
        onNavigationStateChange(
            isSaving,
            browserViewModel::dismissFolderDraft
        )
    }

    DisposableEffect(browserViewModel) {
        onDispose {
            onNavigationStateChange(false) { }
        }
    }

    fun leaveBrowserWith(action: () -> Unit) {
        if (browserViewModel.uiState.value.folderDraft?.isSaving == true) return
        browserViewModel.dismissFolderDraft()
        action()
    }

    BrowserScreen(
        browserUiState = browserUiState,

        canNavigateBack = canNavigateBack,
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
        onNoteClick = { id -> leaveBrowserWith { navToNote(id) } },
        onTaskClick = { id -> leaveBrowserWith { navToTaskList(id) } },

        navToRemindersSummary = { leaveBrowserWith(navToRemindersSummary) },
        navToTasksSummary = { leaveBrowserWith(navToTasksSummary) },

        onRefresh = browserViewModel::refresh,

        onLogoutClick = onLogout?.let { logout -> { leaveBrowserWith(logout) } }
    )
}