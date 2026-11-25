package net.onefivefour.echolist.feature.browser.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
internal class BrowserFolderRenameTest : FunSpec({
    val dispatcher = StandardTestDispatcher()
    beforeSpec { Dispatchers.setMain(dispatcher) }
    afterSpec { Dispatchers.resetMain() }
    val original = FileEntry("Projects/Ideas", "Ideas", ItemType.FOLDER, FileMetadata.Folder(7))
    val other = FileEntry("Projects/Archive", "Archive", ItemType.FOLDER, FileMetadata.Folder(0))
    val note = FileEntry("Projects/Note", "Note", ItemType.NOTE, FileMetadata.Note("note", 0, ""))
    fun repository() = RenameRepository(listOf(original, other, note))
    fun model(repo: RenameRepository, notifier: FakeDirectoryChangeNotifier = FakeDirectoryChangeNotifier()) =
        BrowserViewModel("Projects", repo, notifier)

    test("rename starts with the existing folder and name without changing the listing or persisting") {
        runTest(dispatcher) {
            val repo = repository()
            val vm = model(repo)
            advanceUntilIdle()
            val entries = vm.uiState.value.fileEntries
            vm.startFolderRename(original.path)
            val draft = vm.uiState.value.folderDraft!!
            draft.name shouldBe "Ideas"
            draft.originalEntry shouldBe original
            draft.originalIndex shouldBe 1
            draft.isRenaming shouldBe true
            vm.uiState.value.fileEntries shouldBe entries
            repo.updates shouldBe emptyList()
            vm.dismissFolderDraft()
            vm.uiState.value.folderDraft shouldBe null
            vm.uiState.value.fileEntries shouldBe entries
        }
    }

    test("unknown paths and non-folder entries cannot enter rename mode") {
        runTest(dispatcher) {
            val vm = model(repository())
            advanceUntilIdle()
            vm.startFolderRename("missing")
            vm.uiState.value.folderDraft shouldBe null
            vm.startFolderRename(note.path)
            vm.uiState.value.folderDraft shouldBe null
        }
    }

    test("create and rename actions preserve and refocus the currently active input") {
        runTest(dispatcher) {
            val vm = model(repository())
            advanceUntilIdle()
            vm.startFolderDraft()
            vm.onFolderNameChange("Unsaved creation")
            val createId = vm.uiState.value.folderDraft!!.id
            vm.startFolderRename(original.path)
            vm.uiState.value.folderDraft!!.id shouldBe createId
            vm.uiState.value.folderDraft!!.name shouldBe "Unsaved creation"
            vm.uiState.value.folderDraft!!.isRenaming shouldBe false
            vm.uiState.value.folderDraft!!.focusRequest shouldBe 1
            vm.dismissFolderDraft()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("Unsaved rename")
            val renameId = vm.uiState.value.folderDraft!!.id
            vm.startFolderDraft()
            vm.startFolderRename(other.path)
            vm.uiState.value.folderDraft!!.id shouldBe renameId
            vm.uiState.value.folderDraft!!.originalEntry shouldBe original
            vm.uiState.value.folderDraft!!.name shouldBe "Unsaved rename"
            vm.uiState.value.folderDraft!!.focusRequest shouldBe 2
        }
    }

    test("unchanged confirmation exits editing without a redundant request and blank names stay editable") {
        runTest(dispatcher) {
            val repo = repository()
            val vm = model(repo)
            advanceUntilIdle()
            vm.startFolderRename(original.path)
            vm.confirmFolderDraft()
            vm.uiState.value.folderDraft shouldBe null
            repo.updates shouldBe emptyList()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange(" \t ")
            vm.confirmFolderDraft()
            vm.uiState.value.folderDraft!!.canConfirm shouldBe false
            repo.updates shouldBe emptyList()
        }
    }

    test("confirmation sends the original path and trimmed name once and retains metadata with the backend result") {
        runTest(dispatcher) {
            val response = CompletableDeferred<Result<Folder>>()
            val repo = repository().apply { update = { response.await() } }
            val vm = model(repo)
            advanceUntilIdle()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("  Zebra  ")
            vm.confirmFolderDraft()
            vm.confirmFolderDraft()
            vm.dismissFolderDraft()
            vm.startFolderDraft()
            vm.startFolderRename(other.path)
            vm.onFolderNameChange("Ignored")
            runCurrent()
            repo.updates shouldBe listOf(UpdateFolderParams(original.path, "Zebra"))
            vm.uiState.value.folderDraft!!.isSaving shouldBe true
            response.complete(Result.success(Folder("Projects/Backend name", "Backend name")))
            advanceUntilIdle()
            vm.uiState.value.folderDraft shouldBe null
            vm.uiState.value.fileEntries.map { it.path } shouldBe listOf(other.path, "Projects/Backend name", note.path)
            vm.uiState.value.fileEntries[1].metadata shouldBe original.metadata
            repo.creates shouldBe emptyList()
        }
    }

    test("a rename failure keeps the old folder and edited name and allows a corrected retry") {
        runTest(dispatcher) {
            val repo = repository().apply { update = { Result.failure(Exception("Already exists")) } }
            val vm = model(repo)
            advanceUntilIdle()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("Archive")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            vm.uiState.value.folderDraft!!.name shouldBe "Archive"
            vm.uiState.value.folderDraft!!.error shouldBe "Already exists"
            vm.uiState.value.folderDraft!!.isSaving shouldBe false
            vm.uiState.value.fileEntries.contains(original) shouldBe true
            vm.onFolderNameChange("New name")
            vm.uiState.value.folderDraft!!.error shouldBe null
            repo.update = { Result.success(Folder("Projects/New name", it.newName)) }
            vm.confirmFolderDraft()
            advanceUntilIdle()
            repo.updates.size shouldBe 2
            vm.uiState.value.fileEntries.any { it.path == original.path } shouldBe false
        }
    }

    test("refresh preserves the editor even if the original folder disappears remotely") {
        runTest(dispatcher) {
            val repo = repository()
            val vm = model(repo)
            advanceUntilIdle()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("Still editing")
            val draft = vm.uiState.value.folderDraft
            repo.list = { Result.success(emptyList()) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.folderDraft shouldBe draft
            repo.list = { Result.failure(Exception("Offline")) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.folderDraft shouldBe draft
            vm.uiState.value.error shouldBe BrowserError.LOAD_FAILED
        }
    }

    test("an older read arriving after rename success cannot restore the old path") {
        runTest(dispatcher) {
            val repo = repository()
            val vm = model(repo)
            advanceUntilIdle()
            val oldRead = CompletableDeferred<Result<List<FileEntry>>>()
            repo.list = { oldRead.await() }
            vm.refresh()
            runCurrent()
            repo.list = { Result.success(listOf(original, other, note)) }
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("Renamed")
            vm.confirmFolderDraft()
            runCurrent()
            oldRead.complete(Result.success(listOf(original, other, note)))
            advanceUntilIdle()
            vm.uiState.value.fileEntries.map { it.path } shouldBe listOf(other.path, "Projects/Renamed", note.path)
        }
    }

    test("a notification can list the new path before the rename response without duplicating the folder") {
        runTest(dispatcher) {
            val repo = repository()
            val notifier = FakeDirectoryChangeNotifier()
            val vm = model(repo, notifier)
            advanceUntilIdle()
            val renamed = original.copy(path = "Projects/New", title = "New")
            repo.update = {
                repo.list = { Result.success(listOf(renamed, other, note)) }
                notifier.notifyChanged("Projects")
                Result.success(Folder(renamed.path, renamed.title))
            }
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("New")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            vm.uiState.value.fileEntries.count { it.path == renamed.path } shouldBe 1
            vm.uiState.value.fileEntries.any { it.path == original.path } shouldBe false
        }
    }

    test("stale old and new paths are merged until acknowledgement then legitimate changes are respected") {
        runTest(dispatcher) {
            val repo = repository()
            val vm = model(repo)
            advanceUntilIdle()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("New")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            val renamed = original.copy(path = "Projects/New", title = "New")
            repo.list = { Result.success(listOf(original, renamed)) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe listOf(renamed)
            repo.list = { Result.success(listOf(renamed)) }
            vm.refresh()
            advanceUntilIdle()
            // Once acknowledged, the backend may recreate the old name and remove the new one.
            repo.list = { Result.success(listOf(original)) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe listOf(original)
        }
    }

    test("successive renames before acknowledgement do not resurrect intermediate paths") {
        runTest(dispatcher) {
            val repo = repository()
            val vm = model(repo)
            advanceUntilIdle()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("Middle")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            vm.startFolderRename("Projects/Middle")
            vm.onFolderNameChange("Final")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            val final = original.copy(path = "Projects/Final", title = "Final")
            val middle = original.copy(path = "Projects/Middle", title = "Middle")
            repo.list = { Result.success(listOf(original, middle, final)) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe listOf(final)
            repo.list = { Result.success(listOf(final)) }
            vm.refresh()
            advanceUntilIdle()
            repo.list = { Result.success(emptyList()) }
            vm.refresh()
            advanceUntilIdle()
            vm.uiState.value.fileEntries shouldBe emptyList()
        }
    }

    test("cancellation unlocks the editor without showing a save error") {
        runTest(dispatcher) {
            val repo = repository().apply { update = { throw CancellationException("Cancelled") } }
            val vm = model(repo)
            advanceUntilIdle()
            vm.startFolderRename(original.path)
            vm.onFolderNameChange("New")
            vm.confirmFolderDraft()
            advanceUntilIdle()
            vm.uiState.value.folderDraft!!.isSaving shouldBe false
            vm.uiState.value.folderDraft!!.error shouldBe null
            vm.uiState.value.fileEntries.contains(original) shouldBe true
        }
    }
})

private class RenameRepository(initialEntries: List<FileEntry>) : FileRepository {
    val updates = mutableListOf<UpdateFolderParams>()
    val creates = mutableListOf<CreateFolderParams>()
    var list: suspend () -> Result<List<FileEntry>> = { Result.success(initialEntries) }
    var update: suspend (UpdateFolderParams) -> Result<Folder> = {
        Result.success(Folder("Projects/${it.newName}", it.newName))
    }
    override suspend fun listFiles(parentDir: String) = list()
    override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> {
        updates.add(params)
        return update(params)
    }
    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> {
        creates.add(params)
        return Result.success(Folder("Projects/${params.name}", params.name))
    }
    override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> = error("Unexpected deletion")
}