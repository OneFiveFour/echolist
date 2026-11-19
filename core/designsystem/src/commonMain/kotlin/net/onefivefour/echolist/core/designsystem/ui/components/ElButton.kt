package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
fun ElButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val dimensions = EchoListTheme.dimensions
    val colors = EchoListTheme.materialColors
    Button(
        modifier = modifier.heightIn(min = dimensions.buttonMinHeight),
        onClick = onClick,
        enabled = isEnabled,
        shape = EchoListTheme.shapes.extraLarge,
        contentPadding = PaddingValues(
            start = dimensions.buttonPaddingStart,
            end = dimensions.buttonPaddingEnd
        ),
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun ElButtonPreview() {
    EchoListTheme(modifier = Modifier) {
        ElButton(onClick = {}) {
            Text(text = "Connect")
        }
    }
}

@PreviewLightDark
@Composable
private fun ElButtonDisabledPreview() {
    EchoListTheme(modifier = Modifier) {
        ElButton(onClick = {}, isEnabled = false) {
            Text(text = "Connect")
        }
    }
}
