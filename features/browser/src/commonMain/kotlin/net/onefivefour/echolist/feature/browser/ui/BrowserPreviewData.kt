package net.onefivefour.echolist.feature.browser.ui

import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.FileMetadata
import net.onefivefour.echolist.feature.browser.domain.model.ItemType

internal val browserPreviewState = BrowserUiState(
    breadcrumbs = listOf(
        BreadcrumbItem("Home", ""),
        BreadcrumbItem("Projects", "Projects"),
        BreadcrumbItem("EchoList", "Projects/EchoList")
    ),
    canRenameCurrentFolder = true,
    canDeleteCurrentFolder = true,
    fileEntries = listOf(
        FileEntry(
            path = "Projects/Design",
            title = "Design",
            itemType = ItemType.FOLDER,
            metadata = FileMetadata.Folder(3)
        ),
        FileEntry(
            path = "notes",
            title = "Meeting notes",
            itemType = ItemType.NOTE,
            metadata = FileMetadata.Note(
                id = "note-1",
                updatedAt = 0L,
                preview = "Ideas for the next release"
            )
        ),
        FileEntry(
            path = "tasks",
            title = "Release checklist",
            itemType = ItemType.TASK_LIST,
            metadata = FileMetadata.TaskList(
                id = "tasks-1",
                updatedAt = 0L,
                totalTaskCount = 8,
                doneTaskCount = 3
            )
        )
    )
)
