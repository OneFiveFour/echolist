package net.onefivefour.echolist.feature.tasksettings.ui.recurrence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun RecurrenceIntervalPicker(
    selectedInterval: RecurrenceInterval,
    onIntervalSelected: (RecurrenceInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    val intervals = remember { RecurrenceInterval.entries }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
    ) {
        intervals.forEach { interval ->
            val isSelected = interval == selectedInterval

            Surface(
                onClick = { onIntervalSelected(interval) },
                shape = EchoListTheme.shapes.small,
                color = if (isSelected) {
                    EchoListTheme.materialColors.primary
                } else {
                    EchoListTheme.materialColors.surfaceVariant
                }
            ) {
                Text(
                    text = interval.fullLabel,
                    style = EchoListTheme.typography.labelMedium,
                    color = if (isSelected) {
                        EchoListTheme.materialColors.onPrimary
                    } else {
                        EchoListTheme.materialColors.onSurfaceVariant
                    },
                    modifier = Modifier.padding(
                        horizontal = EchoListTheme.dimensions.l,
                        vertical = EchoListTheme.dimensions.s
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun RecurrenceIntervalPickerPreview() {
    EchoListTheme {
        RecurrenceIntervalPicker(
            selectedInterval = RecurrenceInterval.Off,
            onIntervalSelected = {},
            modifier = Modifier.padding(EchoListTheme.dimensions.l)
        )
    }
}

@Preview
@Composable
private fun RecurrenceIntervalPickerWeeklyPreview() {
    EchoListTheme {
        RecurrenceIntervalPicker(
            selectedInterval = RecurrenceInterval.Weekly,
            onIntervalSelected = {},
            modifier = Modifier.padding(EchoListTheme.dimensions.l)
        )
    }
}