package net.onefivefour.echolist.feature.tasksettings.ui.recurrence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.components.EchoListBackground
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/**
 * Returns the formatted monthly display string for the given month interval and day of month.
 */
internal fun monthlyFormatString(n: Int, m: Int): String = "Every $n month(s) on the ${m}th day"

@Composable
internal fun MonthlyDetailContent(
    everyNMonths: Int?,
    dayOfMonth: Int?,
    onMonthIntervalChanged: (Int?) -> Unit,
    onDayOfMonthChanged: (Int?) -> Unit,
    isMonthIntervalError: Boolean,
    isDayOfMonthError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelText(text = "Repeat every")

            NumberInputField(
                value = everyNMonths,
                onValueChange = onMonthIntervalChanged,
                isError = isMonthIntervalError
            )

            LabelText(text = if (everyNMonths == 1) "month" else "months")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelText(text = "On day")

            NumberInputField(
                value = dayOfMonth,
                onValueChange = onDayOfMonthChanged,
                isError = isDayOfMonthError
            )

            LabelText(text = "of the month")
        }
    }
}

@Preview
@Composable
private fun MonthlyDetailContentPreview() {
    EchoListTheme {
        EchoListBackground {
            MonthlyDetailContent(
                everyNMonths = 1,
                dayOfMonth = 15,
                onMonthIntervalChanged = {},
                onDayOfMonthChanged = {},
                isMonthIntervalError = false,
                isDayOfMonthError = false
            )
        }
    }
}

@Preview
@Composable
private fun MonthlyDetailContentMultiMonthPreview() {
    EchoListTheme {
        EchoListBackground {
            MonthlyDetailContent(
                everyNMonths = 3,
                dayOfMonth = 1,
                onMonthIntervalChanged = {},
                onDayOfMonthChanged = {},
                isMonthIntervalError = false,
                isDayOfMonthError = false
            )
        }
    }
}