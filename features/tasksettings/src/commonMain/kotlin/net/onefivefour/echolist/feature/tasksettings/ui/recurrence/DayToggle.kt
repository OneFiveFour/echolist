package net.onefivefour.echolist.feature.tasksettings.ui.recurrence

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/**
 * A circular, selectable weekday chip showing the day's abbreviation.
 * Selected days use the primary color; unselected days use the surface variant.
 */
@Composable
internal fun DayToggle(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) {
            EchoListTheme.materialColors.primary
        } else {
            EchoListTheme.materialColors.surfaceVariant
        },
        modifier = modifier.size(EchoListTheme.dimensions.iconSmall)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = EchoListTheme.typography.labelSmall,
                color = if (isSelected) {
                    EchoListTheme.materialColors.onPrimary
                } else {
                    EchoListTheme.materialColors.onSurfaceVariant
                }
            )
        }
    }
}

@Preview
@Composable
private fun DayTogglePreview() {
    EchoListTheme {
        DayToggle(
            label = "Mon",
            isSelected = true,
            onClick = {}
        )
    }
}