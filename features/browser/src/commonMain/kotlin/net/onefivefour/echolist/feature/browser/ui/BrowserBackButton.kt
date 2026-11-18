package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_back
import net.onefivefour.echolist.feature.browser.resources.navigate_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BrowserBackButton(
    onBack: () -> Unit,
    size: Dp,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onBack,
        modifier = modifier.size(size).testTag("browser-back"),
        shape = shape,
        color = EchoListTheme.materialColors.surface,
        border = BorderStroke(EchoListTheme.dimensions.borderWidth, EchoListTheme.materialColors.outlineVariant),
        shadowElevation = 12.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painterResource(Res.drawable.browser_back),
                contentDescription = stringResource(Res.string.navigate_back),
                modifier = Modifier.size(21.dp),
                tint = EchoListTheme.materialColors.onSurface
            )
        }
    }
}
