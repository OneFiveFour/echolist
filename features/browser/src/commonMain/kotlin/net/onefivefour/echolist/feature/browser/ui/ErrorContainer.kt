package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun ErrorContainer(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(EchoListTheme.dimensions.l),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = EchoListTheme.typography.bodyMedium,
            color = EchoListTheme.materialColors.secondary
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun ErrorContainerPreview() {
    EchoListTheme(modifier = Modifier) {
        ErrorContainer(message = "The server could not be reached. Please try again.")
    }
}
