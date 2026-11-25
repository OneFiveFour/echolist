package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.json.Json

class RouteSerializationPropertyTest : FunSpec({

    val json = Json {
        serializersModule = navKeySerializersModule
    }

    test("TasksSummaryRoute preserves the selected folder through polymorphic serialization") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { parentDir ->
            val route: NavKey = TasksSummaryRoute(parentDir)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded) shouldBe route
        }
    }

    test("Property 1: BrowserRoute serialization round-trip") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { parentDir ->
            val route = BrowserRoute(parentDir)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<BrowserRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<BrowserRoute>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for BrowserRoute") {
        checkAll(PropTestConfig(iterations = 25), Arb.string(0..200)) { parentDir ->
            val route: NavKey = BrowserRoute(parentDir)
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: EditNoteRoute serialization round-trip") {
        checkAll(
            PropTestConfig(iterations = 25),
            Arb.int(0..50),
            Arb.boolean()
        ) { index, withNoteId ->
            val parentDir = if (index == 0) "" else "folder-$index"
            val route = EditNoteRoute(
                parentDir = parentDir,
                noteId = if (withNoteId) "note-id-$index" else null
            )
            val encoded = json.encodeToString(kotlinx.serialization.serializer<EditNoteRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditNoteRoute>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditNoteRoute") {
        checkAll(
            PropTestConfig(iterations = 25),
            Arb.int(0..50),
            Arb.boolean()
        ) { index, withNoteId ->
            val parentDir = if (index == 0) "" else "folder-$index"
            val route: NavKey = EditNoteRoute(
                parentDir = parentDir,
                noteId = if (withNoteId) "note-id-$index" else null
            )
            val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
            decoded shouldBe route
        }
    }

    test("Property 1: EditTaskListRoute serialization round-trip") {
        val route = EditTaskListRoute(
            parentDir = "folder-1",
            taskListId = "task-list-1"
        )
        val encoded = json.encodeToString(kotlinx.serialization.serializer<EditTaskListRoute>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<EditTaskListRoute>(), encoded)
        decoded shouldBe route
    }

    test("Property 1: Polymorphic NavKey serialization round-trip for EditTaskListRoute") {
        val route: NavKey = EditTaskListRoute(
            parentDir = "folder-1",
            taskListId = "task-list-1"
        )
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
        decoded shouldBe route
    }

    test("MainTaskSettingsRoute serialization preserves draft settings") {
        val route: NavKey = MainTaskSettingsRoute(
            mainTaskId = "draft-task-1",
            editorId = "editor",
            currentDueDate = "2026-08-15",
            currentRecurrence = "FREQ=WEEKLY;INTERVAL=2"
        )
        val encoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
        val decoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), encoded)
        decoded shouldBe route
    }

    test("Feature: notification-permission-toggle, Property 6: Route-Parameter Round-Trip") {
        /**
         * Validates: Requirements 2.4
         *
         * For all Boolean values v, when a MainTaskSettingsRoute is created with
         * currentIsNotificationEnabled = v and serialized/deserialized, the resulting
         * currentIsNotificationEnabled value must equal v.
         */
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..50),
            Arb.string(0..20),
            Arb.string(0..50),
            Arb.boolean()
        ) { mainTaskId, dueDate, recurrence, isNotificationEnabled ->
            val route = MainTaskSettingsRoute(
                mainTaskId = mainTaskId,
                editorId = "editor",
                currentDueDate = dueDate,
                currentRecurrence = recurrence,
                currentIsNotificationEnabled = isNotificationEnabled
            )

            // Direct serialization round-trip
            val encoded = json.encodeToString(kotlinx.serialization.serializer<MainTaskSettingsRoute>(), route)
            val decoded = json.decodeFromString(kotlinx.serialization.serializer<MainTaskSettingsRoute>(), encoded)
            decoded shouldBe route
            decoded.currentIsNotificationEnabled shouldBe isNotificationEnabled

            // Polymorphic NavKey serialization round-trip
            val polyEncoded = json.encodeToString(kotlinx.serialization.serializer<NavKey>(), route)
            val polyDecoded = json.decodeFromString(kotlinx.serialization.serializer<NavKey>(), polyEncoded)
            polyDecoded shouldBe route
            (polyDecoded as MainTaskSettingsRoute).currentIsNotificationEnabled shouldBe isNotificationEnabled
        }
    }
})