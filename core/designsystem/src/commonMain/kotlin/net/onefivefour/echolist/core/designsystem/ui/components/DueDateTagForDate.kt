package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.datetime.LocalDate
import net.onefivefour.echolist.core.designsystem.resources.Res
import net.onefivefour.echolist.core.designsystem.resources.due_date_recurring
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

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

@Composable
fun DueDateTagForDate(
    dueDate: LocalDate,
    today: LocalDate,
    urgency: DueDateTagUrgency,
    isRecurring: Boolean? = null,
    onClick: (() -> Unit)? = null
) {
    val (backgroundColor, contentColor) = dueDateTagColors(urgency)
    ElPill(
        background = backgroundColor,
        contentColor = contentColor,
        onClick = onClick
    ) {
        if (isRecurring == true) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(Res.string.due_date_recurring),
                modifier = Modifier
                    .padding(end = EchoListTheme.dimensions.xs)
                    .size(EchoListTheme.dimensions.l)
            )
        }
        Text(text = relativeDueDateText(dueDate, today))
    }
}

@PreviewLightDark
@Composable
private fun DueDateTagForDateNormalPreview() {
    EchoListTheme(modifier = Modifier) {
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
    EchoListTheme(modifier = Modifier) {
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
    EchoListTheme(modifier = Modifier) {
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
