package net.onefivefour.echolist.feature.note.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.onefivefour.echolist.core.files.domain.normalizePath
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteFeature(parentDir: String, noteId: String?, onBack: () -> Unit) {
    val resolvedNoteId = noteId?.takeIf { it.isNotBlank() }

    val mode = resolvedNoteId?.let(EditNoteMode::Edit)
        ?: EditNoteMode.Create(normalizePath(parentDir))
    val viewModel = koinViewModel<EditNoteViewModel>(
        key = "editNote-$parentDir-${resolvedNoteId.orEmpty()}"
    ) { parametersOf(mode) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navigateBack.collect { onBack() }
    }

    EditNoteScreen(
        uiState = uiState,
        onPreviewToggle = viewModel::onPreviewToggle,
        onBeginEdit = viewModel::onBeginEdit,
        onToolbarAction = viewModel::onToolbarAction,
        onSaveClick = viewModel::onSaveClick,
        onDeleteClick = viewModel::onDeleteClick
    )
}