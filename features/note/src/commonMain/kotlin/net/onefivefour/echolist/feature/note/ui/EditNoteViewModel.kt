package net.onefivefour.echolist.feature.note.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.feature.note.domain.model.CreateNoteParams
import net.onefivefour.echolist.feature.note.domain.model.UpdateNoteParams
import net.onefivefour.echolist.feature.note.domain.repository.NotesRepository

internal class EditNoteViewModel(
    private val mode: EditNoteMode,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val titleState = TextFieldState()

    val uiState: StateFlow<EditNoteUiState>
        field = MutableStateFlow(
            EditNoteUiState(
                titleState = titleState,
                mode = mode,
                isLoading = mode is EditNoteMode.Edit,
                isPreview = mode is EditNoteMode.Edit
            )
        )

    val navigateBack: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

    init {
        if (mode is EditNoteMode.Edit) {
            loadNote(mode.noteId)
        }
    }

    fun onPreviewToggle() {
        uiState.update { it.copy(isPreview = !it.isPreview) }
    }

    fun onBeginEdit() {
        uiState.update { it.copy(isPreview = false) }
    }

    fun onToolbarAction(action: MarkdownToolbarAction) {
        val contentState = uiState.value.contentState
        val text = contentState.text.toString()
        val formatted = MarkdownFormatter.apply(
            action = action,
            text = text,
            selection = contentState.selection
        )

        contentState.edit {
            replace(0, length, formatted.text)
            selection = TextRange(formatted.selectionStart, formatted.selectionEnd)
        }
    }

    fun onDeleteClick() {
        val editMode = uiState.value.mode as? EditNoteMode.Edit ?: return

        uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            notesRepository.deleteNote(editMode.noteId).fold(
                onSuccess = {
                    uiState.update { it.copy(isSaving = false) }
                    navigateBack.emit(Unit)
                },
                onFailure = { e ->
                    uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun onSaveClick() {
        val currentMode = uiState.value.mode
        val trimmedTitle = titleState.text.toString().trim()
        if (trimmedTitle.isBlank()) return

        val content = uiState.value.contentState.text.toString()

        uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = when (currentMode) {
                is EditNoteMode.Create -> notesRepository.createNote(
                    CreateNoteParams(
                        title = trimmedTitle,
                        content = content,
                        parentDir = currentMode.parentDir
                    )
                )
                is EditNoteMode.Edit -> notesRepository.updateNote(
                    UpdateNoteParams(
                        id = currentMode.noteId,
                        title = trimmedTitle,
                        content = content
                    )
                )
            }
            result.fold(
                onSuccess = {
                    uiState.update { it.copy(isSaving = false) }
                    navigateBack.emit(Unit)
                },
                onFailure = { e ->
                    uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            val result = notesRepository.getNote(noteId)
            result.fold(
                onSuccess = { note ->
                    titleState.edit {
                        replace(0, length, note.title)
                    }
                    uiState.value.contentState.edit {
                        replace(0, length, note.content)
                        selection = TextRange(note.content.length)
                    }
                    uiState.update { it.copy(isLoading = false, error = null) }
                },
                onFailure = { e ->
                    uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}