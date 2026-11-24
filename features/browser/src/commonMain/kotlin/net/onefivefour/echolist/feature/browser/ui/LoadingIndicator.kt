package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(EchoListTheme.dimensions.l),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = EchoListTheme.materialColors.primary
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun LoadingIndicatorPreview() {
    EchoListTheme(modifier = Modifier) {
        LoadingIndicator()
    }
}