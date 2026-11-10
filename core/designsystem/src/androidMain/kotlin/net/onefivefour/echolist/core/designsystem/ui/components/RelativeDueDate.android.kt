package net.onefivefour.echolist.core.designsystem.ui.components

import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit
import android.icu.text.RelativeDateTimeFormatter.Direction
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit
import java.util.Locale

internal actual fun formatRelativeDueDate(value: RelativeDueDate, localeTag: String): String {
    val formatter = RelativeDateTimeFormatter.getInstance(Locale.forLanguageTag(localeTag))
    // These overloads work on API 24; the signed-offset overload requires API 28.
    if (value.useNamedDay) {
        val direction = when (value.signedAmount) {
            -1 -> Direction.LAST
            0 -> Direction.THIS
            else -> Direction.NEXT
        }
        return formatter.format(direction, AbsoluteUnit.DAY)
    }
    val unit = when (value.unit) {
        DueDateUnit.Day -> RelativeUnit.DAYS
        DueDateUnit.Week -> RelativeUnit.WEEKS
        DueDateUnit.Month -> RelativeUnit.MONTHS
        DueDateUnit.Year -> RelativeUnit.YEARS
    }
    return formatter.format(value.amount.toDouble(), if (value.isPast) Direction.LAST else Direction.NEXT, unit)
}
