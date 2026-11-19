package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_back
import net.onefivefour.echolist.feature.browser.resources.navigate_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BrowserBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onBack,
        modifier = modifier.size(EchoListTheme.dimensions.buttonMinHeight).testTag("browser-back"),
        shape = EchoListTheme.shapes.extraLarge,
        color = EchoListTheme.materialColors.surface,
        border = BorderStroke(EchoListTheme.dimensions.borderWidth, EchoListTheme.materialColors.outlineVariant),
        shadowElevation = EchoListTheme.dimensions.m
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painterResource(Res.drawable.browser_back),
                contentDescription = stringResource(Res.string.navigate_back),
                modifier = Modifier.size(EchoListTheme.dimensions.xl),
                tint = EchoListTheme.materialColors.onSurface
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BrowserBackButtonPreview() {
    EchoListTheme(modifier = Modifier) {
        BrowserBackButton(
            onBack = {}
        )
    }
}
