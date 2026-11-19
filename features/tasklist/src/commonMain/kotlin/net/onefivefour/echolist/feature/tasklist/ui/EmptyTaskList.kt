package net.onefivefour.echolist.feature.tasklist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun EmptyTaskList(onAddMainTask: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
        ) {
            Text(
                text = "No main tasks yet.",
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.onSurface
            )
            TextButton(onClick = onAddMainTask) {
                Text(
                    text = "Add the first task",
                    style = EchoListTheme.typography.labelLarge
                )
            }
        }
    }
}