package net.onefivefour.echolist

import net.onefivefour.echolist.feature.tasklist.ui.TaskListFeature

import net.onefivefour.echolist.feature.tasksettings.ui.TaskSettingsFeature
import net.onefivefour.echolist.feature.tasknotifications.ui.TaskNotificationsFeature
import net.onefivefour.echolist.ui.navigation.TaskNotificationsRoute
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

import net.onefivefour.echolist.feature.note.ui.NoteFeature

import net.onefivefour.echolist.feature.browser.ui.BrowserFeature

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.notifications.domain.TaskListNotificationTarget
import net.onefivefour.echolist.ui.navigation.openNotificationTaskList
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import org.koin.compose.koinInject
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import net.onefivefour.echolist.app.resources.Res
import net.onefivefour.echolist.app.resources.ic_arrow_back
import net.onefivefour.echolist.app.resources.navigate_back
import net.onefivefour.echolist.ui.AuthState
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.core.designsystem.ui.components.GradientBackground
import net.onefivefour.echolist.core.designsystem.ui.components.RoundIconButton
import net.onefivefour.echolist.feature.login.ui.LoginFeature
import net.onefivefour.echolist.ui.navigation.EditNoteRoute
import net.onefivefour.echolist.ui.navigation.EditTaskListRoute
import net.onefivefour.echolist.ui.navigation.TaskSettingsChannels
import net.onefivefour.echolist.ui.navigation.BrowserRoute
import net.onefivefour.echolist.ui.navigation.MainTaskSettingsRoute
import net.onefivefour.echolist.ui.navigation.echoListSavedStateConfig
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun App(
    notificationTarget: TaskListNotificationTarget? = null,
    onNotificationTargetConsumed: () -> Unit = {},
    onDebugReminder: ((String, String) -> Unit)? = null
) {
    EchoListTheme {
        GradientBackground {
            val authViewModel = koinViewModel<AuthViewModel>()
            val authState by authViewModel.authState.collectAsStateWithLifecycle()
            val settingsChannels = koinViewModel<TaskSettingsChannels>()
            val notifications = koinInject<NotificationCoordinator>()
            val reminderWork = koinInject<ReminderBackgroundScheduler>()
            val reminderScope = rememberCoroutineScope()
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                reminderScope.launch {
                    notifications.refreshSession()
                    notifications.restore()
                    if (authState == AuthState.Authenticated) reminderWork.requestSync()
                }
            }
            LaunchedEffect(authState) {
                if (authState == AuthState.Unauthenticated) settingsChannels.clearEditors()
                notifications.refreshSession()
                notifications.restore()
                if (authState == AuthState.Authenticated) reminderWork.requestSync()
            }

            when (authState) {
                AuthState.Loading -> Unit
                AuthState.Unauthenticated -> UnauthenticatedApp(authViewModel)
                AuthState.Authenticated -> AuthenticatedApp(
                    notificationTarget, onNotificationTargetConsumed, onDebugReminder
                )
            }
        }
    }
}

@Composable
private fun UnauthenticatedApp(authViewModel: AuthViewModel) {
    LoginFeature(onAuthenticated = authViewModel::onAuthenticated)
}

