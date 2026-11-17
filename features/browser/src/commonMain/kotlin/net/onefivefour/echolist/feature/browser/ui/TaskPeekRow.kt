package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTagForDate
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTagUrgency
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.tasks.domain.DueDateUrgencyCalculator
import net.onefivefour.echolist.core.tasks.domain.model.DueDateUrgency
import kotlin.time.Clock

@Composable
internal fun TasksSummaryItemRow(
    row: TasksSummaryRow,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = EchoListTheme.dimensions.m,
                vertical = EchoListTheme.dimensions.xs
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides EchoListTheme.dimensions.xl) {
            // Read-only: onCheckedChange = null makes the checkbox non-interactive.
            Checkbox(
                checked = row.isDone,
                onCheckedChange = null
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = row.description,
            style = EchoListTheme.typography.bodyMedium,
            color = EchoListTheme.materialColors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        row.dueDate?.let { dueDate ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            DueDateTagForDate(
                dueDate = dueDate,
                today = today,
                urgency = DueDateUrgencyCalculator.computeUrgency(dueDate, today).toTagUrgency()
            )
        }
    }
}

private fun DueDateUrgency.toTagUrgency(): DueDateTagUrgency = when (this) {
    DueDateUrgency.Normal -> DueDateTagUrgency.Normal
    DueDateUrgency.Warning -> DueDateTagUrgency.Warning
    DueDateUrgency.Overdue -> DueDateTagUrgency.Overdue
}