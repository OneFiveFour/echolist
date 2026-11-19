package net.onefivefour.echolist.feature.browser.data.repository

import net.onefivefour.echolist.core.files.domain.normalizePath
import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderResponse
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import net.onefivefour.echolist.feature.browser.data.source.network.FakeFileRemoteDataSource

/**
 * Feature: proto-api-update
 * Property 4: FileRepository creates folders correctly
 * Property 5: FileRepository lists files correctly
 * Property 6: FileRepository updates folders correctly
 * Property 7: FileRepository deletes folders correctly
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4
 */
internal class FileRepositoryImplPropertyTest : FunSpec({

    // -- Generators --

    val arbProtoFolder = arbitrary {
        `file`.v1.Folder(
            path = Arb.string(1..100).bind(),
            name = Arb.string(1..100).bind()
        )
    }

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

    val arbProtoFileEntry = arbitrary {
        `file`.v1.FileEntry(
            path = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            item_type = `file`.v1.ItemType.ITEM_TYPE_FOLDER
        )
    }

    val arbEntries = Arb.list(arbProtoFileEntry, 0..20)

    // ---------------------------------------------------------------
    // Property 4: FileRepository creates folders correctly
    // Validates: Requirements 3.1
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 4: FileRepository creates folders correctly - returns mapped Folder") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbCreateFolderParams,
            arbProtoFolder
        ) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.success(CreateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.createFolder(params)

            result.isSuccess shouldBe true
            val folder = result.getOrThrow()
            folder.path shouldBe normalizePath(protoFolder.path)
            folder.name shouldBe protoFolder.name
        }
    }

    test("Feature: proto-api-update, Property 4: FileRepository creates folders correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbCreateFolderParams,
            arbProtoFolder
        ) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.success(CreateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.createFolder(params)

            fake.lastCreateRequest?.parent_dir shouldBe params.parentDir
            fake.lastCreateRequest?.name shouldBe params.name
        }
    }

    // ---------------------------------------------------------------
    // Property 5: FileRepository lists files correctly
    // Validates: Requirements 3.2
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 5: FileRepository lists files correctly - returns mapped entries") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.string(0..100),
            arbEntries
        ) { parentPath, entries ->
            val fake = FakeFileRemoteDataSource()
            fake.listFilesResult = Result.success(ListFilesResponse(entries = entries))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.listFiles(parentPath)

            result.isSuccess shouldBe true
            result.getOrThrow().size shouldBe entries.size
        }
    }

    test("Feature: proto-api-update, Property 5: FileRepository lists files correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.string(0..100)
        ) { parentPath ->
            val fake = FakeFileRemoteDataSource()
            fake.listFilesResult = Result.success(ListFilesResponse(entries = emptyList()))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.listFiles(parentPath)

            fake.lastListRequest?.parent_dir shouldBe parentPath
        }
    }

    // ---------------------------------------------------------------
    // Property 6: FileRepository updates folders correctly
    // Validates: Requirements 3.3
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 6: FileRepository updates folders correctly - returns mapped Folder") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbUpdateFolderParams,
            arbProtoFolder
        ) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.updateFolderResult = Result.success(UpdateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.updateFolder(params)

            result.isSuccess shouldBe true
            val folder = result.getOrThrow()
            folder.path shouldBe normalizePath(protoFolder.path)
            folder.name shouldBe protoFolder.name
        }
    }

    test("Feature: proto-api-update, Property 6: FileRepository updates folders correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbUpdateFolderParams,
            arbProtoFolder
        ) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.updateFolderResult = Result.success(UpdateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.updateFolder(params)

            fake.lastUpdateRequest?.folder_path shouldBe params.folderPath
            fake.lastUpdateRequest?.new_name shouldBe params.newName
        }
    }

    // ---------------------------------------------------------------
    // Property 7: FileRepository deletes folders correctly
    // Validates: Requirements 3.4
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 7: FileRepository deletes folders correctly - returns success") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbDeleteFolderParams
        ) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.deleteFolderResult = Result.success(DeleteFolderResponse())
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.deleteFolder(params)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe Unit
        }
    }

    test("Feature: proto-api-update, Property 7: FileRepository deletes folders correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbDeleteFolderParams
        ) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.deleteFolderResult = Result.success(DeleteFolderResponse())
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.deleteFolder(params)

            fake.lastDeleteRequest?.folder_path shouldBe params.folderPath
        }
    }

    // ---------------------------------------------------------------
    // Feature: create-folder-dialog
    // Property 6: Repository emits directoryChanged on successful mutation
    // Validates: Requirements 4.5
    // ---------------------------------------------------------------

    test("Feature: create-folder-dialog, Property 6: Repository emits directoryChanged on successful createFolder") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbCreateFolderParams,
            arbProtoFolder
        ) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.success(CreateFolderResponse(folder = protoFolder))
            val notifier = FakeDirectoryChangeNotifier()
            val repo = FileRepositoryImpl(fake, notifier, Dispatchers.Unconfined)

            repo.createFolder(params)

            notifier.emissions.size shouldBe 1
            notifier.emissions.first() shouldBe params.parentDir
        }
    }

    test(
        "Feature: create-folder-dialog, Property 6: Repository does not emit directoryChanged on failed createFolder"
    ) {
        checkAll(
            PropTestConfig(iterations = 20),
            arbCreateFolderParams
        ) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.failure(RuntimeException("network error"))
            val notifier = FakeDirectoryChangeNotifier()
            val repo = FileRepositoryImpl(fake, notifier, Dispatchers.Unconfined)

            repo.createFolder(params)

            notifier.emissions.shouldBeEmpty()
        }
    }
})