package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.components.DueCountPill
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTagUrgency
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TaskListItem(
    id: String,
    title: String,
    doneTaskCount: Int,
    totalTaskCount: Int,
    overdueTaskCount: Int?,
    dueSoonTaskCount: Int?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = EchoListTheme.echoListColorScheme.taskColor

    BrowserItemCard(
        title = title,
        icon = Res.drawable.browser_tasks,
        accent = color,
        isFolder = false,
        onClick = { onClick(id) },
        modifier = modifier
    ) {
        Row(
            Modifier
                .padding(top = EchoListTheme.dimensions.s)
                .widthIn(max = 210.dp),
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(modifier = Modifier
                .weight(1f)
                .height(EchoListTheme.dimensions.xs)
                .clip(EchoListTheme.shapes.small)
                .background(EchoListTheme.materialColors.surfaceVariant)
            ) {
                val progress = when {
                    totalTaskCount > 0 -> (doneTaskCount.toFloat() / totalTaskCount).coerceIn(0f, 1f)
                    else -> 0f
                }
                Box(Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(color)
                )
            }

            Text("$doneTaskCount/$totalTaskCount", style = EchoListTheme.typography.labelSmall,
                color = EchoListTheme.materialColors.onSurfaceVariant)
        }

        if ((overdueTaskCount ?: 0) > 0 || (dueSoonTaskCount ?: 0) > 0) {
            Row(
                modifier = Modifier.padding(top = EchoListTheme.dimensions.s),
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                if (overdueTaskCount != null && overdueTaskCount > 0) {
                    DueCountPill(
                        label = stringResource(Res.string.count_overdue, overdueTaskCount),
                        urgency = DueDateTagUrgency.Overdue
                    )
                }
                if (dueSoonTaskCount != null && dueSoonTaskCount > 0) {
                    DueCountPill(
                        label = stringResource(Res.string.count_due_soon, dueSoonTaskCount),
                        urgency = DueDateTagUrgency.Warning
                    )
                }
            }
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun TaskListItemPreview() {
    EchoListTheme(modifier = Modifier) {
        TaskListItem(
            id = "tasks-1",
            title = "Release checklist",
            doneTaskCount = 3,
            totalTaskCount = 8,
            overdueTaskCount = 1,
            dueSoonTaskCount = 2,
            onClick = {}
        )
    }
}
