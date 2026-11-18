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
    test("navigation shows the current folder and allows ancestor clicks only on desktop") {
        SwingUtilities.invokeAndWait {
            val path = listOf(
                BreadcrumbItem("Home", ""),
                BreadcrumbItem("Projects", "Projects"),
                BreadcrumbItem("Product launch", "Projects/Product launch")
            )
            for (isDarkTheme in listOf(false, true)) {
                for (width in listOf(320, 402, 840, 1440)) {
                    for (depth in 1..3) {
                        val breadcrumbs = path.take(depth)
                        val isHome = depth == 1
                        val isDesktop = width >= 840
                        var backClicks = 0
                        val folderClicks = mutableListOf<String>()
                        ImageComposeScene(width, 874) {
                            EchoListTheme(isDarkTheme = isDarkTheme, themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                                BrowserScreen(
                                    browserUiState = BrowserUiState(breadcrumbs),
                                    canNavigateBack = !isHome,
                                    onBack = { backClicks++ },
                                    onFolderClick = { folderClicks.add(it) }
                                )
                            }
                        }.use { scene ->
                            scene.render().use { image ->
                                File("build/browser-navigation-$width-$depth-${if (isDarkTheme) "dark" else "light"}.png")
                                    .writeBytes(requireNotNull(image.encodeToData()).bytes)
                            }
                            val nodes = scene.browserNodes()
                            val heading = requireNotNull(nodes.tagged("browser-heading"))
                            heading.config[SemanticsProperties.Text].single().text shouldBe breadcrumbs.last().label
                            (nodes.tagged("browser-home-icon") != null) shouldBe isHome
                            (heading.config.getOrNull(SemanticsActions.OnClick) == null) shouldBe true
                            val hint = nodes.tagged("browser-breadcrumbs")
                            (hint != null) shouldBe (!isDesktop && !isHome)
                            if (hint != null) {
                                hint.config[SemanticsProperties.Text].single().text shouldBe
                                    breadcrumbs.dropLast(1).joinToString("") { it.label + " › " }
                                (hint.boundsInRoot.bottom <= heading.boundsInRoot.top) shouldBe true
                                hint.hasClickActionInSubtree() shouldBe false
                            }
                            if (isDesktop) {
                                val location = requireNotNull(nodes.tagged("browser-location"))
                                location.hasClickActionInSubtree() shouldBe !isHome
                                breadcrumbs.forEachIndexed { index, crumb ->
                                    val item = requireNotNull(nodes.tagged("browser-breadcrumb-${crumb.parentDir}"))
                                    val isCurrent = index == breadcrumbs.lastIndex
                                    item.config[SemanticsProperties.Selected] shouldBe isCurrent
                                    if (isCurrent) {
                                        item.hasClickActionInSubtree() shouldBe false
                                    } else {
                                        item.config[SemanticsActions.OnClick].action!!.invoke()
                                    }
                                }
                                folderClicks shouldBe breadcrumbs.dropLast(1).map { it.parentDir }
                            }
                            val back = nodes.tagged("browser-back")
                            (back != null) shouldBe !isHome
                            if (back != null) {
                                (back.boundsInRoot.top > heading.boundsInRoot.bottom) shouldBe true
                                (back.boundsInRoot.right <= width) shouldBe true
                                (back.boundsInRoot.bottom <= 874) shouldBe true
                                back.config[SemanticsActions.OnClick].action!!.invoke()
                                backClicks shouldBe 1
                            }
                        }
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

private fun SemanticsNode.hasClickActionInSubtree(): Boolean =
    config.getOrNull(SemanticsActions.OnClick) != null || children.any { it.hasClickActionInSubtree() }
