package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_empty_folder
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun EmptyFolderIllustration() {
    val folderColor = EchoListTheme.echoListColorScheme.folderColor
    Box(
        Modifier.size(EchoListTheme.dimensions.xxxl + EchoListTheme.dimensions.xl).background(
            folderColor.copy(alpha = 0.12f),
            EchoListTheme.shapes.extraLarge.copy(all = CornerSize(EchoListTheme.dimensions.xl))
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(Res.drawable.browser_empty_folder),
            contentDescription = null,
            modifier = Modifier.size(EchoListTheme.dimensions.xxl).testTag("browser-empty-icon"),
            tint = folderColor
        )
    }
}

@PreviewLightDark
@Composable
private fun EmptyFolderIllustrationPreview() {
    EchoListTheme(modifier = Modifier) {
        EmptyFolderIllustration()
    }
}
