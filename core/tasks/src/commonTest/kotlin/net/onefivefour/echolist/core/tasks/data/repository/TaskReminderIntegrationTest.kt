package net.onefivefour.echolist.core.tasks.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationPermissionChecker
import net.onefivefour.echolist.core.notifications.domain.NotificationScheduler
import net.onefivefour.echolist.core.notifications.domain.Reminder
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.ReminderPlatform
import net.onefivefour.echolist.core.notifications.domain.ReminderState
import net.onefivefour.echolist.core.notifications.domain.ReminderStore
import net.onefivefour.echolist.core.tasks.data.source.network.FakeTaskListRemoteDataSource
import net.onefivefour.echolist.core.tasks.domain.completeRecurringTaskFromNotification
import net.onefivefour.echolist.core.tasks.domain.previewTaskNotification
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.UpdateTaskListParams
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import tasks.v1.GetTaskListResponse
import tasks.v1.UpdateTaskListResponse

class TaskReminderIntegrationTest : FunSpec({
    test("preview resolves both one-time and recurring dated tasks before any reminder sync") {
        listOf("", "FREQ=DAILY").forEach { recurrence ->
            val fixture = TaskReminderFixture()
            val task = tasks.v1.MainTask(
                id = "task", description = "Preview me", due_date = "2095-01-01", recurrence = recurrence
            )
            fixture.source.getTaskListResult = Result.success(
                GetTaskListResponse(task_list = tasks.v1.TaskList(id = "list", tasks = listOf(task)))
            )
            fixture.coordinator.refreshSession()
            fixture.state.reminders shouldBe emptyList()

            previewTaskNotification(fixture.repository, fixture.coordinator, "list", "task") shouldBe true

            fixture.shown.single().let {
                it.body shouldBe "Preview me"
                it.taskId shouldBe "task"
                it.taskListId shouldBe "list"
                it.dueDate shouldBe "2095-01-01"
            }
            fixture.coordinator.isCurrentOccurrence("account", "task", "2095-01-01") shouldBe true
            fixture.state.reminders.single().deliveredDates shouldBe emptySet()
            fixture.source.lastUpcomingRequest shouldBe null
            fixture.coordinator.restore()
            fixture.scheduled.values.single().dueDate shouldBe "2095-01-01"
        }
    }

    test("preview rejects undated completed and invalid-date tasks regardless of recurrence") {
        listOf("", "FREQ=DAILY").forEach { recurrence ->
            listOf("" to false, "invalid" to false, "2095-01-01" to true).forEach { (date, done) ->
                val fixture = TaskReminderFixture()
                fixture.source.getTaskListResult = Result.success(
                    GetTaskListResponse(
                        task_list = tasks.v1.TaskList(
                            id = "list",
                            tasks = listOf(tasks.v1.MainTask(
                                id = "task", due_date = date, is_done = done, recurrence = recurrence
                            ))
                        )
                    )
                )
                fixture.coordinator.refreshSession()
                previewTaskNotification(fixture.repository, fixture.coordinator, "list", "task") shouldBe false
                fixture.shown shouldBe emptyList()
                fixture.state.reminders shouldBe emptyList()
            }
        }
    }

    test("one-time preview Done completes the real task even without a prior sync record") {
        val fixture = TaskReminderFixture()
        fixture.coordinator.refreshSession()
        previewTaskNotification(fixture.repository, fixture.coordinator, "list", "task") shouldBe true
        fixture.coordinator.isCurrentOccurrence("account", "task", "2095-01-01") shouldBe true
        completeRecurringTaskFromNotification(
            fixture.repository, fixture.scheduler, "list", "task", expectedDueDate = "2095-01-01"
        ).getOrThrow()
        fixture.source.lastUpdateRequest!!.tasks.single().is_done shouldBe true
    }

    test("Done completes an overdue one-time task without delivering its reminder again") {
        val fixture = TaskReminderFixture()
        val task = tasks.v1.MainTask(id = "task", description = "Task", due_date = "2020-01-01")
        val list = tasks.v1.TaskList(id = "list", title = "List", tasks = listOf(task))
        val completedList = list.copy(tasks = listOf(task.copy(is_done = true)))
        fixture.source.getTaskListResult = Result.success(GetTaskListResponse(task_list = list))
        fixture.source.updateTaskListResult = Result.success(UpdateTaskListResponse(task_list = completedList))
        fixture.coordinator.refreshSession()
        fixture.coordinator.schedule("task", "list", "Title", "Task", task.due_date)
        fixture.shown.size shouldBe 1

        completeRecurringTaskFromNotification(
            fixture.repository, fixture.scheduler, "list", "task", expectedDueDate = task.due_date
        ).getOrThrow()

        fixture.source.lastUpdateRequest!!.tasks.single().is_done shouldBe true
        fixture.state.reminders.single().active shouldBe false
        fixture.coordinator.restore()
        fixture.shown.size shouldBe 1
    }

    test("persistent device preferences override server defaults and stale editor snapshots") {
        val fixture = TaskReminderFixture()
        fixture.coordinator.refreshSession()
        fixture.coordinator.setEnabled("task", false)
        fixture.repository.getTaskList("list").getOrThrow().tasks.single().isNotificationEnabled shouldBe false
        val staleTask = MainTask("task", "Task", false, "2095-01-01", "", subTasks = emptyList())
        fixture.repository.updateTaskList(UpdateTaskListParams("list", "List", listOf(staleTask), false))
            .getOrThrow().tasks.single().isNotificationEnabled shouldBe false
        fixture.scheduled.size shouldBe 0
    }

    test("finishing a one-time task and deleting its list clear durable pending reminders") {
        val fixture = TaskReminderFixture()
        fixture.coordinator.refreshSession()
        val task = MainTask("task", "Task", false, "2095-01-01", "", subTasks = emptyList())
        fixture.repository.updateTaskList(UpdateTaskListParams("list", "List", listOf(task), false)).getOrThrow()
        fixture.scheduled.size shouldBe 1
        fixture.source.updateTaskListResult = Result.success(
            UpdateTaskListResponse(
                task_list = tasks.v1.TaskList(
                    id = "list", title = "List",
                    tasks = listOf(tasks.v1.MainTask(id = "task", due_date = "2095-01-01", is_done = true))
                )
            )
        )
        fixture.repository.updateTaskList(UpdateTaskListParams("list", "List", listOf(task.copy(isDone = true)), false))
            .getOrThrow()
        fixture.scheduled.size shouldBe 0
        fixture.state.reminders.single().active shouldBe false
        fixture.repository.deleteTaskList("list").getOrThrow()
        fixture.scheduled.size shouldBe 0
    }

    test("retrying an older Done action never completes the next recurring occurrence") {
        val fixture = TaskReminderFixture()
        completeRecurringTaskFromNotification(
            fixture.repository, fixture.scheduler, "list", "task",
            expectedDueDate = "2094-01-01"
        ).isSuccess shouldBe true
        fixture.source.lastUpdateRequest shouldBe null
    }

    test("a Done retry for an already completed one-time task does not send another update") {
        val fixture = TaskReminderFixture()
        fixture.source.getTaskListResult = Result.success(
            GetTaskListResponse(
                task_list = tasks.v1.TaskList(
                    id = "list", title = "List",
                    tasks = listOf(tasks.v1.MainTask(id = "task", due_date = "2095-01-01", is_done = true))
                )
            )
        )
        completeRecurringTaskFromNotification(
            fixture.repository, fixture.scheduler, "list", "task",
            expectedDueDate = "2095-01-01"
        ).isSuccess shouldBe true
        fixture.source.lastUpdateRequest shouldBe null
    }
})

