package net.onefivefour.echolist.core.designsystem.ui.components

import com.ibm.icu.text.RelativeDateTimeFormatter
import com.ibm.icu.text.RelativeDateTimeFormatter.RelativeDateTimeUnit
import java.util.Locale

internal actual fun formatRelativeDueDate(value: RelativeDueDate, localeTag: String): String {
    val formatter = RelativeDateTimeFormatter.getInstance(Locale.forLanguageTag(localeTag))
    val unit = when (value.unit) {
        DueDateUnit.Day -> RelativeDateTimeUnit.DAY
        DueDateUnit.Week -> RelativeDateTimeUnit.WEEK
        DueDateUnit.Month -> RelativeDateTimeUnit.MONTH
        DueDateUnit.Year -> RelativeDateTimeUnit.YEAR
    }
    return if (value.useNamedDay) {
        formatter.format(value.signedAmount.toDouble(), unit)
    } else {
        formatter.formatNumeric(value.signedAmount.toDouble(), unit)
    }
}
