package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.resources.Res
import net.onefivefour.echolist.core.designsystem.resources.ic_search
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun RoundIconButton(
    iconRes: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = EchoListTheme.materialColors.surface,
    contentColor: Color = EchoListTheme.materialColors.onSurface,
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier
            .size(EchoListTheme.dimensions.xxxl),
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            color = EchoListTheme.materialColors.surfaceVariant
        ),
        color = containerColor
    ) {
        Icon(
            modifier = Modifier.padding(14.dp),
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = contentColor
        )
    }
}

@PreviewLightDark
@Composable
private fun RoundIconButtonPreview() {
    EchoListTheme(modifier = Modifier) {
        RoundIconButton(
            iconRes = Res.drawable.ic_search,
            onClick = {},
            contentDescription = "Search",
        )
    }
}