private class TaskReminderFixture {
    var state = ReminderState()
    val scheduled = mutableMapOf<String, Reminder>()
    val shown = mutableListOf<Reminder>()
    val coordinator = NotificationCoordinator(
        object : ReminderStore {
            override suspend fun read() = state
            override suspend fun write(state: ReminderState) {
                this@TaskReminderFixture.state = state
            }
        },
        object : ReminderPlatform {
            override suspend fun schedule(reminder: Reminder) {
                scheduled[reminder.key] = reminder
            }
            override suspend fun cancelPending(reminder: Reminder) {
                scheduled.remove(reminder.key)
            }
            override suspend fun dismiss(reminder: Reminder) = Unit
            override suspend fun show(reminder: Reminder): Boolean {
                shown.add(reminder)
                return true
            }
        },
        object : NotificationPermissionChecker {
            override suspend fun isGranted() = true
        },
        NotificationAccountProvider { "account" },
        object : ReminderBackgroundScheduler {
            override fun start() = Unit
            override fun requestSync() = Unit
            override fun stop() = Unit
        }
    )
    val source = FakeTaskListRemoteDataSource().apply {
        val list = tasks.v1.TaskList(
            id = "list",
            title = "List",
            tasks = listOf(tasks.v1.MainTask(id = "task", description = "Task", due_date = "2095-01-01"))
        )
        getTaskListResult = Result.success(GetTaskListResponse(task_list = list))
        updateTaskListResult = Result.success(UpdateTaskListResponse(task_list = list))
    }
    val repository = TaskListRepositoryImpl(source, Dispatchers.Unconfined, FakeDirectoryChangeNotifier(), coordinator)
    val scheduler = object : NotificationScheduler {
        override suspend fun schedule(
            taskId: String,
            taskListId: String,
            title: String,
            body: String,
            dueDateIso: String
        ) =
            coordinator.schedule(taskId, taskListId, title, body, dueDateIso)
        override suspend fun cancel(taskId: String) = coordinator.cancel(taskId)
        override suspend fun setEnabled(taskId: String, enabled: Boolean) = coordinator.setEnabled(taskId, enabled)
    }
}
