package net.onefivefour.echolist

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun NotificationSettingsDialog(
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