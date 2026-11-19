package net.onefivefour.echolist.feature.browser.data.mapper

import net.onefivefour.echolist.core.files.domain.normalizePath
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.Folder
import net.onefivefour.echolist.feature.browser.domain.model.ItemType
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import `file`.v1.CreateFolderRequest
import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderRequest
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderRequest
import `file`.v1.UpdateFolderResponse

/**
 * Maps between Wire-generated file.v1 proto models and domain models.
 * Folder mutations return trailing slashes while ListFiles omits them. Normalize both
 * at this boundary so one folder has one identity for navigation and reconciliation.
 */
internal object FileMapper {

    fun toDomain(proto: `file`.v1.Folder): Folder = Folder(
        path = normalizePath(proto.path),
        name = proto.name
    )

    fun toDomain(proto: CreateFolderResponse): Folder =
        toDomain(requireFolder(proto.folder, "CreateFolderResponse"))

    fun toDomain(proto: ListFilesResponse): List<FileEntry> =
        proto.entries.map { toDomain(it) }

    fun toDomain(proto: `file`.v1.FileEntry): FileEntry = FileEntry(
        path = if (proto.item_type == `file`.v1.ItemType.ITEM_TYPE_FOLDER) normalizePath(proto.path) else proto.path,
        title = proto.title,
        itemType = toDomain(proto.item_type),
        metadata = proto.folder_metadata?.let {
            FileMetadata.Folder(childCount = it.child_count)
        } ?: proto.note_metadata?.let {
            FileMetadata.Note(id = it.id, updatedAt = it.updated_at, preview = it.preview)
        } ?: proto.task_list_metadata?.let {
            FileMetadata.TaskList(
                id = it.id,
                updatedAt = it.updated_at,
                totalTaskCount = it.total_task_count,
                doneTaskCount = it.done_task_count
            )
        }
    )

    fun toDomain(proto: `file`.v1.ItemType): ItemType = when (proto) {
        `file`.v1.ItemType.ITEM_TYPE_FOLDER -> ItemType.FOLDER
        `file`.v1.ItemType.ITEM_TYPE_NOTE -> ItemType.NOTE
        `file`.v1.ItemType.ITEM_TYPE_TASK_LIST -> ItemType.TASK_LIST
        `file`.v1.ItemType.ITEM_TYPE_UNSPECIFIED -> ItemType.UNSPECIFIED
    }

    fun toDomain(proto: UpdateFolderResponse): Folder =
        toDomain(requireFolder(proto.folder, "UpdateFolderResponse"))

    fun toProto(params: CreateFolderParams): CreateFolderRequest = CreateFolderRequest(
        parent_dir = params.parentDir,
        name = params.name
    )

    fun toProto(params: UpdateFolderParams): UpdateFolderRequest = UpdateFolderRequest(
        folder_path = params.folderPath,
        new_name = params.newName
    )

    fun toProto(params: DeleteFolderParams): DeleteFolderRequest = DeleteFolderRequest(
        folder_path = params.folderPath
    )

    private fun requireFolder(folder: `file`.v1.Folder?, responseType: String): `file`.v1.Folder =
        requireNotNull(folder) { "$responseType did not include a folder payload" }
}