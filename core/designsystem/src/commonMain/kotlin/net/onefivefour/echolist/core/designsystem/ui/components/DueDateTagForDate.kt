package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/** Resolves the (background, content) colors for a given [urgency] from the active theme. */
@Composable
fun dueDateTagColors(urgency: DueDateTagUrgency): Pair<Color, Color> = when (urgency) {
    DueDateTagUrgency.Normal -> Pair(
        EchoListTheme.materialColors.surfaceVariant,
        EchoListTheme.materialColors.onSurfaceVariant
    )
    DueDateTagUrgency.Warning -> Pair(
        EchoListTheme.echoListColorScheme.warning,
        EchoListTheme.echoListColorScheme.onWarning
    )
    DueDateTagUrgency.Overdue -> Pair(
        EchoListTheme.materialColors.error,
        EchoListTheme.materialColors.onError
    )
}

/**
 * Renders the shared [DueDateTag] for a [dueDate] relative to [today], using the
 * locale-aware relative-date wording and the urgency coloring resolved from [urgency].
 *
 * The [urgency] is passed in because the design system does not depend on `core:tasks`,
 * which owns the urgency calculation. Callers compute it and map it to [DueDateTagUrgency].
 */
@Composable
fun DueDateTagForDate(
    dueDate: LocalDate,
    today: LocalDate,
    urgency: DueDateTagUrgency,
    isRecurring: Boolean? = null,
    onClick: (() -> Unit)? = null
) {
    val (backgroundColor, contentColor) = dueDateTagColors(urgency)
    DueDateTag(
        label = relativeDueDateText(dueDate, today),
        isRecurring = isRecurring,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
private fun DueDateTagForDateNormalPreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        DueDateTagForDate(
            dueDate = LocalDate(
                year = 2026,
                month = 11,
                day = 20,
            ),
            today = LocalDate(
                year = 2026,
                month = 11,
                day = 19,
            ),
            urgency = DueDateTagUrgency.Normal,
            isRecurring = false,
        )
    }
}

@PreviewLightDark
@Composable
private fun DueDateTagForDateWarningPreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        DueDateTagForDate(
            dueDate = LocalDate(
                year = 2026,
                month = 11,
                day = 19,
            ),
            today = LocalDate(
                year = 2026,
                month = 11,
                day = 19,
            ),
            urgency = DueDateTagUrgency.Warning,
            isRecurring = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun DueDateTagForDateOverduePreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        DueDateTagForDate(
            dueDate = LocalDate(
                year = 2026,
                month = 11,
                day = 18,
            ),
            today = LocalDate(
                year = 2026,
                month = 11,
                day = 19,
            ),
            urgency = DueDateTagUrgency.Overdue,
            isRecurring = false,
        )
    }
}
