package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/** Compact, reusable urgency count for task-list summaries. The caller supplies localized text. */
@Composable
fun DueCountPill(label: String, urgency: DueDateTagUrgency, modifier: Modifier = Modifier) {
    val color = when (urgency) {
        DueDateTagUrgency.Overdue -> EchoListTheme.materialColors.error
        DueDateTagUrgency.Warning -> EchoListTheme.echoListColorScheme.warning
        DueDateTagUrgency.Normal -> EchoListTheme.echoListColorScheme.statusNormal
    }
    Surface(modifier = modifier, shape = CircleShape, color = color.copy(alpha = 0.1f)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = EchoListTheme.typography.labelSmall.copy(
                letterSpacing = 0.sp, fontSize = 9.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

@PreviewLightDark
@Composable
private fun DueCountPillNormalPreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        DueCountPill(
            label = "4 upcoming",
            urgency = DueDateTagUrgency.Normal,
        )
    }
}

@PreviewLightDark
@Composable
private fun DueCountPillWarningPreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        DueCountPill(
            label = "2 due today",
            urgency = DueDateTagUrgency.Warning,
        )
    }
}

@PreviewLightDark
@Composable
private fun DueCountPillOverduePreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        DueCountPill(
            label = "1 overdue",
            urgency = DueDateTagUrgency.Overdue,
        )
    }
}
