package net.onefivefour.echolist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
import kotlinx.coroutines.launch
import net.onefivefour.echolist.app.resources.Res
import net.onefivefour.echolist.app.resources.ic_arrow_back
import net.onefivefour.echolist.app.resources.navigate_back
import net.onefivefour.echolist.core.designsystem.ui.components.RoundIconButton
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.core.notifications.domain.TaskListNotificationTarget
import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges
import net.onefivefour.echolist.feature.browser.ui.BrowserFeature
import net.onefivefour.echolist.feature.login.ui.LoginFeature
import net.onefivefour.echolist.feature.note.ui.NoteFeature
import net.onefivefour.echolist.feature.reminderssummary.ui.RemindersSummaryFeature
import net.onefivefour.echolist.feature.tasklist.ui.TaskListFeature
import net.onefivefour.echolist.feature.tasksettings.ui.TaskSettingsFeature
import net.onefivefour.echolist.feature.taskssummary.ui.TasksSummaryFeature
import net.onefivefour.echolist.ui.AuthState
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.ui.navigation.BrowserRoute
import net.onefivefour.echolist.ui.navigation.EditNoteRoute
import net.onefivefour.echolist.ui.navigation.EditTaskListRoute
import net.onefivefour.echolist.ui.navigation.MainTaskSettingsRoute
import net.onefivefour.echolist.ui.navigation.RemindersSummaryRoute
import net.onefivefour.echolist.ui.navigation.TaskSettingsChannels
import net.onefivefour.echolist.ui.navigation.TasksSummaryRoute
import net.onefivefour.echolist.ui.navigation.echoListSavedStateConfig
import net.onefivefour.echolist.ui.navigation.navigateToBrowserFolder
import net.onefivefour.echolist.ui.navigation.openNotificationTaskList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    notificationTarget: TaskListNotificationTarget? = null,
    onNotificationTargetConsumed: () -> Unit = {},
    onDebugReminder: ((String, String) -> Unit)? = null,
    overlayContent: @Composable () -> Unit = {}
) {
    EchoListTheme {

        val authViewModel = koinViewModel<AuthViewModel>()
        val settingsChannels = koinViewModel<TaskSettingsChannels>()

        val notifications = koinInject<NotificationCoordinator>()
        val reminderWork = koinInject<ReminderBackgroundScheduler>()

        val reminderScope = rememberCoroutineScope()

        val authState by authViewModel.authState.collectAsStateWithLifecycle()

        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            reminderScope.launch {
                notifications.refreshSession()
                notifications.restore()
                if (authState == AuthState.Authenticated) {
                    reminderWork.requestSync()
                }
            }
        }

        LaunchedEffect(authState) {
            if (authState == AuthState.Unauthenticated) {
                settingsChannels.clearEditors()
            }
            notifications.refreshSession()
            notifications.restore()
            if (authState == AuthState.Authenticated) {
                reminderWork.requestSync()
            }
        }

        when (authState) {
            AuthState.Loading -> Unit
            AuthState.Unauthenticated -> UnauthenticatedApp(authViewModel)
            AuthState.Authenticated -> AuthenticatedApp(
                notificationTarget = notificationTarget,
                onNotificationTargetConsumed = onNotificationTargetConsumed,
                onDebugReminder = onDebugReminder
            )
        }

        overlayContent()
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
    var isBrowserNavigationBlocked by remember { mutableStateOf(false) }
    var discardBrowserDraft by remember { mutableStateOf<() -> Unit>({}) }
    val account = koinInject<NotificationAccountProvider>()
    LaunchedEffect(notificationTarget, isBrowserNavigationBlocked) {
        if (notificationTarget != null && !isBrowserNavigationBlocked) {
            discardBrowserDraft()
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
        onBackCompleted = {
            if (!isBrowserNavigationBlocked) {
                discardBrowserDraft()
                backStack.removeLastOrNull()
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AuthenticatedNavDisplay(
            backStack = backStack,
            canNavigateBack = canNavigateBack,
            settingsChannels = settingsChannels,
            isBrowserNavigationBlocked = isBrowserNavigationBlocked,
            onBrowserNavigationStateChange = { isBlocked, discardDraft ->
                isBrowserNavigationBlocked = isBlocked
                discardBrowserDraft = discardDraft
            },
            onLeaveBrowser = { discardBrowserDraft() },
            onDebugReminder = onDebugReminder
        )

        val isBrowserRoute = backStack.lastOrNull() is BrowserRoute
        if (canNavigateBack && !isBrowserRoute && backStack.lastOrNull() !is TasksSummaryRoute) {
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
    isBrowserNavigationBlocked: Boolean,
    onBrowserNavigationStateChange: (Boolean, () -> Unit) -> Unit,
    onLeaveBrowser: () -> Unit,
    onDebugReminder: ((String, String) -> Unit)?
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
                    canNavigateBack = canNavigateBack,
                    navToFolder = { backStack.navigateToBrowserFolder(it) },
                    navToNote = { backStack.add(EditNoteRoute(browserRoute.parentDir, it)) },
                    navToTaskList = { backStack.add(EditTaskListRoute(browserRoute.parentDir, it)) },
                    navToRemindersSummary = { backStack.add(RemindersSummaryRoute) },
                    navToTasksSummary = { backStack.add(TasksSummaryRoute(browserRoute.parentDir)) },
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
                    onBack = { backStack.removeLastOrNull() })
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
