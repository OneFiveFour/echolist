@file:OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    androidx.compose.ui.InternalComposeUiApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class
)

package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.use
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import javax.swing.SwingUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.designsystem.ui.theme.ThemeManager
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListDefaultTheme
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.data.repository.FakeFileRepository
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType

internal class FolderDraftLayoutTest : FunSpec({
    test("empty directories display a focused blank draft and confirmation hides the keyboard") {
        SwingUtilities.invokeAndWait {
            for (width in listOf(320, 402, 840, 1440)) {
                val state = mutableStateOf(BrowserUiState(emptyList(), folderDraft = FolderDraft(1)))
                val keyboard = RecordingKeyboard()
                var confirmations = 0
                var cancellations = 0
                ImageComposeScene(width, 874) {
                    CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboard) {
                        EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                            BrowserScreen(
                                state.value,
                                onFolderNameChange = {
                                    state.value = state.value.copy(
                                        folderDraft = state.value.folderDraft!!.copy(name = it)
                                    )
                                },
                                onConfirmFolder = {
                                    confirmations++
                                    state.value = state.value.copy(
                                        folderDraft = state.value.folderDraft!!.copy(isSaving = true)
                                    )
                                },
                                onDismissFolder = { cancellations++ }
                            )
                        }
                    }
                }.use { scene ->
                    repeat(3) { scene.render().close() }
                    val field = requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                    field.config[SemanticsProperties.EditableText].text shouldBe ""
                    field.config[SemanticsProperties.Focused] shouldBe true
                    (scene.draftNodes().taggedDraft("browser-empty") == null) shouldBe true
                    requireNotNull(scene.draftNodes().taggedDraft("folder-draft-confirm"))
                        .config.contains(SemanticsProperties.Disabled) shouldBe true
                    keyboard.shows shouldBe 1
                    field.config[SemanticsActions.SetText].action!!.invoke(AnnotatedString("Ideas"))
                    scene.render().close()
                    requireNotNull(scene.draftNodes().taggedDraft("folder-draft-confirm"))
                        .config[SemanticsActions.OnClick].action!!.invoke()
                    repeat(2) { scene.render().close() }
                    confirmations shouldBe 1
                    cancellations shouldBe 0
                    keyboard.hides shouldBe 1
                    requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                        .config.contains(SemanticsProperties.Disabled) shouldBe true
                    (scene.draftNodes().taggedDraft("folder-draft-confirm") == null) shouldBe true
                    (scene.draftNodes().taggedDraft("folder-draft-cancel") == null) shouldBe true
                    scene.render().use { image ->
                        File(
                            "build/folder-draft-saving-$width.png"
                        ).writeBytes(requireNotNull(image.encodeToData()).bytes)
                    }
                }
            }
        }
    }

    test("IME Done and hardware Enter confirm while Escape cancels and focus loss does not save") {
        SwingUtilities.invokeAndWait {
            for (action in listOf("ime", "enter", "escape", "blur", "cancel")) {
                var confirmations = 0
                var cancellations = 0
                var focusManager: FocusManager? = null
                ImageComposeScene(402, 874) {
                    focusManager = LocalFocusManager.current
                    EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                        BrowserScreen(
                            BrowserUiState(emptyList(), folderDraft = FolderDraft(1, "Ideas")),
                            onConfirmFolder = { confirmations++ },
                            onDismissFolder = { cancellations++ }
                        )
                    }
                }.use { scene ->
                    repeat(3) { scene.render().close() }
                    val field = requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                    when (action) {
                        "ime" -> field.config[SemanticsActions.OnImeAction].action!!.invoke()
                        "enter", "escape" -> {
                            val key = if (action == "enter") Key.Enter else Key.Escape
                            scene.sendKeyEvent(KeyEvent(key, KeyEventType.KeyDown))
                            scene.sendKeyEvent(KeyEvent(key, KeyEventType.KeyUp))
                        }
                        "blur" -> requireNotNull(focusManager).clearFocus()
                        "cancel" -> requireNotNull(scene.draftNodes().taggedDraft("folder-draft-cancel"))
                            .config[SemanticsActions.OnClick].action!!.invoke()
                    }
                    confirmations shouldBe if (action == "ime" || action == "enter") 1 else 0
                    cancellations shouldBe if (action == "escape" || action == "cancel") 1 else 0
                }
            }
        }
    }

    test("scrolling a draft out of view and back does not steal focus or reopen the keyboard") {
        SwingUtilities.invokeAndWait {
            val keyboard = RecordingKeyboard()
            var focusManager: FocusManager? = null
            val gridState = LazyGridState()
            val entries = (1..100).map {
                FileEntry("folder-draft-$it", "Folder $it", ItemType.FOLDER, FileMetadata.Folder(0))
            }
            ImageComposeScene(402, 874) {
                focusManager = LocalFocusManager.current
                CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboard) {
                    EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                        FileOverview(
                            entries,
                            false,
                            null,
                            {},
                            {},
                            {},
                            gridState = gridState,
                            folderDraft = FolderDraft(1)
                        )
                    }
                }
            }.use { scene ->
                repeat(3) { scene.render().close() }
                requireNotNull(focusManager).clearFocus()
                gridState.requestScrollToItem(80)
                repeat(3) { scene.render().close() }
                (scene.draftNodes().taggedDraft("folder-draft-name") == null) shouldBe true
                gridState.requestScrollToItem(0)
                repeat(3) { scene.render().close() }
                requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                    .config[SemanticsProperties.Focused] shouldBe false
                keyboard.shows shouldBe 1
            }
        }
    }

    test("the draft remains above existing folders and errors have space in short narrow windows") {
        SwingUtilities.invokeAndWait {
            for ((width, height) in listOf(320 to 400, 402 to 500, 840 to 400, 1440 to 700)) {
                val entry = FileEntry("Existing", "Existing", ItemType.FOLDER, FileMetadata.Folder(0))
                ImageComposeScene(width, height) {
                    EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                        BrowserScreen(
                            BrowserUiState(
                                emptyList(),
                                fileEntries = listOf(entry),
                                folderDraft = FolderDraft(
                                    1,
                                    "Ideas",
                                    error = "A folder with this name already exists. Please choose another name."
                                )
                            )
                        )
                    }
                }.use { scene ->
                    repeat(3) { scene.render().close() }
                    val nodes = scene.draftNodes()
                    val draft = requireNotNull(nodes.taggedDraft("browser-folder-draft"))
                    val error = requireNotNull(nodes.taggedDraft("folder-draft-error"))
                    (error.boundsInRoot.bottom <= draft.boundsInRoot.bottom) shouldBe true
                    (draft.boundsInRoot.bottom <= height) shouldBe true
                    nodes.taggedDraft("browser-folder")?.let { folder ->
                        (folder.boundsInRoot.top >= draft.boundsInRoot.top) shouldBe true
                    }
                    scene.render().use { image ->
                        File(
                            "build/folder-draft-error-$width.png"
                        ).writeBytes(requireNotNull(image.encodeToData()).bytes)
                    }
                }
            }
        }
    }

    test("recreating the browser retains creation and rename drafts while navigation discards them") {
        SwingUtilities.invokeAndWait {
            val dispatcher = StandardTestDispatcher()
            Dispatchers.setMain(dispatcher)
            fun renderScene(scene: ImageComposeScene) {
                dispatcher.scheduler.advanceUntilIdle()
                scene.render().close()
                dispatcher.scheduler.advanceUntilIdle()
            }
            try {
                for (isRenaming in listOf(false, true)) {
                    val repository = FakeFileRepository().apply {
                        if (isRenaming) {
                            listFilesResult = Result.success(
                                listOf(
                                    FileEntry("Projects", "Projects", ItemType.FOLDER, FileMetadata.Folder(3))
                                )
                            )
                        }
                    }
                    val storeOwner = object : ViewModelStoreOwner {
                        override val viewModelStore = ViewModelStore()
                    }
                    val application = koinApplication {
                        modules(
                            module {
                                viewModel { params ->
                                    BrowserViewModel(
                                        params.get(),
                                        repository,
                                        FakeDirectoryChangeNotifier()
                                    )
                                }
                            }
                        )
                    }
                    var discardDraft: () -> Unit = {}
                    var navigationCount = 0
                    fun createScene(): ImageComposeScene {
                        val lifecycleOwner = object : LifecycleOwner {
                            override val lifecycle = LifecycleRegistry.createUnsafe(this).apply {
                                currentState = Lifecycle.State.RESUMED
                            }
                        }
                        return ImageComposeScene(402, 874) {
                            CompositionLocalProvider(
                                LocalViewModelStoreOwner provides storeOwner,
                                LocalLifecycleOwner provides lifecycleOwner
                            ) {
                                KoinIsolatedContext(application) {
                                    EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                                        BrowserFeature(
                                            selectedFolder = "",
                                            navToFolder = { navigationCount++ },
                                            navToNote = {},
                                            navToTaskList = {},
                                            navToRemindersSummary = {},
                                            navToTasksSummary = { navigationCount++ },
                                            onNavigationStateChange = { _, discard -> discardDraft = discard }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    fun startEditor(scene: ImageComposeScene) {
                        if (isRenaming) {
                            requireNotNull(scene.draftNodes().taggedDraft("browser-folder"))
                                .config[SemanticsActions.OnLongClick].action!!.invoke()
                        } else {
                            requireNotNull(scene.draftNodes().taggedDraft("browser-create"))
                                .config[SemanticsActions.OnClick].action!!.invoke()
                            renderScene(scene)
                            requireNotNull(scene.draftNodes().taggedDraft("create-New folder"))
                                .config[SemanticsActions.OnClick].action!!.invoke()
                        }
                        repeat(3) { renderScene(scene) }
                    }
                    try {
                        createScene().use { scene ->
                            repeat(4) { renderScene(scene) }
                            startEditor(scene)
                            navigationCount shouldBe 0
                            requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                                .config[SemanticsActions.SetText].action!!.invoke(AnnotatedString("Kept on rotation"))
                            repeat(2) { renderScene(scene) }
                        }
                        createScene().use { scene ->
                            repeat(4) { renderScene(scene) }
                            requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                                .config[SemanticsProperties.EditableText].text shouldBe "Kept on rotation"
                            repository.lastCreateParams shouldBe null
                            repository.callLog.any { it.startsWith("updateFolder") } shouldBe false
                            // The app invokes this before system back or incoming notification navigation.
                            discardDraft()
                            repeat(3) { renderScene(scene) }
                            (scene.draftNodes().taggedDraft("folder-draft-name") == null) shouldBe true
                            startEditor(scene)
                            requireNotNull(scene.draftNodes().taggedDraft("nav-Due"))
                                .config[SemanticsActions.OnClick].action!!.invoke()
                            repeat(3) { renderScene(scene) }
                            navigationCount shouldBe 1
                            (scene.draftNodes().taggedDraft("folder-draft-name") == null) shouldBe true
                        }
                    } finally {
                        storeOwner.viewModelStore.clear()
                        application.close()
                    }
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
    }

    test("longpress edits the existing item in place with its entire name selected in both layouts and themes") {
        SwingUtilities.invokeAndWait {
            val entries = listOf("Archive", "Middle", "Zebra").map {
                FileEntry(it, it, ItemType.FOLDER, FileMetadata.Folder(3))
            } + FileEntry("note", "Note", ItemType.NOTE, FileMetadata.Note("note", 0, "Preview"))
            for (isDarkTheme in listOf(false, true)) {
                for (width in listOf(320, 840, 1440)) {
                    val keyboard = RecordingKeyboard()
                    var navigationCount = 0
                    var confirmations = 0
                    val state = mutableStateOf(BrowserUiState(emptyList(), fileEntries = entries))
                    ImageComposeScene(width, 874) {
                        CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboard) {
                            EchoListTheme(
                                isDarkTheme = isDarkTheme,
                                themeManager = ThemeManager(listOf(EchoListDefaultTheme))
                            ) {
                                BrowserScreen(
                                    state.value,
                                    onFolderClick = { navigationCount++ },
                                    onFolderLongClick = { path ->
                                        val original = entries.first { it.path == path }
                                        state.value = state.value.copy(
                                            folderDraft = FolderDraft(
                                                id = 1,
                                                name = original.title,
                                                originalEntry = original,
                                                originalIndex = 1
                                            )
                                        )
                                    },
                                    onFolderNameChange = { name ->
                                        state.value = state.value.copy(
                                            folderDraft = state.value.folderDraft!!.copy(name = name)
                                        )
                                    },
                                    onConfirmFolder = { confirmations++ },
                                    onDismissFolder = { state.value = state.value.copy(folderDraft = null) }
                                )
                            }
                        }
                    }.use { scene ->
                        repeat(3) { scene.render().close() }
                        val target = scene.draftNodes().filter {
                            it.config.getOrNull(SemanticsProperties.TestTag) == "browser-folder"
                        }[1]
                        val originalPosition = target.boundsInRoot.topLeft
                        target.config[SemanticsActions.OnLongClick].action!!.invoke()
                        repeat(3) { scene.render().close() }
                        val field = requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                        field.config[SemanticsProperties.EditableText].text shouldBe "Middle"
                        field.config[SemanticsProperties.TextSelectionRange] shouldBe TextRange(0, 6)
                        field.config[SemanticsProperties.Focused] shouldBe true
                        requireNotNull(scene.draftNodes().taggedDraft("browser-folder-draft"))
                            .boundsInRoot.topLeft shouldBe originalPosition
                        scene.draftNodes().count {
                            it.config.getOrNull(SemanticsProperties.TestTag) == "browser-folder"
                        } shouldBe 2
                        keyboard.shows shouldBe 1
                        navigationCount shouldBe 0
                        scene.draftNodes().any {
                            it.config.getOrNull(SemanticsProperties.ContentDescription) == listOf("Save folder name")
                        } shouldBe true
                        field.config[SemanticsActions.SetText].action!!.invoke(AnnotatedString("Updated"))
                        repeat(2) { scene.render().close() }
                        requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                            .config[SemanticsActions.SetSelection].action!!.invoke(2, 2, false)
                        scene.render().close()
                        requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                            .config[SemanticsProperties.TextSelectionRange] shouldBe TextRange(2)
                        // A refresh without the original entry must keep the active rename input visible.
                        state.value = state.value.copy(fileEntries = emptyList(), error = BrowserError.LOAD_FAILED)
                        repeat(3) { scene.render().close() }
                        requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                            .config[SemanticsProperties.EditableText].text shouldBe "Updated"
                        scene.render().use { image ->
                            File("build/folder-rename-$width-${if (isDarkTheme) "dark" else "light"}.png")
                                .writeBytes(requireNotNull(image.encodeToData()).bytes)
                        }
                        confirmations shouldBe 0
                        keyboard.shows shouldBe 1
                    }
                }
            }
        }
    }

    test("crossing adaptive breakpoints keeps the draft input and does not reopen the keyboard") {
        SwingUtilities.invokeAndWait {
            val window = DraftWindowInfo(IntSize(599, 874))
            val keyboard = RecordingKeyboard()
            val state = mutableStateOf(BrowserUiState(emptyList(), folderDraft = FolderDraft(1, "Ideas")))
            ImageComposeScene(1600, 874) {
                CompositionLocalProvider(
                    LocalWindowInfo provides window,
                    LocalSoftwareKeyboardController provides keyboard
                ) {
                    EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                        BrowserScreen(state.value)
                    }
                }
            }.use { scene ->
                scene.constraints = Constraints(maxWidth = 599, maxHeight = 874)
                repeat(3) { scene.render().close() }
                keyboard.shows shouldBe 1
                for (width in listOf(840, 1200, 1600, 599)) {
                    window.containerSize = IntSize(width, 874)
                    scene.constraints = Constraints(maxWidth = width, maxHeight = 874)
                    repeat(3) { scene.render().close() }
                    val field = requireNotNull(scene.draftNodes().taggedDraft("folder-draft-name"))
                    field.config[SemanticsProperties.EditableText].text shouldBe "Ideas"
                    field.config[SemanticsProperties.Focused] shouldBe true
                    keyboard.shows shouldBe 1
                }
                // A failed request must not refocus the input or reopen the keyboard.
                state.value = state.value.copy(folderDraft = state.value.folderDraft!!.copy(isSaving = true))
                repeat(2) { scene.render().close() }
                state.value = state.value.copy(
                    folderDraft = state.value.folderDraft!!.copy(isSaving = false, error = "Offline")
                )
                repeat(2) { scene.render().close() }
                keyboard.shows shouldBe 1
            }
        }
    }
})

private class RecordingKeyboard : SoftwareKeyboardController {
    var shows = 0
    var hides = 0
    override fun show() {
        shows++
    }
    override fun hide() {
        hides++
    }
}

private class DraftWindowInfo(initialSize: IntSize) : WindowInfo {
    override val isWindowFocused = true
    override var containerSize by mutableStateOf(initialSize)
    override val containerDpSize: DpSize get() = DpSize(containerSize.width.dp, containerSize.height.dp)
}

private fun ImageComposeScene.draftNodes(): List<SemanticsNode> {
    fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
    return semanticsOwners.flatMap { descendants(it.unmergedRootSemanticsNode) }
}

private fun List<SemanticsNode>.taggedDraft(tag: String): SemanticsNode? = firstOrNull {
    it.config.getOrNull(SemanticsProperties.TestTag) == tag
}