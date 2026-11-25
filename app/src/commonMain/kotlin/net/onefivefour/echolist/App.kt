package net.onefivefour.echolist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import net.onefivefour.echolist.app.resources.Res
import net.onefivefour.echolist.app.resources.ic_arrow_back
import net.onefivefour.echolist.app.resources.navigate_back
import net.onefivefour.echolist.core.deeplinks.domain.DeepLink
import net.onefivefour.echolist.core.deeplinks.domain.DeepLinkHandler
import net.onefivefour.echolist.core.designsystem.ui.components.RoundIconButton
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider
import net.onefivefour.echolist.core.notifications.domain.NotificationCoordinator
import net.onefivefour.echolist.core.notifications.domain.ReminderBackgroundScheduler
import net.onefivefour.echolist.feature.login.ui.LoginFeature
import net.onefivefour.echolist.ui.AuthState
import net.onefivefour.echolist.ui.AuthViewModel
import net.onefivefour.echolist.ui.LogoutState
import net.onefivefour.echolist.ui.navigation.BrowserRoute
import net.onefivefour.echolist.ui.navigation.TasksSummaryRoute
import net.onefivefour.echolist.ui.navigation.echoListSavedStateConfig
import net.onefivefour.echolist.ui.navigation.open
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    deepLink: DeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {},
    overlayContent: @Composable () -> Unit = {}
) {
    EchoListTheme {
        val authViewModel = koinViewModel<AuthViewModel>()

        val notifications = koinInject<NotificationCoordinator>()
        val reminderWork = koinInject<ReminderBackgroundScheduler>()

        val reminderScope = rememberCoroutineScope()

        val authState by authViewModel.authState.collectAsStateWithLifecycle()
        val logoutState by authViewModel.logoutState.collectAsStateWithLifecycle()

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
                onLogout = authViewModel::logout,
                deepLink = deepLink,
                onDeepLinkConsumed = onDeepLinkConsumed
            )
        }

        if (authState == AuthState.Authenticated && logoutState == LogoutState.Failed) {
            LogoutFailedDialog(
                onRetry = authViewModel::logout,
                onDismiss = authViewModel::dismissLogoutError
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
    onLogout: () -> Unit,
    deepLink: DeepLink?,
    onDeepLinkConsumed: () -> Unit
) {
    val backStack = rememberNavBackStack(echoListSavedStateConfig, BrowserRoute())
    val account = koinInject<NotificationAccountProvider>()

    val deepLinkHandler = koinInject<DeepLinkHandler>()
    LaunchedEffect(deepLink) {
        if (deepLink != null) {
            deepLinkHandler.resolve(deepLink)?.let { backStack.open(it, account.currentAccountId()) }
            onDeepLinkConsumed()
        }
    }
    val canNavigateBack = backStack.size > 1
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = canNavigateBack,
        onBackCompleted = { backStack.removeLastOrNull() }
    )

    AuthenticatedNavDisplay(
        backStack = backStack,
        onLogout = onLogout
    )
}