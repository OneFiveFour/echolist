package net.onefivefour.echolist.feature.browser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import net.onefivefour.echolist.core.files.domain.normalizePath
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository

internal class RenameFolderViewModel(
    private val parentDir: String,
    private val fileRepository: FileRepository
) : ViewModel() {

    val uiState: StateFlow<RenameFolderUiState>
        field = MutableStateFlow(RenameFolderUiState())

    val navigateToFolder: SharedFlow<String>
        field = MutableSharedFlow<String>()

    fun showDialog() {
        val currentName = parentDir.trimEnd('/').substringAfterLast('/')
        uiState.update {
            RenameFolderUiState(isVisible = true, folderName = currentName)
        }
    }

    fun dismissDialog() {
        uiState.update {
            RenameFolderUiState()
        }
    }

    fun onNameChange(value: String) {
        uiState.update { it.copy(folderName = value, error = null) }
    }

    fun onConfirm() {
        val trimmedName = uiState.value.folderName.trim()
        if (trimmedName.isBlank()) return

        uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val normalizedPath = normalizePath(parentDir)
            val result = fileRepository.updateFolder(
                UpdateFolderParams(folderPath = normalizedPath, newName = trimmedName)
            )
            result.fold(
                onSuccess = { folder ->
                    uiState.update { RenameFolderUiState() }
                    navigateToFolder.emit(normalizePath(folder.path))
                },
                onFailure = { exception ->
                    uiState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                }
            )
        }
    }
}