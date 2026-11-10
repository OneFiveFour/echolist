package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.periodUntil

internal enum class DueDateUnit { Day, Week, Month, Year }

internal data class RelativeDueDate(val amount: Int, val unit: DueDateUnit, val isPast: Boolean) {
    val signedAmount: Int get() = if (isPast) -amount else amount
    val useNamedDay: Boolean get() = unit == DueDateUnit.Day && amount <= 1
}

/** Uses local calendar days and the largest complete calendar unit, rounded down. */
internal fun relativeDueDate(dueDate: LocalDate, today: LocalDate): RelativeDueDate {
    val isPast = dueDate < today
    val start = minOf(dueDate, today)
    val end = maxOf(dueDate, today)
    val period = start.periodUntil(end)
    val days = start.daysUntil(end)
    return when {
        period.years > 0 -> RelativeDueDate(period.years, DueDateUnit.Year, isPast)
        period.months > 0 -> RelativeDueDate(period.months, DueDateUnit.Month, isPast)
        days >= 7 -> RelativeDueDate(days / 7, DueDateUnit.Week, isPast)
        else -> RelativeDueDate(days, DueDateUnit.Day, isPast)
    }
}

@Composable
fun relativeDueDateText(dueDate: LocalDate, today: LocalDate): String {
    val localeTag = Locale.current.toLanguageTag()
    return remember(dueDate, today, localeTag) {
        formatRelativeDueDate(relativeDueDate(dueDate, today), localeTag)
    }
}

/** Delegates wording, number formatting and pluralization to the platform's locale-aware formatter. */
internal expect fun formatRelativeDueDate(value: RelativeDueDate, localeTag: String): String
