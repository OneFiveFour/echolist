package net.onefivefour.echolist.feature.browser.domain.model

internal data class FileEntry(
    val path: String,
    val title: String,
    val itemType: ItemType,
    val metadata: FileMetadata?
) {
    val lastModified: Long
        get() = when (val value = metadata) {
            is FileMetadata.Note -> value.updatedAt
            is FileMetadata.TaskList -> value.updatedAt
            else -> 0L
        }
}

internal enum class ItemType {
    UNSPECIFIED,
    FOLDER,
    NOTE,
    TASK_LIST
}

internal sealed interface FileMetadata {
    data class Folder(val childCount: Int) : FileMetadata
    data class Note(
        val id: String,
        val updatedAt: Long,
        val preview: String
    ) : FileMetadata
    data class TaskList(
        val id: String,
        val updatedAt: Long,
        val totalTaskCount: Int,
        val doneTaskCount: Int,
        val overdueTaskCount: Int? = null,
        val dueSoonTaskCount: Int? = null
    ) : FileMetadata
}