package net.onefivefour.echolist.feature.browser.ui.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.your_space
import net.onefivefour.echolist.feature.browser.ui.CreateItemButton
import net.onefivefour.echolist.feature.browser.ui.CreateItemCallbacks
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopBrowserLayout(
    createItemCallbacks: CreateItemCallbacks,
    toolbar: @Composable (TextStyle) -> Unit,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 80.dp, vertical = 64.dp)) {
            Text(
                text = stringResource(resource = Res.string.your_space),
                style = EchoListTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                color = EchoListTheme.materialColors.onSurfaceVariant
            )
            Spacer(Modifier.height(EchoListTheme.dimensions.s))
            toolbar(EchoListTheme.typography.titleLarge.copy(fontSize = 40.sp, lineHeight = 48.sp))
            Spacer(Modifier.height(56.dp))
            Box(Modifier.weight(1f)) {
                content()
            }
        }
        Box(
            Modifier.align(Alignment.BottomEnd)
                .padding(end = 68.dp, bottom = EchoListTheme.dimensions.xl)
        ) {
            CreateItemButton(createItemCallbacks, isDesktop = true)
        }
    }
}
