package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewPhoneLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_empty
import net.onefivefour.echolist.feature.browser.resources.browser_empty_title
import org.jetbrains.compose.resources.stringResource

private val EmptyFolderTextMaxWidth = 340.dp

@Composable
internal fun EmptyFolderContent(
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("browser-empty")
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(all = dimensions.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyFolderIllustration()
            Spacer(Modifier.height(dimensions.xl))
            Text(
                text = stringResource(Res.string.browser_empty_title),
                modifier = Modifier.testTag("browser-empty-title"),
                style = EchoListTheme.typography.titleMedium,
                color = EchoListTheme.materialColors.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.s))
            Text(
                text = stringResource(Res.string.browser_empty),
                modifier = Modifier.widthIn(max = EmptyFolderTextMaxWidth),
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@PreviewPhoneLightDark
@Composable
private fun EmptyFolderContentPreview() {
    EchoListTheme {
        EmptyFolderContent(
            createItemCallbacks = CreateItemCallbacks()
        )
    }
}
