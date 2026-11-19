package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.login_server
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun LoginFieldIcon(
    resource: DrawableResource,
    tint: Color = EchoListTheme.materialColors.outline
) {
    Icon(
        painter = painterResource(resource = resource),
        contentDescription = null,
        modifier = Modifier.size(19.dp),
        tint = tint
    )
}

@PreviewLightDark
@Composable
private fun LoginFieldIconPreview() {
    EchoListTheme(modifier = Modifier) {
        LoginFieldIcon(Res.drawable.login_server)
    }
}
