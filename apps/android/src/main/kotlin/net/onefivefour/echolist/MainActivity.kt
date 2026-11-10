package net.onefivefour.echolist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.onefivefour.echolist.core.notifications.data.PermissionResultBridge
import net.onefivefour.echolist.core.notifications.data.TaskListNotificationIntent
import net.onefivefour.echolist.core.notifications.domain.TaskListNotificationTarget
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val permissionBridge: PermissionResultBridge by inject()

    private var showRationaleDialog by mutableStateOf(false)
    private var showSettingsDialog by mutableStateOf(false)
    private var awaitingSettingsReturn by mutableStateOf(false)
    private var notificationTarget by mutableStateOf<TaskListNotificationTarget?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionBridge.deliverResult(true)
        } else {
            // Check if permanently denied (shouldShowRationale is false after denial)
            val permanentlyDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )

            if (permanentlyDenied) {
                showSettingsDialog = true
            } else {
                permissionBridge.deliverResult(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTarget = if (savedInstanceState == null) {
            TaskListNotificationIntent.target(intent)
        } else {
            val accountId = savedInstanceState.getString(STATE_NOTIFICATION_ACCOUNT)
            val taskListId = savedInstanceState.getString(STATE_NOTIFICATION_TASK_LIST)
            if (accountId != null && taskListId != null) TaskListNotificationTarget(accountId, taskListId) else null
        }

        setContent {
            val systemBarStyle = if (isSystemInDarkTheme()) {
                SystemBarStyle.dark(scrim = Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT
                )
            }

            enableEdgeToEdge(
                statusBarStyle = systemBarStyle,
                navigationBarStyle = systemBarStyle
            )

            // Observe permission bridge requests
            LaunchedEffect(Unit) {
                permissionBridge.requests.collect {
                    handlePermissionRequest()
                }
            }

            App(
                notificationTarget = notificationTarget,
                onNotificationTargetConsumed = { notificationTarget = null }
            )

            EchoListTheme {
                // Rationale dialog — explains why notifications are needed
                if (showRationaleDialog) {
                    NotificationRationaleDialog(
                        onConfirm = {
                            showRationaleDialog = false
                            launchPermissionRequest()
                        },
                        onDismiss = {
                            showRationaleDialog = false
                            permissionBridge.deliverResult(false)
                        }
                    )
                }

                // Settings redirect dialog — user permanently denied permission
                if (showSettingsDialog) {
                    NotificationSettingsDialog(
                        onOpenSettings = {
                            showSettingsDialog = false
                            awaitingSettingsReturn = true
                            openAppNotificationSettings()
                        },
                        onDismiss = {
                            showSettingsDialog = false
                            permissionBridge.deliverResult(false)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationTarget = TaskListNotificationIntent.target(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        notificationTarget?.let {
            outState.putString(STATE_NOTIFICATION_ACCOUNT, it.accountId)
            outState.putString(STATE_NOTIFICATION_TASK_LIST, it.taskListId)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        // After returning from app notification settings, check if permission was granted
        if (awaitingSettingsReturn) {
            awaitingSettingsReturn = false
            val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            permissionBridge.deliverResult(granted)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionBridge.cancel()
    }

    private fun handlePermissionRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionBridge.deliverResult(true)
            return
        }

        // Already granted (race condition check)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionBridge.deliverResult(true)
            return
        }

        // Should we show rationale?
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (shouldShowRationale) {
            showRationaleDialog = true
        } else {
            launchPermissionRequest()
        }
    }

    private fun launchPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionBridge.deliverResult(true)
        }
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    private companion object {
        const val STATE_NOTIFICATION_ACCOUNT = "notification_account"
        const val STATE_NOTIFICATION_TASK_LIST = "notification_task_list"
    }
}

@Composable
private fun NotificationRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EchoListTheme.materialColors.surface,
        title = {
            Text(
                text = "Notification Permission",
                style = EchoListTheme.typography.titleSmall
            )
        },
        text = {
            Text(
                text = "EchoList needs notification permission to remind you " +
                    "about upcoming tasks. Without it, task reminders will be silently disabled.",
                style = EchoListTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Allow",
                    color = EchoListTheme.materialColors.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Not now",
                    color = EchoListTheme.materialColors.onSurface
                )
            }
        }
    )
}

@Composable
private fun NotificationSettingsDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EchoListTheme.materialColors.surface,
        title = {
            Text(
                text = "Notifications Blocked",
                style = EchoListTheme.typography.titleSmall
            )
        },
        text = {
            Text(
                text = "Notification permission was permanently denied. To " +
                    "enable task reminders, please allow notifications in the app settings.",
                style = EchoListTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = "Open Settings",
                    color = EchoListTheme.materialColors.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = EchoListTheme.materialColors.onSurface
                )
            }
        }
    )
}

@Preview
@Composable
private fun AppAndroidPreview() {
    App()
}
