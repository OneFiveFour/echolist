package net.onefivefour.echolist.feature.tasksettings.ui.recurrence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/**
 * Returns the formatted weekly display string for the given week count.
 */
internal fun weeklyFormatString(n: Int): String = "Every $n week(s)"

@Composable
internal fun WeeklyDetailContent(
    everyNWeeks: Int?,
    onWeekCountChanged: (Int?) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelText(text = "Repeat every")

        NumberInputField(
            value = everyNWeeks,
            onValueChange = onWeekCountChanged,
            isError = isError
        )

        LabelText(text = if (everyNWeeks == 1) "week" else "weeks")
    }
}

@Preview
@Composable
private fun WeeklyDetailContentPreview() {
    EchoListTheme {
        WeeklyDetailContent(
            everyNWeeks = 1,
            onWeekCountChanged = {},
            isError = false
        )
    }
}

@Preview
@Composable
private fun WeeklyDetailContentMultiWeekPreview() {
    EchoListTheme {
        WeeklyDetailContent(
            everyNWeeks = 3,
            onWeekCountChanged = {},
            isError = false
        )
    }
}