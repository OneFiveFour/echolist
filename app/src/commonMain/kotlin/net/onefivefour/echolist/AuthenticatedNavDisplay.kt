package net.onefivefour.echolist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import net.onefivefour.echolist.feature.browser.ui.BrowserFeature
import net.onefivefour.echolist.feature.note.ui.NoteFeature
import net.onefivefour.echolist.feature.reminderssummary.ui.RemindersSummaryFeature
import net.onefivefour.echolist.feature.tasklist.ui.TaskListFeature
import net.onefivefour.echolist.feature.tasksettings.ui.TaskSettingsFeature
import net.onefivefour.echolist.feature.taskssummary.ui.TasksSummaryFeature
import net.onefivefour.echolist.ui.navigation.BrowserRoute
import net.onefivefour.echolist.ui.navigation.EditNoteRoute
import net.onefivefour.echolist.ui.navigation.EditTaskListRoute
import net.onefivefour.echolist.ui.navigation.MainTaskSettingsRoute
import net.onefivefour.echolist.ui.navigation.RemindersSummaryRoute
import net.onefivefour.echolist.ui.navigation.TaskSettingsChannels
import net.onefivefour.echolist.ui.navigation.TasksSummaryRoute
import net.onefivefour.echolist.ui.navigation.navigateToBrowserFolder

@Composable
internal fun AuthenticatedNavDisplay(
    backStack: NavBackStack<NavKey>,
    settingsChannels: TaskSettingsChannels,
    isBrowserNavigationBlocked: Boolean,
    onBrowserNavigationStateChange: (Boolean, () -> Unit) -> Unit,
    onLeaveBrowser: () -> Unit,
    onLogout: () -> Unit
) {
    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        onBack = {
            if (!isBrowserNavigationBlocked) {
                onLeaveBrowser()
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<TasksSummaryRoute> { route ->
                TasksSummaryFeature(
                    parentDir = route.parentDir,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<BrowserRoute> { browserRoute ->
                BrowserFeature(
                    selectedFolder = browserRoute.parentDir,
                    onNavigationStateChange = { isBlocked, discardDraft ->
                        if (backStack.lastOrNull() == browserRoute) {
                            onBrowserNavigationStateChange(isBlocked, discardDraft)
                        }
                    },
                    navToFolder = { backStack.navigateToBrowserFolder(it) },
                    navToNote = { backStack.add(EditNoteRoute(browserRoute.parentDir, it)) },
                    navToTaskList = {
                        backStack.add(
                            EditTaskListRoute(
                                browserRoute.parentDir,
                                it
                            )
                        )
                    },
                    navToRemindersSummary = { backStack.add(RemindersSummaryRoute) },
                    navToTasksSummary = { backStack.add(TasksSummaryRoute(browserRoute.parentDir)) },
                    onLogout = onLogout,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<RemindersSummaryRoute> {
                RemindersSummaryFeature()
            }

            entry<EditNoteRoute> { route ->
                NoteFeature(
                    parentDir = route.parentDir,
                    noteId = route.noteId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<EditTaskListRoute> { route ->
                TaskListFeature(
                    parentDir = route.parentDir,
                    taskListId = route.taskListId,
                    settingsResults = settingsChannels.resultsFor(route.editorId),
                    navToMainTaskSettings = { taskListId, initial ->
                        backStack.add(
                            MainTaskSettingsRoute(
                                mainTaskId = initial.mainTaskId,
                                currentDueDate = initial.dueDate,
                                currentRecurrence = initial.recurrence,
                                currentIsNotificationEnabled = initial.isNotificationEnabled,
                                editorId = route.editorId,
                                taskListId = taskListId
                            )
                        )
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<MainTaskSettingsRoute> { route ->
                TaskSettingsFeature(
                    initial = TaskSettingsChanges(
                        route.mainTaskId,
                        route.currentDueDate,
                        route.currentRecurrence,
                        route.currentIsNotificationEnabled
                    ),
                    onResult = settingsChannels.sinkFor(
                        route.editorId.ifBlank {
                            backStack.filterIsInstance<EditTaskListRoute>()
                                .lastOrNull()?.editorId.orEmpty()
                        }
                    )
                )
            }
        }
    )
}