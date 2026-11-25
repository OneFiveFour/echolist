package net.onefivefour.echolist.feature.browser.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.Folder
import net.onefivefour.echolist.feature.browser.domain.model.ItemType
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams

@OptIn(ExperimentalCoroutinesApi::class)
internal class BrowserFolderCreationTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }

    fun model(repository: FolderCreationRepository, notifier: DirectoryChangeNotifier = FakeDirectoryChangeNotifier()) =
        BrowserViewModel("Projects", repository, notifier)

    test("starting and editing a draft makes no request and repeated start keeps the name") {
        runTest(dispatcher) {
            val repository = FolderCreationRepository()
            val vm = model(repository)
            advanceUntilIdle()
            vm.startFolderDraft()
            vm.uiState.value.folderDraft!!.name shouldBe ""
            val id = vm.uiState.value.folderDraft!!.id
            vm.onFolderNameChange("Ideas")
            vm.startFolderDraft()
            vm.uiState.value.folderDraft!!.id shouldBe id
            vm.uiState.value.folderDraft!!.name shouldBe "Ideas"
            vm.uiState.value.folderDraft!!.focusRequest shouldBe 1
            repository.calls shouldBe emptyList()
            vm.dismissFolderDraft()
            vm.uiState.value.folderDraft shouldBe null
            vm.startFolderDraft()
            (vm.uiState.value.folderDraft!!.id != id) shouldBe true
        }
    }

    test("empty and whitespace names cannot be confirmed") {
        runTest(dispatcher) {
            val repository = FolderCreationRepository()
            val vm = model(repository)
            advanceUntilIdle()
            vm.startFolderDraft()
            for (name in listOf("", " ", "\t\n")) {
                vm.onFolderNameChange(name)
                vm.confirmFolderDraft()
                vm.uiState.value.folderDraft!!.canConfirm shouldBe false
            }
            advanceUntilIdle()
            repository.calls shouldBe emptyList()
        }
    }

    test("confirmation trims the name and locks all draft actions until persistence completes") {
        runTest(dispatcher) {
            val response = CompletableDeferred<Result<Folder>>()
            val repository = FolderCreationRepository().apply { create = { response.await() } }
            val vm = model(repository)
            advanceUntilIdle()
            vm.startFolderDraft()
            vm.onFolderNameChange("  Ideas  ")
            vm.confirmFolderDraft()
            vm.confirmFolderDraft()
            vm.startFolderDraft()
            vm.dismissFolderDraft()
            vm.onFolderNameChange("Changed")
            vm.onDeleteCurrentFolderClick()
            runCurrent()
            vm.uiState.value.folderDraft!!.isSaving shouldBe true
            vm.uiState.value.folderDraft!!.name shouldBe "Ideas"
            repository.calls shouldBe listOf(CreateFolderParams("Projects", "Ideas"))
            repository.deleteCalls shouldBe 0
            response.complete(Result.success(Folder("Projects/Canonical", "Canonical")))
            advanceUntilIdle()
            vm.uiState.value.folderDraft shouldBe null
            vm.uiState.value.fileEntries.single().path shouldBe "Projects/Canonical"
            vm.uiState.value.fileEntries.single().title shouldBe "Canonical"
            vm.uiState.value.canDeleteCurrentFolder shouldBe false
        }
    }

    test("failure retains the submitted name and supports editing and retry") {
        runTest(dispatcher) {
            val repository = FolderCreationRepository().apply {
                create = { Result.failure(Exception("Already exists")) }
            }
            val vm = model(repository)
            advanceUntilIdle()
            vm.startFolderDraft()
            vm.onFolderNameChange("Ideas")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            vm.uiState.value.folderDraft!!.name shouldBe "Ideas"
            vm.uiState.value.folderDraft!!.error shouldBe "Already exists"
            vm.uiState.value.folderDraft!!.isSaving shouldBe false
            vm.onFolderNameChange("Other")
            vm.uiState.value.folderDraft!!.error shouldBe null
            repository.create = { Result.success(Folder("Projects/Other", it.name)) }
            vm.confirmFolderDraft()
            advanceUntilIdle()
            repository.calls.size shouldBe 2
            vm.uiState.value.folderDraft shouldBe null
        }
    }

    test("refresh and directory notifications preserve an active draft including failures") {
        runTest(dispatcher) {
            val repository = FolderCreationRepository()
            val notifier = FakeDirectoryChangeNotifier()
            val vm = model(repository, notifier)
            advanceUntilIdle()
            vm.startFolderDraft()
            vm.onFolderNameChange("Ideas")
            val draft = vm.uiState.value.folderDraft
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.folderDraft shouldBe draft
            repository.list = { Result.failure(Exception("Offline")) }
            notifier.notifyChanged("Projects")
            advanceUntilIdle()
            vm.uiState.value.folderDraft shouldBe draft
            vm.uiState.value.error shouldBe BrowserError.LOAD_FAILED
        }
    }

    test("a listing received before the create response is merged without duplicate paths") {
        runTest(dispatcher) {
            val notifier = FakeDirectoryChangeNotifier()
            val entry = FileEntry("Projects/Ideas", "Ideas", ItemType.FOLDER, FileMetadata.Folder(0))
            val repository = FolderCreationRepository()
            val vm = model(repository, notifier)
            advanceUntilIdle()
            repository.create = {
                repository.list = { Result.success(listOf(entry)) }
                notifier.notifyChanged("Projects")
                Result.success(Folder(entry.path, entry.title))
            }
            vm.startFolderDraft()
            vm.onFolderNameChange("Ideas")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe listOf(entry)
        }
    }

    test("an older refresh cannot overwrite a successful creation and stale listings keep it visible") {
        runTest(dispatcher) {
            val repository = FolderCreationRepository()
            val vm = model(repository)
            advanceUntilIdle()
            val oldListing = CompletableDeferred<Result<List<FileEntry>>>()
            repository.list = { oldListing.await() }
            vm.refresh()
            runCurrent()
            repository.list = { Result.success(emptyList()) }
            vm.startFolderDraft()
            vm.onFolderNameChange("Ideas")
            vm.confirmFolderDraft()
            runCurrent()
            oldListing.complete(Result.success(emptyList()))
            advanceUntilIdle()
            vm.uiState.value.fileEntries.single().path shouldBe "Projects/Ideas"
            vm.uiState.value.isRefreshing shouldBe false
            repository.list = { Result.failure(Exception("Offline")) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries.single().path shouldBe "Projects/Ideas"
        }
    }

    test("a server acknowledgement retires the local overlay so a subsequent removal is respected") {
        runTest(dispatcher) {
            val repository = FolderCreationRepository()
            val vm = model(repository)
            advanceUntilIdle()
            vm.startFolderDraft()
            vm.onFolderNameChange("Ideas")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            val entries = vm.uiState.value.fileEntries
            repository.list = { Result.success(entries) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe entries
            repository.list = { Result.success(emptyList()) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe emptyList()
        }
    }

    test("completion of a previous creation reload cannot unlock a newer creation") {
        runTest(dispatcher) {
            val repository = FolderCreationRepository()
            val vm = model(repository)
            advanceUntilIdle()
            val firstReload = CompletableDeferred<Result<List<FileEntry>>>()
            repository.list = { firstReload.await() }
            vm.startFolderDraft()
            vm.onFolderNameChange("First")
            vm.confirmFolderDraft()
            runCurrent()
            val secondCreate = CompletableDeferred<Result<Folder>>()
            repository.create = { secondCreate.await() }
            vm.startFolderDraft()
            vm.onFolderNameChange("Second")
            vm.confirmFolderDraft()
            runCurrent()
            firstReload.complete(Result.success(emptyList()))
            runCurrent()
            vm.uiState.value.folderDraft!!.isSaving shouldBe true
            vm.confirmFolderDraft()
            repository.calls.size shouldBe 2
            repository.list = { Result.success(emptyList()) }
            secondCreate.complete(Result.success(Folder("Projects/Second", "Second")))
            advanceUntilIdle()
            vm.uiState.value.fileEntries.map { it.title } shouldBe listOf("First", "Second")
        }
    }
})

private class FolderCreationRepository : FileRepository {
    val calls = mutableListOf<CreateFolderParams>()
    var deleteCalls = 0
    var create: suspend (CreateFolderParams) -> Result<Folder> = {
        Result.success(Folder("${it.parentDir}/${it.name}", it.name))
    }
    var list: suspend (String) -> Result<List<FileEntry>> = { Result.success(emptyList()) }
    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> {
        calls.add(params)
        return create(params)
    }
    override suspend fun listFiles(parentDir: String) = list(parentDir)
    override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> = error("Unexpected rename")
    override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> {
        deleteCalls++
        return Result.success(Unit)
    }
}