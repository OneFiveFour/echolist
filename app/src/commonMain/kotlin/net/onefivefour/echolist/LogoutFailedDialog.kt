package net.onefivefour.echolist

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import net.onefivefour.echolist.app.resources.Res
import net.onefivefour.echolist.app.resources.logout_cancel
import net.onefivefour.echolist.app.resources.logout_failed_message
import net.onefivefour.echolist.app.resources.logout_failed_title
import net.onefivefour.echolist.app.resources.logout_retry
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogoutFailedDialog(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = EchoListTheme.materialColors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(Res.string.logout_failed_title),
                style = EchoListTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.logout_failed_message),
                style = EchoListTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(Res.string.logout_retry),
                    style = EchoListTheme.typography.labelLarge,
                    color = colors.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.logout_cancel),
                    style = EchoListTheme.typography.labelLarge,
                    color = colors.primary
                )
            }
        }
    )
}