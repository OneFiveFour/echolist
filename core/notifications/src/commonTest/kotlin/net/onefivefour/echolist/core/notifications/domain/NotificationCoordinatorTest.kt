package net.onefivefour.echolist.core.notifications.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class NotificationCoordinatorTest : FunSpec({
    test("preview delivers future reminders repeatedly without changing history or alarms") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-23"))) }
        val state = fixture.store.state
        val scheduled = fixture.platform.scheduled.toMap()
        fixture.coordinator.preview("task") shouldBe true
        fixture.coordinator.preview("task") shouldBe true
        fixture.platform.shown shouldBe listOf(state.reminders.single(), state.reminders.single())
        fixture.store.state shouldBe state
        fixture.platform.scheduled shouldBe scheduled
        fixture.instant = Instant.parse("2026-10-23T12:00:00Z")
        fixture.coordinator.restore()
        fixture.platform.shown.size shouldBe 3
        fixture.store.state.reminders.single().deliveredDates shouldBe setOf("2026-10-23")
    }

    test("preview can redisplay an already delivered occurrence with its original action metadata") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-20"))) }
        val state = fixture.store.state
        fixture.coordinator.preview("task") shouldBe true
        fixture.platform.shown.last().let {
            it.accountId shouldBe "account"
            it.taskListId shouldBe "list"
            it.taskId shouldBe "task"
            it.dueDate shouldBe "2026-10-20"
        }
        fixture.store.state shouldBe state
    }

    test("preview respects permission and disabled reminders") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-23"))) }
        fixture.permission = false
        fixture.coordinator.preview("task") shouldBe false
        fixture.permission = true
        fixture.coordinator.setEnabled("task", false)
        fixture.coordinator.preview("task") shouldBe false
        fixture.platform.shown.size shouldBe 0
    }

    test("preview rejects missing tasks inactive reminders and mismatched sessions") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-23"))) }
        fixture.coordinator.preview("unsaved-task") shouldBe false
        fixture.account = "other-account"
        fixture.coordinator.preview("task") shouldBe false
        fixture.account = null
        fixture.coordinator.preview("task") shouldBe false
        fixture.account = "account"
        fixture.coordinator.cancel("task")
        fixture.coordinator.preview("task") shouldBe false
        fixture.platform.shown.size shouldBe 0
    }

    test("preview waits for authoritative task mutations and reports platform delivery failure") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-23"))) }
        fixture.coordinator.beginMutation()
        fixture.coordinator.preview("task") shouldBe false
        fixture.coordinator.endMutation()
        fixture.platform.canShow = false
        val state = fixture.store.state
        fixture.coordinator.preview("task") shouldBe false
        fixture.store.state shouldBe state
        fixture.platform.shown.size shouldBe 0
    }

    test("future reminders are replaced by task ID and delivered once per due date across process restarts") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-21"))) }.isSuccess shouldBe true
        fixture.platform.scheduled.values.single().dueDate shouldBe "2026-10-21"
        fixture.instant = Instant.parse("2026-10-21T00:00:00Z")
        fixture.coordinator.restore()
        fixture.coordinator.restore()
        fixture.newCoordinator().restore()
        fixture.platform.shown.size shouldBe 1
        fixture.platform.scheduled.size shouldBe 0
    }

    test("new occurrences of a recurring task can notify after an earlier occurrence was delivered") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-20"))) }
        fixture.coordinator.replaceTaskList("list", listOf(fixture.task("2026-10-21")))
        fixture.instant = Instant.parse("2026-10-21T01:00:00Z")
        fixture.coordinator.restore()
        fixture.platform.shown.map { it.dueDate } shouldBe listOf("2026-10-20", "2026-10-21")
    }

    test("a successful empty snapshot cancels missing future tasks") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-23"))) }
        fixture.coordinator.synchronize { Result.success(emptyList()) }.isSuccess shouldBe true
        fixture.platform.scheduled.size shouldBe 0
        fixture.store.state.reminders.single().active shouldBe false
    }

    test("offline synchronization retains schedules and delivers missed known reminders") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-21"))) }
        fixture.instant = Instant.parse("2026-10-23T12:00:00Z")
        fixture.coordinator.synchronize { Result.failure(Exception("offline")) }.isFailure shouldBe true
        fixture.platform.shown.single().dueDate shouldBe "2026-10-21"
        fixture.store.state.syncDates["account"] shouldBe "2026-10-20"
        fixture.coordinator.restore()
        fixture.platform.shown.size shouldBe 1
    }

    test("disabled preferences survive restarts and stay independent of server responses") {
        val fixture = ReminderFixture()
        fixture.coordinator.refreshSession()
        fixture.coordinator.setEnabled("task", false)
        val restarted = fixture.newCoordinator()
        restarted.synchronize { Result.success(listOf(fixture.task("2026-10-20"))) }
        restarted.isEnabled("task") shouldBe false
        fixture.platform.shown.size shouldBe 0
        restarted.setEnabled("task", true)
        restarted.restore()
        fixture.platform.shown.size shouldBe 1
    }

    test("denied permission keeps an occurrence pending until permission is restored") {
        val fixture = ReminderFixture()
        fixture.permission = false
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-20"))) }
        fixture.platform.shown.size shouldBe 0
        fixture.store.state.reminders.single().deliveredDates shouldBe emptySet()
        fixture.permission = true
        fixture.coordinator.restore()
        fixture.platform.shown.size shouldBe 1
    }

    test("failed platform delivery stays pending") {
        val fixture = ReminderFixture()
        fixture.platform.canShow = false
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-20"))) }
        fixture.store.state.reminders.single().deliveredDates shouldBe emptySet()
        fixture.platform.canShow = true
        fixture.coordinator.restore()
        fixture.store.state.reminders.single().deliveredDates shouldBe setOf("2026-10-20")
    }

    test("snapshot fetch includes the local day west of UTC without converting due dates") {
        val fixture = ReminderFixture()
        fixture.instant = Instant.parse("2026-10-20T00:30:00Z")
        fixture.zone = TimeZone.of("America/Los_Angeles")
        fixture.coordinator.synchronize { from ->
            from shouldBe LocalDate(0, 1, 1)
            Result.success(listOf(fixture.task("2026-10-20")))
        }.isSuccess shouldBe true
        fixture.platform.shown.size shouldBe 0
        fixture.zone = TimeZone.of("Europe/Berlin")
        fixture.coordinator.restore()
        fixture.platform.shown.single().dueDate shouldBe "2026-10-20"
    }

    test("missed days remain within the full open-task snapshot") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-21"))) }
        fixture.instant = Instant.parse("2026-10-25T00:00:00Z")
        fixture.coordinator.synchronize { from ->
            from shouldBe LocalDate(0, 1, 1)
            Result.success(listOf(fixture.task("2026-10-21")))
        }
        fixture.platform.shown.single().dueDate shouldBe "2026-10-21"
    }

    test("backdated tasks created elsewhere are caught even before our last successful sync date") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(emptyList()) }
        fixture.coordinator.synchronize { from ->
            (from <= LocalDate(2025, 1, 1)) shouldBe true
            Result.success(listOf(fixture.task("2025-01-01")))
        }.isSuccess shouldBe true
        fixture.platform.shown.single().dueDate shouldBe "2025-01-01"
    }

    test("local preference changes invalidate an in-flight server snapshot") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize {
            fixture.coordinator.setEnabled("task", false)
            Result.success(listOf(fixture.task("2026-10-20")))
        }.isFailure shouldBe true
        fixture.platform.shown.size shouldBe 0
        fixture.coordinator.isEnabled("task") shouldBe false
    }

    test("a task-list mutation prevents older background snapshots from being applied") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize {
            fixture.coordinator.beginMutation()
            fixture.coordinator.replaceTaskList("list", listOf(fixture.task("2026-10-25")))
            fixture.coordinator.endMutation()
            Result.success(listOf(fixture.task("2026-10-20")))
        }.isFailure shouldBe true
        fixture.platform.scheduled.values.single().dueDate shouldBe "2026-10-25"
        fixture.platform.shown.size shouldBe 0
    }

    test("logout cancels old alarms and prevents delayed broadcasts from displaying") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-21"))) }
        fixture.account = null
        fixture.coordinator.refreshSession()
        fixture.platform.scheduled.size shouldBe 0
        fixture.instant = Instant.parse("2026-10-21T12:00:00Z")
        fixture.coordinator.deliver("account", "task", "2026-10-21")
        fixture.platform.shown.size shouldBe 0
        fixture.background.stopped shouldBe true
    }

    test("account changes reject old responses and isolate preferences for identical task IDs") {
        val fixture = ReminderFixture()
        fixture.coordinator.refreshSession()
        fixture.coordinator.setEnabled("task", false)
        fixture.coordinator.synchronize {
            fixture.account = "other"
            fixture.coordinator.refreshSession()
            Result.success(listOf(fixture.task("2026-10-20")))
        }.isFailure shouldBe true
        fixture.coordinator.isEnabled("task") shouldBe true
        fixture.platform.shown.size shouldBe 0
    }

    test("stale alarm occurrence is ignored after rescheduling") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-21"))) }
        fixture.coordinator.replaceTaskList("list", listOf(fixture.task("2026-10-23")))
        fixture.instant = Instant.parse("2026-10-22T00:00:00Z")
        fixture.coordinator.deliver("account", "task", "2026-10-21")
        fixture.platform.shown.size shouldBe 0
    }

    test("incomplete or duplicate snapshots do not cancel known alarms") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-21"))) }
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-22").copy(taskListId = ""))) }
            .isFailure shouldBe true
        val duplicate = fixture.task("2026-10-22")
        fixture.coordinator.synchronize { Result.success(listOf(duplicate, duplicate)) }
            .isFailure shouldBe true
        fixture.platform.scheduled.values.single().dueDate shouldBe "2026-10-21"
    }

    test("visible delivered notifications survive absence from an upcoming snapshot") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-20"))) }
        fixture.platform.dismissed.clear()
        fixture.coordinator.synchronize { Result.success(emptyList()) }
        fixture.platform.dismissed.size shouldBe 0
    }

    test("full identities distinguish Java hash collisions and delimiter-containing IDs") {
        "FB".hashCode() shouldBe "Ea".hashCode()
        (reminderKey("account", "FB") == reminderKey("account", "Ea")) shouldBe false
        (reminderKey("a", "bc") == reminderKey("ab", "c")) shouldBe false
    }

    test("cancellation is propagated") {
        val fixture = ReminderFixture()
        shouldThrow<CancellationException> {
            fixture.coordinator.synchronize { throw CancellationException("cancel") }
        }
    }

    test("late editor callbacks cannot restore an earlier due date or cancel an eligible task") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-23"))) }
        fixture.coordinator.schedule("task", "list", "Old title", "Old body", "2026-10-20")
        fixture.coordinator.cancelWhenIneligible("task")
        fixture.platform.scheduled.values.single().dueDate shouldBe "2026-10-23"
        fixture.platform.shown.size shouldBe 0
    }

    test("late scheduling cannot resurrect a task removed by an authoritative snapshot") {
        val fixture = ReminderFixture()
        fixture.coordinator.synchronize { Result.success(listOf(fixture.task("2026-10-23"))) }
        fixture.coordinator.synchronize { Result.success(emptyList()) }
        fixture.coordinator.schedule("task", "list", "Old title", "Old body", "2026-10-23")
        fixture.store.state.reminders.single().active shouldBe false
        fixture.platform.scheduled.size shouldBe 0
    }
})

