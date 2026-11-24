package net.onefivefour.echolist.feature.browser.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType

class BrowserSortingTest : FunSpec({
    fun folder(title: String) = FileEntry(title, title, ItemType.FOLDER, FileMetadata.Folder(0))
    fun note(title: String, date: Long) = FileEntry(title, title, ItemType.NOTE, FileMetadata.Note(title, date, ""))
    fun tasks(
        title: String,
        date: Long
    ) = FileEntry(title, title, ItemType.TASK_LIST, FileMetadata.TaskList(title, date, 0, 0))

    test("folders precede files alphabetically and files sort by modification date across types") {
        val entries =
            listOf(
                note("New note", 30),
                folder("zebra"),
                tasks("Older list", 10),
                folder("Alpha"),
                note("Middle note", 20)
            )
        sortBrowserEntries(entries).map { it.title } shouldBe
            listOf("Alpha", "zebra", "Older list", "Middle note", "New note")
    }
    test("missing timestamps use epoch and equal timestamps preserve server order") {
        val entries = listOf(note("first", 0), tasks("second", 0), note("third", 1))
        sortBrowserEntries(entries) shouldBe entries
        FileEntry("unknown", "unknown", ItemType.NOTE, null).lastModified shouldBe 0L
    }
})