package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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