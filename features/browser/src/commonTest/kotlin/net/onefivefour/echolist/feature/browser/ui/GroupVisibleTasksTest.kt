package net.onefivefour.echolist.feature.browser.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.tasks.domain.model.MainTasksSummary

class GroupVisibleTasksTest : FunSpec({
    fun peek(
        id: String,
        isDone: Boolean = false,
        dueDate: LocalDate? = null,
        listId: String,
        listName: String
    ) = MainTasksSummary(id, "desc-$id", isDone, dueDate, listId, listName)

    test("groups by task list preserving server order and dropping done tasks") {
        val result = groupVisibleTasks(
            listOf(
                peek("a", listId = "l1", listName = "List 1"),
                peek("b", isDone = true, listId = "l1", listName = "List 1"),
                peek("c", dueDate = LocalDate(2026, 1, 1), listId = "l2", listName = "List 2"),
                peek("d", listId = "l1", listName = "List 1")
            )
        )

        result shouldBe listOf(
            TasksSummaryGroup(
                "l1", "List 1",
                listOf(
                    TasksSummaryRow("a", "desc-a", false, null),
                    TasksSummaryRow("d", "desc-d", false, null)
                )
            ),
            TasksSummaryGroup(
                "l2", "List 2",
                listOf(TasksSummaryRow("c", "desc-c", false, LocalDate(2026, 1, 1)))
            )
        )
    }

    test("drops task lists that have no visible tasks after hiding done") {
        val result = groupVisibleTasks(
            listOf(
                peek("a", isDone = true, listId = "l1", listName = "Empty"),
                peek("b", listId = "l2", listName = "Visible")
            )
        )

        result shouldBe listOf(
            TasksSummaryGroup("l2", "Visible", listOf(TasksSummaryRow("b", "desc-b", false, null)))
        )
    }

    test("returns an empty list when all tasks are done") {
        groupVisibleTasks(
            listOf(peek("a", isDone = true, listId = "l1", listName = "List 1"))
        ) shouldBe emptyList()
    }
})