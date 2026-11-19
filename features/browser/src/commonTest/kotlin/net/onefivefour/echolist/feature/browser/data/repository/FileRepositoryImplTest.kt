package net.onefivefour.echolist.feature.browser.data.repository

import `file`.v1.CreateFolderResponse
import `file`.v1.DeleteFolderResponse
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.data.source.network.FakeFileRemoteDataSource
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams

internal class FileRepositoryImplTest : FunSpec({

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

    // -- CreateFolder --

    test("createFolder returns mapped folder on success").config(invocations = 20) {
        checkAll(arbCreateFolderParams, arbProtoFolder) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.success(CreateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.createFolder(params)

            result.isSuccess shouldBe true
            val folder = result.getOrThrow()
            folder.path shouldBe protoFolder.path
            folder.name shouldBe protoFolder.name
        }
    }

    test("createFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbCreateFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.createFolderResult = Result.success(
                CreateFolderResponse(folder = `file`.v1.Folder(path = "/test/", name = "test"))
            )
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.createFolder(params)

            fake.lastCreateRequest?.parent_dir shouldBe params.parentDir
            fake.lastCreateRequest?.name shouldBe params.name
        }
    }

    test("createFolder returns failure when network throws") {
        val fake = FakeFileRemoteDataSource()
        fake.createFolderResult = Result.failure(NetworkException.ServerError(500, "boom"))
        val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.createFolder(CreateFolderParams("", "n"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    test("repository operations propagate cancellation without reporting a directory change") {
        val fake = FakeFileRemoteDataSource()
        val cancellation = CancellationException("Cancelled")
        fake.createFolderResult = Result.failure(cancellation)
        fake.listFilesResult = Result.failure(cancellation)
        fake.updateFolderResult = Result.failure(cancellation)
        fake.deleteFolderResult = Result.failure(cancellation)
        val notifier = FakeDirectoryChangeNotifier()
        val repo = FileRepositoryImpl(fake, notifier, Dispatchers.Unconfined)
        shouldThrow<CancellationException> { repo.createFolder(CreateFolderParams("", "Ideas")) }
        shouldThrow<CancellationException> { repo.listFiles("") }
        shouldThrow<CancellationException> { repo.updateFolder(UpdateFolderParams("Ideas", "Other")) }
        shouldThrow<CancellationException> { repo.deleteFolder(DeleteFolderParams("Ideas")) }
        notifier.emissions shouldBe emptyList()
    }

    // -- ListFiles --

    test("listFiles returns mapped entries on success") {
        val protoEntries = listOf(
            `file`.v1.FileEntry(
                path = "/file1.txt",
                title = "file1.txt",
                item_type = `file`.v1.ItemType.ITEM_TYPE_NOTE
            ),
            `file`.v1.FileEntry(path = "/file2.md", title = "file2.md", item_type = `file`.v1.ItemType.ITEM_TYPE_NOTE),
            `file`.v1.FileEntry(path = "/folder1", title = "folder1", item_type = `file`.v1.ItemType.ITEM_TYPE_FOLDER)
        )
        val fake = FakeFileRemoteDataSource()
        fake.listFilesResult = Result.success(ListFilesResponse(entries = protoEntries))
        val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.listFiles("/home/user")

        result.isSuccess shouldBe true
        result.getOrThrow().size shouldBe 3
        result.getOrThrow()[0].path shouldBe "/file1.txt"
        result.getOrThrow()[1].path shouldBe "/file2.md"
        result.getOrThrow()[2].path shouldBe "/folder1"
    }

    test("listFiles forwards correct parent_dir to data source") {
        val fake = FakeFileRemoteDataSource()
        fake.listFilesResult = Result.success(ListFilesResponse(entries = emptyList()))
        val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        repo.listFiles("/some/path")

        fake.lastListRequest?.parent_dir shouldBe "/some/path"
    }

    test("listFiles returns empty list when response has no entries") {
        val fake = FakeFileRemoteDataSource()
        fake.listFilesResult = Result.success(ListFilesResponse(entries = emptyList()))
        val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.listFiles("")

        result.isSuccess shouldBe true
        result.getOrThrow() shouldBe emptyList()
    }

    test("listFiles returns failure when network throws") {
        val fake = FakeFileRemoteDataSource()
        fake.listFilesResult = Result.failure(NetworkException.TimeoutError("timed out"))
        val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.listFiles("/any")

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.TimeoutError>()
    }

    // -- UpdateFolder --

    test("updateFolder returns mapped folder on success").config(invocations = 20) {
        checkAll(arbUpdateFolderParams, arbProtoFolder) { params, protoFolder ->
            val fake = FakeFileRemoteDataSource()
            fake.updateFolderResult = Result.success(UpdateFolderResponse(folder = protoFolder))
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.updateFolder(params)

            result.isSuccess shouldBe true
            val folder = result.getOrThrow()
            folder.path shouldBe protoFolder.path
            folder.name shouldBe protoFolder.name
        }
    }

    test("updateFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbUpdateFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.updateFolderResult = Result.success(
                UpdateFolderResponse(folder = `file`.v1.Folder(path = "/new/", name = "new"))
            )
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.updateFolder(params)

            fake.lastUpdateRequest?.folder_path shouldBe params.folderPath
            fake.lastUpdateRequest?.new_name shouldBe params.newName
        }
    }

    test("updateFolder returns failure when network throws") {
        val fake = FakeFileRemoteDataSource()
        fake.updateFolderResult = Result.failure(NetworkException.ClientError(404, "not found"))
        val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.updateFolder(UpdateFolderParams("p/", "n"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- DeleteFolder --

    test("deleteFolder returns Unit on success").config(invocations = 20) {
        checkAll(arbDeleteFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            fake.deleteFolderResult = Result.success(DeleteFolderResponse())
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.deleteFolder(params)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe Unit
        }
    }

    test("deleteFolder forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbDeleteFolderParams) { params ->
            val fake = FakeFileRemoteDataSource()
            val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.deleteFolder(params)

            fake.lastDeleteRequest?.folder_path shouldBe params.folderPath
        }
    }

    test("deleteFolder returns failure when network throws") {
        val fake = FakeFileRemoteDataSource()
        fake.deleteFolderResult = Result.failure(NetworkException.NetworkError("timeout"))
        val repo = FileRepositoryImpl(fake, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.deleteFolder(DeleteFolderParams("p/"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