@Composable
private fun AuthenticatedApp(
    notificationTarget: TaskListNotificationTarget?,
    onNotificationTargetConsumed: () -> Unit,
    onDebugReminder: ((String, String) -> Unit)?
) {
    val backStack = rememberNavBackStack(echoListSavedStateConfig, BrowserRoute())
    val account = koinInject<NotificationAccountProvider>()
    LaunchedEffect(notificationTarget) {
        if (notificationTarget != null) {
            backStack.openNotificationTaskList(notificationTarget, account.currentAccountId())
            onNotificationTargetConsumed()
        }
    }
    val canNavigateBack = backStack.size > 1
    val settingsChannels = koinViewModel<TaskSettingsChannels>()
    val editorIds = backStack.filterIsInstance<EditTaskListRoute>().map { it.editorId }.toSet()
    LaunchedEffect(editorIds) { settingsChannels.retainEditors(editorIds) }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = canNavigateBack,
        onBackCompleted = { backStack.removeLastOrNull() }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AuthenticatedNavDisplay(
            backStack = backStack,
            canNavigateBack = canNavigateBack,
            settingsChannels = settingsChannels,
            onDebugReminder = onDebugReminder
        )

        if (canNavigateBack) {
            RoundIconButton(
                iconRes = Res.drawable.ic_arrow_back,
                onClick = { backStack.removeLastOrNull() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = EchoListTheme.dimensions.l,
                        bottom = EchoListTheme.dimensions.m
                    ),
                containerColor = EchoListTheme.materialColors.primary,
                contentColor = EchoListTheme.materialColors.onPrimary,
                contentDescription = stringResource(Res.string.navigate_back)
            )
        }
    }
}

@Composable
private fun AuthenticatedNavDisplay(
    backStack: NavBackStack<NavKey>,
    canNavigateBack: Boolean,
    settingsChannels: TaskSettingsChannels,
    onDebugReminder: ((String, String) -> Unit)?
) {
    NavDisplay(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = EchoListTheme.dimensions.l,
                top = EchoListTheme.dimensions.m,
                end = EchoListTheme.dimensions.l,
                bottom = if (canNavigateBack) {
                    EchoListTheme.dimensions.xxxl + EchoListTheme.dimensions.xl
                } else {
                    EchoListTheme.dimensions.m
                }
            ),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryProvider = entryProvider {
            entry<BrowserRoute> { route ->
                BrowserFeature(
                    parentDir = route.parentDir,
                    onBreadcrumbClick = { parentDir ->
                        val index = backStack.indexOfLast { it is BrowserRoute && it.parentDir == parentDir }
                        if (index >= 0) {
                            while (backStack.size > index + 1) backStack.removeLast()
                        } else {
                            backStack.add(BrowserRoute(parentDir))
                        }
                    },
                    onReplaceFolder = { parentDir ->
                        val index = backStack.indexOfLast { it is BrowserRoute && it.parentDir == parentDir }
                        if (index >= 0) {
                            while (backStack.size > index + 1) backStack.removeLast()
                        } else {
                            backStack.removeLastOrNull()
                            backStack.add(BrowserRoute(parentDir))
                        }
                    },
                    onOpenFolder = { backStack.add(BrowserRoute(it)) },
                    onOpenNote = { backStack.add(EditNoteRoute(route.parentDir, it)) },
                    onOpenTaskList = { backStack.add(EditTaskListRoute(route.parentDir, it)) },
                    onOpenTaskNotifications = { backStack.add(TaskNotificationsRoute) }
                )
            }

            entry<TaskNotificationsRoute> {
                TaskNotificationsFeature()
            }

            entry<EditNoteRoute> { route ->
                NoteFeature(route.parentDir, route.noteId, onNavigateBack = { backStack.removeLastOrNull() })
            }

            entry<EditTaskListRoute> { route ->
                TaskListFeature(
                    parentDir = route.parentDir,
                    taskListId = route.taskListId,
                    settingsResults = settingsChannels.resultsFor(route.editorId),
                    onOpenSettings = { taskListId, initial ->
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
                    onNavigateBack = { backStack.removeLastOrNull() }
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
                    onDebugReminder = onDebugReminder?.let { trigger ->
                        val editor = backStack.filterIsInstance<EditTaskListRoute>()
                            .lastOrNull { it.editorId == route.editorId }
                        (route.taskListId ?: editor?.taskListId)?.let { listId ->
                            { trigger(listId, route.mainTaskId) }
                        }
                    },
                    onResult = settingsChannels.sinkFor(
                        route.editorId.ifBlank {
                            backStack.filterIsInstance<EditTaskListRoute>().lastOrNull()?.editorId.orEmpty()
                        }
                    )
                )
            }
        }
    )
}
