@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package net.onefivefour.echolist.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object TaskNotificationsRoute : NavKey

@Serializable
data object LoginRoute : NavKey

@Serializable
@kotlinx.serialization.SerialName("net.onefivefour.echolist.ui.navigation.HomeRoute")
data class BrowserRoute(val parentDir: String = "") : NavKey

@Serializable
data class EditNoteRoute(
    val parentDir: String,
    val noteId: String? = null
) : NavKey

@Serializable
data class EditTaskListRoute(
    val parentDir: String,
    val taskListId: String? = null,
    val editorId: String = kotlin.uuid.Uuid.random().toString()
) : NavKey

@Serializable
data class MainTaskSettingsRoute(
    val mainTaskId: String,
    val currentDueDate: String = "",
    val currentRecurrence: String = "",
    val currentIsNotificationEnabled: Boolean = true,
    val editorId: String = ""
) : NavKey

val navKeySerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(TaskNotificationsRoute::class, TaskNotificationsRoute.serializer())
        subclass(LoginRoute::class, LoginRoute.serializer())
        subclass(BrowserRoute::class, BrowserRoute.serializer())
        subclass(EditNoteRoute::class, EditNoteRoute.serializer())
        subclass(EditTaskListRoute::class, EditTaskListRoute.serializer())
        subclass(MainTaskSettingsRoute::class, MainTaskSettingsRoute.serializer())
    }
}

val echoListSavedStateConfig = SavedStateConfiguration {
    serializersModule = navKeySerializersModule
}