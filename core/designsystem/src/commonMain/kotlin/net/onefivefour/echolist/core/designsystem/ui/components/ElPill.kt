package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/** Shared pill container for text, icons or other inline content. */
@Composable
fun ElPill(
    background: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = EchoListTheme.materialColors.onSurfaceVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = background,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .then(
                    when {
                        onClick != null -> Modifier.clickable(onClick = onClick)
                        else -> Modifier
                    }
                )
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.xs
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(EchoListTheme.typography.labelMedium) {
                content()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ElPillPreview() {
    EchoListTheme(modifier = Modifier) {
        ElPill(background = EchoListTheme.materialColors.surfaceVariant) {
            Text("Tomorrow")
        }
    }
}
