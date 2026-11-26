package net.onefivefour.echolist.feature.browser.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType
import net.onefivefour.echolist.feature.browser.domain.model.Folder
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository

/**
 * Tests for [BrowserViewModel.clearErrorAndReload].
 *
 * Validates that stale error state from a previous session (e.g. expired auth token)
 * is cleared when the composable re-enters composition after re-authentication.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class BrowserViewModelClearErrorTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    test("clearErrorAndReload clears a stale error and reloads successfully") {
        runTest(testDispatcher) {
            var shouldFail = true
            val repo = object : FileRepository {
                override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> {
                    return if (shouldFail) {
                        Result.failure(Exception("code Unauthenticated token expired"))
                    } else {
                        Result.success(emptyList())
                    }
                }
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
                    Result.failure(UnsupportedOperationException())
            }

            val vm = BrowserViewModel(
                parentDir = "",
                fileRepository = repo,
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )

            // Let init's loadData() complete — it should fail
            advanceUntilIdle()
            vm.uiState.value.error.shouldNotBeNull()
            vm.uiState.value.error shouldBe BrowserError.LOAD_FAILED

            // Simulate successful re-authentication: network calls now succeed
            shouldFail = false

            // Simulate composable re-entering composition after re-auth
            vm.clearErrorAndReload()
            advanceUntilIdle()

            vm.uiState.value.error.shouldBeNull()
            vm.uiState.value.isLoading shouldBe false
        }
    }

    test("clearErrorAndReload reloads data even when previous load failed") {
        runTest(testDispatcher) {
            var callCount = 0
            val repo = object : FileRepository {
                override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> {
                    callCount++
                    return Result.failure(Exception("token expired"))
                }
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
                    Result.failure(UnsupportedOperationException())
            }

            val vm = BrowserViewModel(
                parentDir = "",
                fileRepository = repo,
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )

            // Let init's loadData() complete
            advanceUntilIdle()
            val callsAfterInit = callCount
            callsAfterInit shouldBe 1

            // clearErrorAndReload should trigger another listFiles call
            vm.clearErrorAndReload()
            advanceUntilIdle()

            callCount shouldBe callsAfterInit + 1
        }
    }

    test("empty non-root folder can be deleted and requests back navigation") {
        runTest(testDispatcher) {
            var deletedPath: String? = null
            val repo = object : FileRepository {
                override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> =
                    Result.success(emptyList())
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> {
                    deletedPath = params.folderPath
                    return Result.success(Unit)
                }
            }

            val vm = BrowserViewModel(
                parentDir = "parent/child",
                fileRepository = repo,
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )
            advanceUntilIdle()

            vm.uiState.value.canDeleteCurrentFolder shouldBe true
            val navigation = async { vm.navigateBack.first() }

            vm.onDeleteCurrentFolder()
            advanceUntilIdle()

            deletedPath shouldBe "parent/child"
            navigation.await() shouldBe Unit
            vm.uiState.value.isDeletingFolder shouldBe false
        }
    }

    test("root and non-empty folders are not deletable") {
        runTest(testDispatcher) {
            var deleteCallCount = 0
            fun repoWithEntries(entries: List<FileEntry>) = object : FileRepository {
                override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> =
                    Result.success(entries)
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> {
                    deleteCallCount++
                    return Result.success(Unit)
                }
            }
            val childEntry = FileEntry(
                path = "child",
                title = "child",
                itemType = ItemType.FOLDER,
                metadata = FileMetadata.Folder(childCount = 0)
            )

            val nonEmptyVm = BrowserViewModel(
                parentDir = "parent",
                fileRepository = repoWithEntries(listOf(childEntry)),
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )
            advanceUntilIdle()

            nonEmptyVm.uiState.value.canDeleteCurrentFolder shouldBe false
            nonEmptyVm.onDeleteCurrentFolder()
            advanceUntilIdle()

            val rootVm = BrowserViewModel(
                parentDir = "",
                fileRepository = repoWithEntries(emptyList()),
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )
            advanceUntilIdle()

            rootVm.uiState.value.canDeleteCurrentFolder shouldBe false
            rootVm.onDeleteCurrentFolder()
            advanceUntilIdle()

            deleteCallCount shouldBe 0
        }
    }

    test("a load failure without exception message is still reported instead of an empty folder") {
        runTest(testDispatcher) {
            val repo = object : FileRepository {
                override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> =
                    Result.failure(Exception())
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
                    Result.failure(UnsupportedOperationException())
            }
            val vm = BrowserViewModel(
                parentDir = "",
                fileRepository = repo,
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )
            advanceUntilIdle()
            vm.uiState.value.error shouldBe BrowserError.LOAD_FAILED
            vm.uiState.value.fileEntries shouldBe emptyList()
            vm.uiState.value.isLoading shouldBe false
        }
    }

    test("a failed deletion reports a typed error and stays on the folder") {
        runTest(testDispatcher) {
            val repo = object : FileRepository {
                override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> =
                    Result.success(emptyList())
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
                    Result.failure(Exception("internal: stack trace and server details"))
            }
            val vm = BrowserViewModel(
                parentDir = "parent/child",
                fileRepository = repo,
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )
            advanceUntilIdle()
            vm.onDeleteCurrentFolder()
            advanceUntilIdle()
            vm.uiState.value.error shouldBe BrowserError.DELETE_FAILED
            vm.uiState.value.isDeletingFolder shouldBe false
        }
    }
})