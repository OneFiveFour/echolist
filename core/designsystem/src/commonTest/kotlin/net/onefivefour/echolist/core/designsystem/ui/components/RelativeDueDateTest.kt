package net.onefivefour.echolist.core.designsystem.ui.components

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

internal class RelativeDueDateTest : FunSpec({
    val today = LocalDate(2026, 10, 4)

    test("platform formatter produces the requested English labels") {
        val cases = listOf(
            "2026-10-06" to "in 2 days",
            "2026-10-05" to "tomorrow",
            "2026-10-04" to "today",
            "2026-10-03" to "yesterday",
            "2026-10-02" to "2 days ago",
            "2026-10-11" to "in 1 week",
            "2026-11-04" to "in 1 month",
            "2027-10-04" to "in 1 year",
            "2026-09-20" to "2 weeks ago",
            "2026-08-04" to "2 months ago",
            "2024-10-04" to "2 years ago"
        )
        cases.forEach { (date, expected) ->
            formatRelativeDueDate(relativeDueDate(LocalDate.parse(date), today), "en-US") shouldBe expected
        }
    }

    test("platform formatter uses the requested locale for words and plurals") {
        formatRelativeDueDate(relativeDueDate(today, today), "de-DE") shouldBe "heute"
        formatRelativeDueDate(relativeDueDate(LocalDate(2026, 10, 6), today), "de-DE") shouldBe "in 2 Tagen"
        formatRelativeDueDate(relativeDueDate(LocalDate(2026, 9, 4), today), "de-DE") shouldBe "vor 1 Monat"
    }

    test("nearby dates use calendar days on both sides of today") {
        for (offset in -6..6) {
            val dueDate = LocalDate.fromEpochDays(today.toEpochDays() + offset)
            relativeDueDate(dueDate, today) shouldBe
                RelativeDueDate(kotlin.math.abs(offset), DueDateUnit.Day, offset < 0)
        }
    }

    test("longer intervals use complete weeks months and years") {
        val cases = listOf(
            "2026-10-11" to RelativeDueDate(1, DueDateUnit.Week, false),
            "2026-10-17" to RelativeDueDate(1, DueDateUnit.Week, false),
            "2026-10-18" to RelativeDueDate(2, DueDateUnit.Week, false),
            "2026-11-03" to RelativeDueDate(4, DueDateUnit.Week, false),
            "2026-11-04" to RelativeDueDate(1, DueDateUnit.Month, false),
            "2027-01-04" to RelativeDueDate(3, DueDateUnit.Month, false),
            "2027-10-03" to RelativeDueDate(11, DueDateUnit.Month, false),
            "2027-10-04" to RelativeDueDate(1, DueDateUnit.Year, false),
            "2028-10-04" to RelativeDueDate(2, DueDateUnit.Year, false),
            "2026-09-27" to RelativeDueDate(1, DueDateUnit.Week, true),
            "2026-09-04" to RelativeDueDate(1, DueDateUnit.Month, true),
            "2024-10-04" to RelativeDueDate(2, DueDateUnit.Year, true)
        )
        cases.forEach { (date, expected) ->
            relativeDueDate(LocalDate.parse(date), today) shouldBe expected
        }
    }

    test("calendar boundaries and leap days use actual calendar intervals") {
        relativeDueDate(LocalDate(2026, 3, 1), LocalDate(2026, 2, 1)) shouldBe
            RelativeDueDate(1, DueDateUnit.Month, false)
        relativeDueDate(LocalDate(2024, 3, 1), LocalDate(2024, 2, 28)) shouldBe
            RelativeDueDate(2, DueDateUnit.Day, false)
        relativeDueDate(LocalDate(2025, 1, 1), LocalDate(2024, 12, 31)) shouldBe
            RelativeDueDate(1, DueDateUnit.Day, false)
        relativeDueDate(LocalDate(2024, 3, 1), LocalDate(2023, 3, 1)) shouldBe
            RelativeDueDate(1, DueDateUnit.Year, false)
    }
})
