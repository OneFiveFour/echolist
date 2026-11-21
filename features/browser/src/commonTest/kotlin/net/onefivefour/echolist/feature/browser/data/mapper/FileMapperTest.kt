package net.onefivefour.echolist.feature.browser.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams

/**
 * Unit tests for FileMapper transformations.
 * Tests specific examples, edge cases, and field name conversions.
 */
internal class FileMapperTest : FunSpec({

    // -- Proto -> Domain transformations --

    test("toDomain transforms proto Folder to domain Folder") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/documents",
            name = "documents"
        )

        val domain = FileMapper.toDomain(protoFolder)

        domain.path shouldBe "home/user/documents"
        domain.name shouldBe "documents"
    }

    test("toDomain transforms CreateFolderResponse to domain Folder") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/projects",
            name = "projects"
        )
        val response = `file`.v1.CreateFolderResponse(folder = protoFolder)

        val domain = FileMapper.toDomain(response)

        domain.path shouldBe "home/user/projects"
        domain.name shouldBe "projects"
    }

    test("toDomain transforms ListFilesResponse with multiple entries") {
        val protoEntries = listOf(
            `file`.v1.FileEntry(
                path = "/file1.txt",
                title = "file1.txt",
                item_type = `file`.v1.ItemType.ITEM_TYPE_NOTE
            ),
            `file`.v1.FileEntry(path = "/file2.md", title = "file2.md", item_type = `file`.v1.ItemType.ITEM_TYPE_NOTE),
            `file`.v1.FileEntry(path = "/folder1", title = "folder1", item_type = `file`.v1.ItemType.ITEM_TYPE_FOLDER)
        )
        val response = `file`.v1.ListFilesResponse(entries = protoEntries)

        val domainList = FileMapper.toDomain(response)

        domainList shouldHaveSize 3
        domainList[0].path shouldBe "/file1.txt"
        domainList[1].path shouldBe "/file2.md"
        domainList[2].path shouldBe "folder1"
    }

    test("toDomain transforms ListFilesResponse with empty entries") {
        val response = `file`.v1.ListFilesResponse(entries = emptyList())

        val domainList = FileMapper.toDomain(response)

        domainList.shouldBeEmpty()
    }

    test("toDomain transforms UpdateFolderResponse to domain Folder") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/renamed",
            name = "renamed"
        )
        val response = `file`.v1.UpdateFolderResponse(folder = protoFolder)

        val domain = FileMapper.toDomain(response)

        domain.path shouldBe "home/user/renamed"
        domain.name shouldBe "renamed"
    }

    test("folder mutation responses and listing entries share one canonical path") {
        for (canonicalPath in listOf("NO 1", "Projects/NO 1", "Projects/Übung 1")) {
            for (wirePath in listOf(canonicalPath, "$canonicalPath/", "/$canonicalPath/")) {
                val folder = `file`.v1.Folder(path = wirePath, name = "Name")
                FileMapper.toDomain(`file`.v1.CreateFolderResponse(folder = folder)).path shouldBe canonicalPath
                FileMapper.toDomain(`file`.v1.UpdateFolderResponse(folder = folder)).path shouldBe canonicalPath
                val listedFolder = `file`.v1.FileEntry(
                    path = wirePath,
                    title = "Name",
                    item_type = `file`.v1.ItemType.ITEM_TYPE_FOLDER,
                    folder_metadata = `file`.v1.FolderMetadata(child_count = 6)
                )
                FileMapper.toDomain(listedFolder).path shouldBe canonicalPath
                FileMapper.toDomain(listedFolder).metadata shouldBe
                    FileMetadata.Folder(6)
            }
        }
    }

    test("normalizing folder paths does not change note and task-list paths") {
        for (type in listOf(`file`.v1.ItemType.ITEM_TYPE_NOTE, `file`.v1.ItemType.ITEM_TYPE_TASK_LIST)) {
            val entry = `file`.v1.FileEntry(path = "/Projects/NO 1/", title = "NO 1", item_type = type)
            FileMapper.toDomain(entry).path shouldBe entry.path
        }
    }

    // -- Domain -> Proto transformations --

    test("task-list urgency counts preserve missing, zero and populated values over the wire") {
        for ((overdueTaskCount, dueSoonTaskCount) in listOf(null to null, 0 to 0, 2 to 5, null to 3, 4 to null)) {
            val protoEntry = `file`.v1.FileEntry(
                path = "tasks",
                title = "Release checklist",
                item_type = `file`.v1.ItemType.ITEM_TYPE_TASK_LIST,
                task_list_metadata = `file`.v1.TaskListMetadata(
                    id = "tasks-1",
                    total_task_count = 10,
                    done_task_count = 3,
                    overdue_task_count = overdueTaskCount,
                    due_soon_task_count = dueSoonTaskCount
                )
            )
            val response = `file`.v1.ListFilesResponse(entries = listOf(protoEntry))
            val decoded = `file`.v1.ListFilesResponse.ADAPTER.decode(response.encode())

            val metadata = FileMapper.toDomain(decoded).single().metadata as FileMetadata.TaskList

            metadata.totalTaskCount shouldBe 10
            metadata.doneTaskCount shouldBe 3
            metadata.overdueTaskCount shouldBe overdueTaskCount
            metadata.dueSoonTaskCount shouldBe dueSoonTaskCount
        }
    }

    test("toProto transforms CreateFolderParams to CreateFolderRequest with snake_case fields") {
        val params = CreateFolderParams(
            parentDir = "/home/user",
            name = "new_folder"
        )

        val proto = FileMapper.toProto(params)

        proto.parent_dir shouldBe "/home/user"
        proto.name shouldBe "new_folder"
    }

    test("toProto transforms UpdateFolderParams to UpdateFolderRequest with snake_case fields") {
        val params = UpdateFolderParams(
            folderPath = "/home/user/old_name",
            newName = "new_name"
        )

        val proto = FileMapper.toProto(params)

        proto.folder_path shouldBe "/home/user/old_name"
        proto.new_name shouldBe "new_name"
    }

    test("toProto transforms DeleteFolderParams to DeleteFolderRequest with snake_case fields") {
        val params = DeleteFolderParams(
            folderPath = "/home/user/to_delete"
        )

        val proto = FileMapper.toProto(params)

        proto.folder_path shouldBe "/home/user/to_delete"
    }

    // -- Edge cases --

    test("toDomain handles folder with empty name") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user",
            name = ""
        )

        val domain = FileMapper.toDomain(protoFolder)

        domain.path shouldBe "home/user"
        domain.name shouldBe ""
    }

    test("toDomain handles folder with special characters in name") {
        val protoFolder = `file`.v1.Folder(
            path = "/home/user/special-folder_123",
            name = "special-folder_123"
        )

        val domain = FileMapper.toDomain(protoFolder)

        domain.path shouldBe "home/user/special-folder_123"
        domain.name shouldBe "special-folder_123"
    }

    test("toDomain handles ListFilesResponse with single entry") {
        val response = `file`.v1.ListFilesResponse(
            entries = listOf(
                `file`.v1.FileEntry(
                    path = "/single.txt",
                    title = "single.txt",
                    item_type = `file`.v1.ItemType.ITEM_TYPE_NOTE
                )
            )
        )

        val domainList = FileMapper.toDomain(response)

        domainList shouldHaveSize 1
        domainList[0].path shouldBe "/single.txt"
    }
})
