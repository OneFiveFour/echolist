package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
fun DueCountPill(
    label: String,
    urgency: DueDateTagUrgency,
    modifier: Modifier = Modifier
) {
    val color = when (urgency) {
        DueDateTagUrgency.Overdue -> EchoListTheme.materialColors.error
        DueDateTagUrgency.Warning -> EchoListTheme.echoListColorScheme.warning
        DueDateTagUrgency.Normal -> EchoListTheme.echoListColorScheme.statusNormal
    }
    ElPill(
        background = color.copy(alpha = 0.1f),
        modifier = modifier,
        contentColor = color
    ) {
        Text(
            text = label,
            style = EchoListTheme.typography.labelSmall
        )
    }
}

@PreviewLightDark
@Composable
private fun DueCountPillNormalPreview() {
    EchoListTheme(modifier = Modifier) {
        DueCountPill(
            label = "4 upcoming",
            urgency = DueDateTagUrgency.Normal,
        )
    }
}

@PreviewLightDark
@Composable
private fun DueCountPillWarningPreview() {
    EchoListTheme(modifier = Modifier) {
        DueCountPill(
            label = "2 due today",
            urgency = DueDateTagUrgency.Warning,
        )
    }
}

@PreviewLightDark
@Composable
private fun DueCountPillOverduePreview() {
    EchoListTheme(modifier = Modifier) {
        DueCountPill(
            label = "1 overdue",
            urgency = DueDateTagUrgency.Overdue,
        )
    }
}
