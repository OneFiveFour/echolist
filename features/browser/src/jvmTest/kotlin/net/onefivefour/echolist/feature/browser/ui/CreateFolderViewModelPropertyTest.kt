package net.onefivefour.echolist.feature.browser.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.Folder
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository

/**
 * Property-based tests for CreateFolderViewModel.
 *
 * Feature: create-folder-dialog
 * Properties 1, 3, 4, 5
 *
 * Validates: Requirements 1.1, 1.5, 2.1, 2.2, 2.3, 3.1
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class CreateFolderViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // -- Generators --

    /** Non-blank strings with leading/trailing whitespace for trimming tests. */
    val arbPaddedNonBlankName: Arb<String> = Arb.string(1..30)
        .filter { it.trim().isNotBlank() }
        .map { "  $it  " }

    val arbPath: Arb<String> = Arb.string(1..50)

    val arbErrorMessage: Arb<String> = Arb.string(1..100)

    // -- Mock --

    class MockFileRepository : FileRepository {
        var createFolderHandler: suspend (CreateFolderParams) -> Result<Folder> = {
            Result.success(Folder(path = "${it.parentDir}/${it.name}", name = it.name))
        }
        val createFolderCalls = mutableListOf<CreateFolderParams>()

        override suspend fun createFolder(params: CreateFolderParams): Result<Folder> {
            createFolderCalls.add(params)
            return createFolderHandler(params)
        }

        override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> =
            Result.success(emptyList())

        override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
            Result.failure(UnsupportedOperationException())
    }

    // -- Property 1: Show/dismiss dialog round trip --

    test("Feature: create-folder-dialog, Property 1: showDialog then dismissDialog returns to default hidden state") {
        checkAll(PropTestConfig(iterations = 100), arbPath) { path ->
            runTest(testDispatcher) {
                val repo = MockFileRepository()
                val vm = CreateFolderViewModel(parentDir = path, fileRepository = repo)

                vm.uiState.value.isVisible shouldBe false

                vm.showDialog()
                vm.uiState.value.isVisible shouldBe true
                vm.uiState.value.folderName shouldBe ""
                vm.uiState.value.error shouldBe null
                vm.uiState.value.isLoading shouldBe false

                vm.onDismissDialog()
                vm.uiState.value.isVisible shouldBe false
                vm.uiState.value.folderName shouldBe ""
                vm.uiState.value.error shouldBe null
                vm.uiState.value.isLoading shouldBe false
            }
        }
    }

    // -- Property 3: Confirm sends trimmed name with correct parentDir --

    test("Feature: create-folder-dialog, Property 3: onConfirm sends trimmed name with correct path") {
        checkAll(PropTestConfig(iterations = 100), arbPaddedNonBlankName, arbPath) { name, path ->
            runTest(testDispatcher) {
                val repo = MockFileRepository()
                val vm = CreateFolderViewModel(parentDir = path, fileRepository = repo)

                vm.showDialog()
                vm.onNameChange(name)
                vm.onConfirm()
                testScheduler.advanceUntilIdle()

                repo.createFolderCalls.size shouldBe 1
                repo.createFolderCalls[0].parentDir shouldBe path
                repo.createFolderCalls[0].name shouldBe name.trim()
            }
        }
    }

    // -- Property 4: Successful creation closes dialog --

    test("Feature: create-folder-dialog, Property 4: successful creation resets state to hidden") {
        checkAll(PropTestConfig(iterations = 100), arbPaddedNonBlankName, arbPath) { name, path ->
            runTest(testDispatcher) {
                val repo = MockFileRepository()
                repo.createFolderHandler = { params ->
                    Result.success(Folder(path = "${params.parentDir}/${params.name}", name = params.name))
                }
                val vm = CreateFolderViewModel(parentDir = path, fileRepository = repo)

                vm.showDialog()
                vm.onNameChange(name)
                vm.onConfirm()
                testScheduler.advanceUntilIdle()

                vm.uiState.value.isVisible shouldBe false
                vm.uiState.value.folderName shouldBe ""
                vm.uiState.value.isLoading shouldBe false
                vm.uiState.value.error shouldBe null
            }
        }
    }

    // -- Property 5: Failed creation keeps dialog open with error --

    test("Feature: create-folder-dialog, Property 5: failed creation keeps dialog open with error message") {
        checkAll(PropTestConfig(iterations = 100), arbPaddedNonBlankName, arbErrorMessage) { name, errorMsg ->
            runTest(testDispatcher) {
                val repo = MockFileRepository()
                repo.createFolderHandler = {
                    Result.failure(RuntimeException(errorMsg))
                }
                val vm = CreateFolderViewModel(parentDir = "test", fileRepository = repo)

                vm.showDialog()
                vm.onNameChange(name)
                vm.onConfirm()
                testScheduler.advanceUntilIdle()

                vm.uiState.value.isVisible shouldBe true
                vm.uiState.value.isLoading shouldBe false
                vm.uiState.value.error shouldBe errorMsg
            }
        }
    }
})