private class MemoryReminderStore : ReminderStore {
    var state = ReminderState()
    override suspend fun read() = state
    override suspend fun write(state: ReminderState) {
        this.state = state
    }
}

private class RecordingReminderPlatform : ReminderPlatform {
    val scheduled = mutableMapOf<String, Reminder>()
    val shown = mutableListOf<Reminder>()
    val dismissed = mutableListOf<Reminder>()
    var canShow = true
    override suspend fun schedule(reminder: Reminder) {
        scheduled[reminder.key] = reminder
    }
    override suspend fun cancelPending(reminder: Reminder) {
        scheduled.remove(reminder.key)
    }
    override suspend fun dismiss(reminder: Reminder) {
        dismissed.add(reminder)
    }
    override suspend fun show(reminder: Reminder): Boolean {
        if (canShow) shown.add(reminder)
        return canShow
    }
}

private class FakeReminderBackground : ReminderBackgroundScheduler {
    var stopped = false
    override fun start() {
        stopped = false
    }
    override fun requestSync() = Unit
    override fun stop() {
        stopped = true
    }
}

private class ReminderFixture {
    val store = MemoryReminderStore()
    val platform = RecordingReminderPlatform()
    val background = FakeReminderBackground()
    var instant = Instant.parse("2026-10-20T12:00:00Z")
    var zone: TimeZone = TimeZone.UTC
    var account: String? = "account"
    var permission = true
    val coordinator = newCoordinator()
    fun newCoordinator() = NotificationCoordinator(
        store,
        platform,
        object : NotificationPermissionChecker {
            override suspend fun isGranted() = permission
        },
        NotificationAccountProvider { account },
        background,
        object : Clock {
            override fun now() = instant
        },
        { zone }
    )
    fun task(date: String) = UpcomingReminder("task", "list", "Task", LocalDate.parse(date))
}
