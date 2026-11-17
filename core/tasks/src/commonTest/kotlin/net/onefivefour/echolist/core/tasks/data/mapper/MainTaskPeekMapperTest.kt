package net.onefivefour.echolist.core.tasks.data.mapper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.tasks.domain.model.MainTasksSummary
import tasks.v1.ListMainTasksSummaryResponse
import tasks.v1.MainTasksSummary as ProtoMainTasksSummary

class MainTasksSummaryMapperTest : FunSpec({
    test("maps peek entries preserving server order and grouping") {
        val response = ListMainTasksSummaryResponse(
            tasks = listOf(
                ProtoMainTasksSummary(
                    main_task_id = "task-2",
                    description = "  Milch kaufen 🥛  ",
                    is_done = false,
                    due_date = "2026-01-02",
                    task_list_id = "list-1",
                    task_list_name = "Einkauf"
                ),
                ProtoMainTasksSummary(
                    main_task_id = "task-1",
                    description = "Erledigt",
                    is_done = true,
                    due_date = "",
                    task_list_id = "list-1",
                    task_list_name = "Einkauf"
                )
            )
        )

        TaskListMapper.toDomain(response) shouldBe listOf(
            MainTasksSummary("task-2", "  Milch kaufen 🥛  ", false, LocalDate(2026, 1, 2), "list-1", "Einkauf"),
            MainTasksSummary("task-1", "Erledigt", true, null, "list-1", "Einkauf")
        )
    }

    test("maps an empty due date to null") {
        TaskListMapper.toDomain(ProtoMainTasksSummary(main_task_id = "t", due_date = "")).dueDate shouldBe null
    }

    test("maps an empty response to an empty list") {
        TaskListMapper.toDomain(ListMainTasksSummaryResponse()) shouldBe emptyList()
    }

    test("rejects non-empty invalid due dates") {
        listOf("2026-02-29", "2026-13-01", "2026-1-01", "2026-01-01T00:00:00Z", " 2026-01-01").forEach { date ->
            val error = shouldThrow<NetworkException.SerializationError> {
                TaskListMapper.toDomain(ProtoMainTasksSummary(main_task_id = "t", due_date = date))
            }
            error.message shouldBe "MainTasksSummary.due_date is not a valid YYYY-MM-DD date"
            (error.cause is IllegalArgumentException) shouldBe true
        }
    }
})