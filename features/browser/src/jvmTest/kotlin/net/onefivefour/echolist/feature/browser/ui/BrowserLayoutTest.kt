@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.use
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import javax.swing.SwingUtilities
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.designsystem.ui.theme.ThemeManager
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListDefaultTheme
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType

class BrowserLayoutTest : FunSpec({
    test("phone and desktop layouts render with compact folders, navigation and working create menu") {
        SwingUtilities.invokeAndWait {
            for (isDarkTheme in listOf(false, true)) {
                for ((width, height) in listOf(402 to 874, 1440 to 1024, 320 to 640, 600 to 700, 839 to 700, 840 to 700, 840 to 400)) {
                    var dueClicks = 0
                    var reminderClicks = 0
                    var createClicks = 0
                    ImageComposeScene(width, height) {
                        EchoListTheme(isDarkTheme = isDarkTheme, themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                            BrowserScreen(
                                browserUiState = BrowserUiState(listOf(BreadcrumbItem("Home", "")), fileEntries = sampleEntries()),
                                navToTasksSummary = { dueClicks++ },
                                navToRemindersSummary = { reminderClicks++ },
                                createItemCallbacks = CreateItemCallbacks(onCreateNote = { createClicks++ })
                            )
                        }
                    }.use { scene ->
                        scene.render().use { image ->
                            File("build/browser-$width-$height-${if (isDarkTheme) "dark" else "light"}.png")
                                .writeBytes(requireNotNull(image.encodeToData()).bytes)
                        }
                        val nodes = scene.browserNodes()
                        val isDesktop = width >= 840
                        (nodes.tagged("browser-sidebar") != null) shouldBe isDesktop
                        // Short windows need not compose off-screen lazy grid items.
                        if (height >= 640) {
                            val folder = requireNotNull(nodes.tagged("browser-folder"))
                            val file = requireNotNull(nodes.tagged("browser-file"))
                            (folder.boundsInRoot.height < file.boundsInRoot.height) shouldBe true
                            val folders = nodes.filter { it.config.getOrNull(SemanticsProperties.TestTag) == "browser-folder" }
                            (folders[0].boundsInRoot.top == folders[1].boundsInRoot.top) shouldBe (width >= 1200)
                            (file.boundsInRoot.top >= folders.last().boundsInRoot.bottom) shouldBe true
                        }
                        nodes.tagged("nav-Due")!!.config[SemanticsActions.OnClick].action!!.invoke()
                        dueClicks shouldBe 1
                        nodes.tagged(if (isDesktop) "nav-Reminders" else "nav-Remind")!!.config[SemanticsActions.OnClick].action!!.invoke()
                        reminderClicks shouldBe 1
                        val create = requireNotNull(nodes.tagged("browser-create"))
                        (create.boundsInRoot.bottom <= height) shouldBe true
                        create.config[SemanticsActions.OnClick].action!!.invoke()
                        scene.render().close()
                        requireNotNull(scene.browserNodes().tagged("create-New note"))
                            .config[SemanticsActions.OnClick].action!!.invoke()
                        createClicks shouldBe 1
                    }
                }
            }
        }
    }
})

private fun sampleEntries(): List<FileEntry> = listOf(
    FileEntry("Projects", "Projects", ItemType.FOLDER, FileMetadata.Folder(12)),
    FileEntry("Personal", "Personal", ItemType.FOLDER, FileMetadata.Folder(8)),
    FileEntry("Archive", "Archive", ItemType.FOLDER, FileMetadata.Folder(0)),
    FileEntry("capture", "Quick capture", ItemType.NOTE, FileMetadata.Note("capture", 0, "Ideas from the train, books to find, a thought about slower mornings…")),
    FileEntry("weekly", "Weekly reset", ItemType.TASK_LIST, FileMetadata.TaskList("weekly", 0, 7, 3)),
    FileEntry("reading", "Reading list", ItemType.NOTE, FileMetadata.Note("reading", 0, "The Creative Act, An Immense World, Four Thousand Weeks…")),
    FileEntry("errands", "Home errands", ItemType.TASK_LIST, FileMetadata.TaskList("errands", 0, 6, 4))
)

private fun ImageComposeScene.browserNodes(): List<SemanticsNode> {
    fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
    return semanticsOwners.flatMap { descendants(it.unmergedRootSemanticsNode) }
}
private fun List<SemanticsNode>.tagged(tag: String) = firstOrNull {
    it.config.getOrNull(SemanticsProperties.TestTag) == tag
}
