package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.core.notifications.domain.TaskListNotificationTarget

class NotificationNavigationTest : FunSpec({
    val target = TaskListNotificationTarget("account", "task-list")

    test("cold start opens the notified list with a browser back destination") {
        val stack = mutableListOf<NavKey>(BrowserRoute())
        stack.openNotificationTaskList(target, "account")
        stack.first() shouldBe BrowserRoute()
        stack.size shouldBe 2
        (stack.last() as EditTaskListRoute).taskListId shouldBe "task-list"
    }

    test("warm navigation preserves the existing stack and opens the notified list") {
        val initial = listOf(BrowserRoute("folder"), EditNoteRoute("folder", "note"))
        val stack = initial.toMutableList<NavKey>()
        stack.openNotificationTaskList(target, "account")
        stack.dropLast(1) shouldBe initial
        (stack.last() as EditTaskListRoute).taskListId shouldBe "task-list"
    }

    test("an existing editor is reused with its identity and settings channel") {
        val editor = EditTaskListRoute("folder", "task-list")
        val stack = mutableListOf<NavKey>(BrowserRoute(), editor, MainTaskSettingsRoute("task"))
        stack.openNotificationTaskList(target, "account")
        stack shouldBe listOf(BrowserRoute(), editor)
        stack.openNotificationTaskList(target, "account")
        stack shouldBe listOf(BrowserRoute(), editor)
    }

    test("notifications from another account or without a session cannot open a list") {
        listOf(null, "other-account").forEach { account ->
            val stack = mutableListOf<NavKey>(BrowserRoute())
            stack.openNotificationTaskList(target, account)
            stack shouldBe listOf(BrowserRoute())
        }
    }

    test("blank list IDs never enter creation mode") {
        val stack = mutableListOf<NavKey>(BrowserRoute())
        stack.openNotificationTaskList(target.copy(taskListId = " "), "account")
        stack shouldBe listOf(BrowserRoute())
    }
})
