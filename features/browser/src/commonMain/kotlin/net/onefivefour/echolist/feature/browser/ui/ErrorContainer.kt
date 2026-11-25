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
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_delete_failed
import net.onefivefour.echolist.feature.browser.resources.browser_load_failed
import org.jetbrains.compose.resources.stringResource

/** Localized text for a [BrowserError]; technical exception messages never reach the UI. */
@Composable
internal fun browserErrorMessage(error: BrowserError): String = stringResource(
    when (error) {
        BrowserError.LOAD_FAILED -> Res.string.browser_load_failed
        BrowserError.DELETE_FAILED -> Res.string.browser_delete_failed
    }
)

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
            color = EchoListTheme.materialColors.error
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun ErrorContainerPreview() {
    EchoListTheme(modifier = Modifier) {
        ErrorContainer(message = browserErrorMessage(BrowserError.LOAD_FAILED))
    }
}