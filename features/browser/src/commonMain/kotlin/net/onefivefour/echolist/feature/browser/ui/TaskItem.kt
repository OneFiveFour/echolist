package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.core.designsystem.ui.components.DueCountPill
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTagUrgency
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TaskItem(id: String, title: String, doneTaskCount: Int, totalTaskCount: Int,
    onClick: (String) -> Unit, modifier: Modifier = Modifier, isWide: Boolean = false) {
    val color = EchoListTheme.echoListColorScheme.taskColor
    BrowserItemCard(title, Res.drawable.browser_tasks, color, false, isWide, { onClick(id) }, modifier, compactHeight = 99.dp) {
        Row(Modifier.padding(top = EchoListTheme.dimensions.s).widthIn(max = 210.dp),
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(3.dp).clip(EchoListTheme.shapes.small).background(EchoListTheme.materialColors.surfaceVariant)) {
                val progress = if (totalTaskCount > 0) (doneTaskCount.toFloat() / totalTaskCount).coerceIn(0f, 1f) else 0f
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(color))
            }
            Text("$doneTaskCount/$totalTaskCount", style = EchoListTheme.typography.labelSmall.copy(letterSpacing = 0.sp, fontSize = 10.sp, lineHeight = 15.sp),
                color = EchoListTheme.materialColors.onSurfaceVariant)
        }
        // TODO backend: replace these design preview counts with per-list urgency counts.
        Row(Modifier.padding(top = EchoListTheme.dimensions.s), horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)) {
            DueCountPill(stringResource(Res.string.count_overdue, 1), DueDateTagUrgency.Overdue)
            DueCountPill(stringResource(Res.string.count_due_soon, 2), DueDateTagUrgency.Warning)
        }
    }
}
