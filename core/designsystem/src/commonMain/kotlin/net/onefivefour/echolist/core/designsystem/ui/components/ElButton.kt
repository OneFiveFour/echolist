package net.onefivefour.echolist.core.designsystem.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
fun ElButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    shape: Shape = RoundedCornerShape(50),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = EchoListTheme.materialColors.primary,
        contentColor = EchoListTheme.materialColors.onPrimary
    ),
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = isEnabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = colors
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun ElButtonPreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        ElButton(
            onClick = {},
        ) {
            Text(
                text = "Connect",
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ElButtonDisabledPreview() {
    EchoListTheme(
        modifier = Modifier,
    ) {
        ElButton(
            onClick = {},
            isEnabled = false,
        ) {
            Text(
                text = "Connect",
            )
        }
    }
}
