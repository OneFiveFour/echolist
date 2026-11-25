package net.onefivefour.echolist.ui.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

class MainTaskSettingsResultsTest : FunSpec({
    test("two screens of the same task receive only their own settings") {
        runTest {
            val results = MainTaskSettingsResults()
            val first = results.receiveFor("screen-a")
            val second = results.receiveFor("screen-b")
            val changeA = TaskSettingsChanges("same-task", "2026-10-04", "", false)
            val changeB = changeA.copy(dueDate = "2026-10-05")
            results.senderTo("screen-a").emit(changeA)
            results.senderTo("screen-b").emit(changeB)
            first.first() shouldBe changeA
            second.first() shouldBe changeB
            results.discardAll()
        }
    }

    test("settings are queued until the owning screen starts collecting") {
        runTest {
            val results = MainTaskSettingsResults()
            val change = TaskSettingsChanges("draft", "2026-10-04", "FREQ=DAILY;INTERVAL=1")
            results.senderTo("screen").emit(change)
            results.discardExcept(setOf("screen"))
            results.receiveFor("screen").first() shouldBe change
            results.discardAll()
        }
    }

    test("a late permission result cannot reach a closed or newly opened screen") {
        runTest {
            val results = MainTaskSettingsResults()
            val oldResults = results.receiveFor("screen")
            val oldSink = results.senderTo("screen")
            results.discardAll()
            oldSink.emit(TaskSettingsChanges("task", "", "", false))
            oldResults.toList() shouldBe emptyList()

            val newResults = results.receiveFor("screen")
            val current = TaskSettingsChanges("task", "2026-10-06", "", true)
            results.senderTo("screen").emit(current)
            newResults.first() shouldBe current
            results.discardAll()
        }
    }
})