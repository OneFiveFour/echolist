package net.onefivefour.echolist.feature.browser.ui

import `file`.v1.CreateFolderRequest
import `file`.v1.CreateFolderResponse
import `file`.v1.Folder
import `file`.v1.FolderMetadata
import `file`.v1.ItemType
import `file`.v1.ListFilesResponse
import `file`.v1.UpdateFolderRequest
import `file`.v1.UpdateFolderResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.data.repository.FileRepositoryImpl
import net.onefivefour.echolist.feature.browser.data.source.network.FakeFileRemoteDataSource
import net.onefivefour.echolist.feature.browser.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata

/** Uses the actual repository and mapper with the backend's different response path formats. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class BrowserFolderPathReconciliationTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    test("renaming a folder with six children never shows two copies with the backend path formats") {
        runTest(dispatcher) {
            for (parent in listOf("", "Projects")) {
                val backend = BackendFolderPaths(parent, hasExistingFolder = true)
                val notifier = FakeDirectoryChangeNotifier()
                val repository = FileRepositoryImpl(backend.source, notifier, Dispatchers.Unconfined)
                val vm = BrowserViewModel(parent, repository, notifier)
                val snapshots = mutableListOf<BrowserUiState>()
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.toList(snapshots) }
                advanceUntilIdle()
                for (name in listOf("NO 1", "NO 2")) {
                    val oldPath = vm.uiState.value.fileEntries.single().path
                    vm.startFolderRename(oldPath)
                    vm.onFolderNameChange(name)
                    vm.confirmFolderDraft()
                    advanceUntilIdle()
                    vm.uiState.value.fileEntries.size shouldBe 1
                    vm.uiState.value.fileEntries.single().title shouldBe name
                    vm.uiState.value.fileEntries.single().path shouldBe backend.path(name)
                    vm.uiState.value.fileEntries.single().metadata shouldBe FileMetadata.Folder(6)
                    backend.fake.lastUpdateRequest!!.folder_path shouldBe oldPath
                    vm.refresh()
                    advanceUntilIdle()
                    vm.uiState.value.fileEntries.size shouldBe 1
                }
                snapshots.all { it.fileEntries.size <= 1 } shouldBe true
            }
        }
    }

    test("creating a folder merges the trailing-slash response with the slash-free listing") {
        runTest(dispatcher) {
            for (parent in listOf("", "Projects")) {
                val backend = BackendFolderPaths(parent, hasExistingFolder = false)
                val notifier = FakeDirectoryChangeNotifier()
                val repository = FileRepositoryImpl(backend.source, notifier, Dispatchers.Unconfined)
                val vm = BrowserViewModel(parent, repository, notifier)
                advanceUntilIdle()
                vm.startFolderDraft()
                vm.onFolderNameChange("NO 1")
                vm.confirmFolderDraft()
                advanceUntilIdle()
                vm.uiState.value.fileEntries.size shouldBe 1
                vm.uiState.value.fileEntries.single().path shouldBe backend.path("NO 1")
                vm.uiState.value.fileEntries.single().metadata shouldBe FileMetadata.Folder(0)
            }
        }
    }

    test("acknowledgement of an actual backend rename retires local state so remote deletion stays deleted") {
        runTest(dispatcher) {
            val backend = BackendFolderPaths("Projects", hasExistingFolder = true)
            val notifier = FakeDirectoryChangeNotifier()
            val repository = FileRepositoryImpl(backend.source, notifier, Dispatchers.Unconfined)
            val vm = BrowserViewModel("Projects", repository, notifier)
            advanceUntilIdle()
            vm.startFolderRename(backend.path("Original"))
            vm.onFolderNameChange("NO 1")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            backend.fake.listFilesResult = Result.success(ListFilesResponse())
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe emptyList()
        }
    }
})

private class BackendFolderPaths(private val parent: String, hasExistingFolder: Boolean) {
    val fake = FakeFileRemoteDataSource()
    init { if (hasExistingFolder) setListing("Original", 6) }

    fun path(name: String) = if (parent.isEmpty()) name else "$parent/$name"

    private fun setListing(name: String, children: Int) {
        fake.listFilesResult = Result.success(ListFilesResponse(entries = listOf(
            `file`.v1.FileEntry(
                path = path(name),
                title = name,
                item_type = ItemType.ITEM_TYPE_FOLDER,
                folder_metadata = FolderMetadata(child_count = children)
            )
        )))
    }

    val source = object : FileRemoteDataSource by fake {
        override suspend fun updateFolder(request: UpdateFolderRequest): UpdateFolderResponse {
            setListing(request.new_name, 6)
            fake.updateFolderResult = Result.success(UpdateFolderResponse(
                folder = Folder(path = path(request.new_name) + "/", name = request.new_name)
            ))
            return fake.updateFolder(request)
        }

        override suspend fun createFolder(request: CreateFolderRequest): CreateFolderResponse {
            setListing(request.name, 0)
            fake.createFolderResult = Result.success(CreateFolderResponse(
                folder = Folder(path = path(request.name) + "/", name = request.name)
            ))
            return fake.createFolder(request)
        }
    }
}
