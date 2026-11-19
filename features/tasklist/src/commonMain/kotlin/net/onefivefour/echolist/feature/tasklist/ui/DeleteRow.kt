package net.onefivefour.echolist.feature.tasklist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun DeleteRow(
    uiState: EditTaskListUiState,
    onToggleAutoDelete: (Boolean) -> Unit,
    onDeleteTaskList: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        TaskListDeleteButton(uiState = uiState, onDeleteTaskList = onDeleteTaskList)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Auto Delete",
            style = EchoListTheme.typography.labelLarge,
            color = EchoListTheme.materialColors.onSurface
        )

        Spacer(modifier = Modifier.width(EchoListTheme.dimensions.s))

        Switch(
            checked = uiState.isAutoDelete,
            onCheckedChange = onToggleAutoDelete,
            enabled = !uiState.isLoading && !uiState.isSaving
        )
    }
}