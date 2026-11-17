package net.onefivefour.echolist.feature.tasksettings.ui.recurrence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.format.DayOfWeekNames
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

private val dayAbbreviations = DayOfWeekNames.ENGLISH_ABBREVIATED.names

@Composable
internal fun DailyDetailContent(
    selectedDays: Set<DayOfWeek>,
    onDayToggled: (DayOfWeek, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = EchoListTheme.dimensions.xs,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DayOfWeek.entries.forEachIndexed { index, day ->
            DayToggle(
                label = dayAbbreviations[index],
                isSelected = day in selectedDays,
                onClick = { onDayToggled(day, day !in selectedDays) }
            )
        }
    }
}

@Preview
@Composable
private fun DailyDetailContentPreview() {
    EchoListTheme {
        DailyDetailContent(
            selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            onDayToggled = { _, _ -> }
        )
    }
}

@Preview
@Composable
private fun DailyDetailContentEmptyPreview() {
    EchoListTheme {
        DailyDetailContent(
            selectedDays = emptySet(),
            onDayToggled = { _, _ -> }
        )
    }
}