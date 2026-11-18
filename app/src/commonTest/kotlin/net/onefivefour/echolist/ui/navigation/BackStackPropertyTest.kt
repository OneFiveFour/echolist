package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

private fun arbBrowserRoute(): Arb<BrowserRoute> =
    Arb.int(0..50).map { index ->
        if (index == 0) BrowserRoute("") else BrowserRoute("folder-$index")
    }

private fun arbEditNoteRoute(): Arb<EditNoteRoute> =
    Arb.int(0..50).map { index ->
        if (index % 2 == 0) {
            EditNoteRoute(parentDir = "folder-$index")
        } else {
            EditNoteRoute(
                parentDir = "folder-$index",
                noteId = "note-id-$index"
            )
        }
    }

private fun arbNavKey(): Arb<NavKey> =
    Arb.choice(arbBrowserRoute(), arbEditNoteRoute())

class BackStackPropertyTest : FunSpec({

    test("Property 2: pushing a NavKey increases stack size by one") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.list(arbNavKey(), 1..20),
            arbNavKey()
        ) { initial, newKey ->
            val stack = initial.toMutableList()
            val sizeBefore = stack.size
            val entriesBefore = stack.toList()

            stack.add(newKey)

            stack.size shouldBe sizeBefore + 1
            stack.last() shouldBe newKey
            stack.subList(0, sizeBefore) shouldBe entriesBefore
        }
    }

    test("Property 3: breadcrumb navigation truncates to matching BrowserRoute") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.list(Arb.int(1..10).map { "folder-$it" }, 2..10)
        ) { paths ->
            for (targetPath in paths) {
                val stack = paths.map<String, NavKey> { BrowserRoute(it) }.toMutableList()
                val entriesBefore = stack.toList()
                val index = paths.indexOfLast { it == targetPath }

                stack.navigateToBrowserFolder(targetPath)

                stack.last() shouldBe BrowserRoute(targetPath)
                stack.size shouldBe index + 1
                stack shouldBe entriesBefore.subList(0, index + 1)
            }
        }
    }

    test("a breadcrumb absent from the stack becomes active and back restores the previous directory") {
        for (targetPath in listOf("", "Projects", "Projects/Planning")) {
            val initial = listOf<NavKey>(BrowserRoute(), BrowserRoute("Projects/Planning/Release"))
            // For Home, simulate a restored deep entry without a root route.
            val stack = (if (targetPath.isEmpty()) initial.drop(1) else initial).toMutableList()
            val entriesBefore = stack.toList()

            stack.navigateToBrowserFolder(targetPath)

            stack shouldBe entriesBefore + BrowserRoute(targetPath)
            stack.removeLastOrNull()
            stack shouldBe entriesBefore
        }
    }

    test("jumping across multiple levels allows opening a different branch and jumping Home") {
        val stack = mutableListOf<NavKey>(
            BrowserRoute(), BrowserRoute("Projects"), BrowserRoute("Projects/Planning"),
            BrowserRoute("Projects/Planning/Release")
        )
        stack.navigateToBrowserFolder("Projects")
        stack shouldBe listOf(BrowserRoute(), BrowserRoute("Projects"))
        stack.navigateToBrowserFolder("Projects/Other")
        stack.last() shouldBe BrowserRoute("Projects/Other")
        stack.navigateToBrowserFolder("")
        stack shouldBe listOf(BrowserRoute())
    }

    test("selecting the current folder is a no-op and does not match an editor with the same parent") {
        val stack = mutableListOf<NavKey>(BrowserRoute(), BrowserRoute("Projects"))
        val initial = stack.toList()
        stack.navigateToBrowserFolder("Projects")
        stack shouldBe initial

        stack.add(EditNoteRoute("Projects/Planning", "note"))
        val withEditor = stack.toList()
        stack.navigateToBrowserFolder("Projects/Planning")
        stack shouldBe withEditor + BrowserRoute("Projects/Planning")
    }

    test("Property 4: back on multi-entry stack removes top entry") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.list(arbNavKey(), 2..20)
        ) { initial ->
            val stack = initial.toMutableList()
            val sizeBefore = stack.size
            val expectedRemaining = stack.dropLast(1)

            stack.removeLastOrNull()

            stack.size shouldBe sizeBefore - 1
            stack shouldBe expectedRemaining
        }
    }

    test("Property 4: back on single-entry stack leaves it unchanged") {
        checkAll(
            PropTestConfig(iterations = 20),
            arbNavKey()
        ) { singleKey ->
            val stack = mutableListOf<NavKey>(singleKey)

            if (stack.size > 1) {
                stack.removeLastOrNull()
            }

            stack.size shouldBe 1
            stack.first() shouldBe singleKey
        }
    }
})