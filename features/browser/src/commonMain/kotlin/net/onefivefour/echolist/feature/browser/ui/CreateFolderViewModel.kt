package net.onefivefour.echolist.feature.browser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository

internal class CreateFolderViewModel(
    private val parentDir: String,
    private val fileRepository: FileRepository
) : ViewModel() {

    val uiState: StateFlow<CreateFolderUiState>
        field = MutableStateFlow(CreateFolderUiState())

    fun showDialog() {
        uiState.update {
            CreateFolderUiState(isVisible = true)
        }
    }

    fun dismissDialog() {
        uiState.update {
            CreateFolderUiState()
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
            val result = fileRepository.createFolder(
                CreateFolderParams(parentDir = parentDir, name = trimmedName)
            )
            result.fold(
                onSuccess = {
                    uiState.update { CreateFolderUiState() }
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