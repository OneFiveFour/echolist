package net.onefivefour.echolist

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun NotificationRationaleDialog(
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