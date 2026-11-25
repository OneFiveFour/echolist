package net.onefivefour.echolist.feature.tasklist.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.core.deeplinks.domain.DeepLink
import net.onefivefour.echolist.core.notifications.domain.ReminderDeepLink
import net.onefivefour.echolist.core.tasks.domain.model.OpenTaskListDestination

class TaskListDeepLinkTargetTest : FunSpec({
    val target = TaskListDeepLinkTarget()

    test("reminder links open the task list of their account") {
        val link = DeepLink(ReminderDeepLink.HOST, listOf("account", "list", "task"))

        target.resolve(link) shouldBe OpenTaskListDestination(accountId = "account", taskListId = "list")
    }

    test("other hosts, wrong shapes and blank segments are rejected") {
        target.resolve(DeepLink("other", listOf("account", "list", "task"))) shouldBe null
        target.resolve(DeepLink(ReminderDeepLink.HOST, listOf("account", "list"))) shouldBe null
        target.resolve(DeepLink(ReminderDeepLink.HOST, listOf("account", " ", "task"))) shouldBe null
    }
})
