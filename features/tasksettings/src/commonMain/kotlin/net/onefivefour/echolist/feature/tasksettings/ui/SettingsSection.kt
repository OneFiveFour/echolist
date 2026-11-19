package net.onefivefour.echolist.feature.tasksettings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/**
 * A titled section consisting of a header label and a bordered surface card that
 * groups related settings controls. Used to give the Main Task Settings screen a
 * clear, scannable visual hierarchy.
 */
@Composable
internal fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = EchoListTheme.typography.titleMedium,
            color = EchoListTheme.materialColors.onBackground,
            modifier = Modifier.padding(
                start = EchoListTheme.dimensions.xs,
                bottom = EchoListTheme.dimensions.s
            )
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = EchoListTheme.shapes.medium,
            color = EchoListTheme.materialColors.surface,
            border = BorderStroke(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(EchoListTheme.dimensions.m),
                content = content
            )
        }
    }
}

@Preview
@Composable
private fun SettingsSectionPreview() {
    EchoListTheme {
        SettingsSection(
            title = "Due date",
            modifier = Modifier.padding(EchoListTheme.dimensions.l)
        ) {
            Text(
                text = "Section content",
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.onSurface
            )
        }
    }
}