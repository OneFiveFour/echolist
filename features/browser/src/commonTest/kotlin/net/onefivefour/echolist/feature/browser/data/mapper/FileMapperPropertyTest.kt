package net.onefivefour.echolist.feature.browser.data.mapper

import net.onefivefour.echolist.core.files.domain.normalizePath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams

/**
 * Feature: proto-api-update
 * Property 1: FileMapper domain-to-proto field preservation
 * Property 2: FileMapper proto-to-domain field preservation
 *
 * Validates: Requirements 1.4, 1.5, 1.6, 1.8, 1.9, 1.10, 1.11, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7
 */
internal class FileMapperPropertyTest : FunSpec({

    // -- Generators --

    val arbCreateFolderParams = arbitrary {
        CreateFolderParams(
            parentDir = Arb.string(0..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

    val arbUpdateFolderParams = arbitrary {
        UpdateFolderParams(
            folderPath = Arb.string(1..100).bind(),
            newName = Arb.string(1..100).bind()
        )
    }

    val arbDeleteFolderParams = arbitrary {
        DeleteFolderParams(
            folderPath = Arb.string(1..100).bind()
        )
    }

    val arbProtoFolder = arbitrary {
        `file`.v1.Folder(
            path = Arb.string(1..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

    // -- Property 1: Domain -> Proto field preservation --

    test("Feature: proto-api-update, Property 1: CreateFolderParams -> CreateFolderRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbCreateFolderParams) { params ->
            val proto = FileMapper.toProto(params)
            proto.parent_dir shouldBe params.parentDir
            proto.name shouldBe params.name
        }
    }

    test("Feature: proto-api-update, Property 1: UpdateFolderParams -> UpdateFolderRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbUpdateFolderParams) { params ->
            val proto = FileMapper.toProto(params)
            proto.folder_path shouldBe params.folderPath
            proto.new_name shouldBe params.newName
        }
    }

    test("Feature: proto-api-update, Property 1: DeleteFolderParams -> DeleteFolderRequest preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbDeleteFolderParams) { params ->
            val proto = FileMapper.toProto(params)
            proto.folder_path shouldBe params.folderPath
        }
    }

    // -- Property 2: Proto -> Domain field preservation --

    test("Feature: proto-api-update, Property 2: proto Folder -> domain Folder normalizes path and preserves name") {
        checkAll(PropTestConfig(iterations = 100), arbProtoFolder) { proto ->
            val domain = FileMapper.toDomain(proto)
            domain.path shouldBe normalizePath(proto.path)
            domain.name shouldBe proto.name
        }
    }

    test("Feature: proto-api-update, Property 2: CreateFolderResponse -> domain Folder normalizes path and preserves name") {
        checkAll(PropTestConfig(iterations = 100), arbProtoFolder) { protoFolder ->
            val response = `file`.v1.CreateFolderResponse(folder = protoFolder)
            val domain = FileMapper.toDomain(response)
            domain.path shouldBe normalizePath(protoFolder.path)
            domain.name shouldBe protoFolder.name
        }
    }

    test("Feature: proto-api-update, Property 2: ListFilesResponse -> domain entries list preserves all entries") {
        val arbProtoFileEntry = arbitrary {
            `file`.v1.FileEntry(
                path = Arb.string(1..100).bind(),
                title = Arb.string(1..100).bind(),
                item_type = `file`.v1.ItemType.ITEM_TYPE_FOLDER
            )
        }
        checkAll(PropTestConfig(iterations = 100), Arb.list(arbProtoFileEntry, 0..10)) { entries ->
            val response = `file`.v1.ListFilesResponse(entries = entries)
            val domainList = FileMapper.toDomain(response)
            domainList shouldHaveSize entries.size
        }
    }

    test("Feature: proto-api-update, Property 2: UpdateFolderResponse -> domain Folder normalizes path and preserves name") {
        checkAll(PropTestConfig(iterations = 100), arbProtoFolder) { protoFolder ->
            val response = `file`.v1.UpdateFolderResponse(folder = protoFolder)
            val domain = FileMapper.toDomain(response)
            domain.path shouldBe normalizePath(protoFolder.path)
            domain.name shouldBe protoFolder.name
        }
    }
})