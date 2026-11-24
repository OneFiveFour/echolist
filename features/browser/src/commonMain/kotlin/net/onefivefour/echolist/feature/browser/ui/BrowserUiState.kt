package net.onefivefour.echolist.feature.browser.ui

import net.onefivefour.echolist.feature.browser.domain.model.FileEntry

internal data class BrowserUiState(
    val breadcrumbs: List<BreadcrumbItem>,
    val fileEntries: List<FileEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isDeletingFolder: Boolean = false,
    val canDeleteCurrentFolder: Boolean = false,
    val folderDraft: FolderDraft? = null,
    val error: String? = null
)

internal data class BreadcrumbItem(
    val label: String,
    val parentDir: String
)

internal data class FolderDraft(
    val id: Long,
    val name: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val focusRequest: Long = 0,
    val originalEntry: FileEntry? = null,
    val originalIndex: Int = 0
) {
    val isRenaming: Boolean get() = originalEntry != null
    val canConfirm: Boolean get() = name.trim().isNotEmpty() && !isSaving
}