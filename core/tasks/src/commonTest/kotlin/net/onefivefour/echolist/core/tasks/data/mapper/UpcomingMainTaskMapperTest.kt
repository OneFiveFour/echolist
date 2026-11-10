package net.onefivefour.echolist.core.tasks.data.mapper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.networking.data.error.NetworkException
import net.onefivefour.echolist.core.tasks.domain.model.UpcomingMainTask
import tasks.v1.ListUpcomingMainTasksResponse
import tasks.v1.UpcomingMainTask as ProtoUpcomingMainTask

class UpcomingMainTaskMapperTest : FunSpec({
    test("maps summaries without changing titles or server order") {
        val response = ListUpcomingMainTasksResponse(
            tasks = listOf(
                ProtoUpcomingMainTask(
                    tasklist_id = "list-1",
                    task_id = "second",
                    title = "  Einkaufen 🛒  ",
                    due_date = "2028-02-29"
                ),
                ProtoUpcomingMainTask(tasklist_id = "list-1", task_id = "first", title = "", due_date = "2026-01-01")
            )
        )

        TaskListMapper.toDomain(response) shouldBe listOf(
            UpcomingMainTask("second", "  Einkaufen 🛒  ", LocalDate(2028, 2, 29), "list-1"),
            UpcomingMainTask("first", "", LocalDate(2026, 1, 1), "list-1")
        )
    }

    test("preserves recurring, one-time and missing recurrence flags from the wire response") {
        listOf(true, false, null).forEach { isRecurring ->
            val proto = ProtoUpcomingMainTask(
                task_id = "task",
                title = "Task",
                tasklist_id = "list-1",
                due_date = "2028-02-29",
                is_recurring = isRecurring
            )
            val decoded = ProtoUpcomingMainTask.ADAPTER.decode(ProtoUpcomingMainTask.ADAPTER.encode(proto))

            TaskListMapper.toDomain(decoded).isRecurring shouldBe isRecurring
        }
    }

    test("maps an empty response to an empty list") {
        TaskListMapper.toDomain(ListUpcomingMainTasksResponse()) shouldBe emptyList()
    }

    test("accepts four-digit calendar year boundaries") {
        listOf("0000-01-01", "9999-12-31").forEach { date ->
            val task = TaskListMapper.toDomain(ProtoUpcomingMainTask(tasklist_id = "list-1", due_date = date))
            task.dueDate shouldBe LocalDate.parse(date)
        }
    }

    test("rejects missing dates, invalid calendar dates and non-contract formats") {
        listOf(
            "", "2026-02-29", "2026-04-31", "2026-13-01", "2026-00-01", "2026-01-00",
            "2026-1-01", "2026-01-1", " 2026-01-01", "2026-01-01 ",
            "2026-01-01T00:00:00Z", "+10000-01-01", "-0001-01-01"
        ).forEach { date ->
            val error = shouldThrow<NetworkException.SerializationError> {
                TaskListMapper.toDomain(ProtoUpcomingMainTask(tasklist_id = "list-1", due_date = date))
            }
            error.message shouldBe "UpcomingMainTask.due_date is not a valid YYYY-MM-DD date"
            (error.cause is IllegalArgumentException) shouldBe true
        }
    }

    test("rejects the entire response when one entry is invalid") {
        shouldThrow<NetworkException.SerializationError> {
            TaskListMapper.toDomain(
                ListUpcomingMainTasksResponse(
                    tasks = listOf(
                        ProtoUpcomingMainTask(tasklist_id = "list-1", task_id = "valid", due_date = "2026-10-20"),
                        ProtoUpcomingMainTask(tasklist_id = "list-1", task_id = "invalid", due_date = "")
                    )
                )
            )
        }
    }
})