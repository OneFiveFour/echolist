package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import net.onefivefour.echolist.core.designsystem.resources.Res
import net.onefivefour.echolist.core.designsystem.resources.due_date_recurring
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.stringResource

/** A shared due-date pill. Null recurrence means the source does not supply that information. */
@Composable
fun DueDateTag(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    isRecurring: Boolean? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = CircleShape,
        color = backgroundColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.xs
                )
        ) {
            if (isRecurring == true) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.due_date_recurring),
                    modifier = Modifier
                        .padding(end = EchoListTheme.dimensions.xs)
                        .size(EchoListTheme.dimensions.l),
                    tint = contentColor
                )
            }
            Text(
                text = label,
                style = EchoListTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}
