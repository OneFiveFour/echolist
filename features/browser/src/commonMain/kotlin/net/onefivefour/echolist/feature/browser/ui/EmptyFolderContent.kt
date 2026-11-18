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
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_empty
import net.onefivefour.echolist.feature.browser.resources.browser_empty_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyFolderContent(
    createItemCallbacks: CreateItemCallbacks,
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions
    // The surrounding browser already reserves the header, content margins and navigation.
    val layoutModifier = if (isDesktop) modifier.heightIn(max = 494.dp) else modifier

    BoxWithConstraints(layoutModifier.fillMaxSize().testTag("browser-empty")) {
        Column(
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(
                    start = if (isDesktop) dimensions.xxxl else dimensions.xl,
                    end = if (isDesktop) dimensions.xxxl else dimensions.xl,
                    top = if (isDesktop) dimensions.xxxl else dimensions.xl,
                    bottom = if (isDesktop) 104.dp else 60.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyFolderIllustration()
            Spacer(Modifier.height(dimensions.xl))
            Text(
                text = stringResource(Res.string.browser_empty_title),
                modifier = Modifier.testTag("browser-empty-title"),
                style = EchoListTheme.echoListTypography.title,
                color = EchoListTheme.materialColors.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.s))
            Text(
                text = stringResource(Res.string.browser_empty),
                modifier = Modifier.widthIn(max = if (isDesktop) 340.dp else 314.dp),
                style = EchoListTheme.echoListTypography.bodySmall.copy(
                    fontSize = 13.sp, lineHeight = 20.sp, letterSpacing = 0.sp
                ),
                color = EchoListTheme.materialColors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.xl))
            EmptyFolderCreateButton(createItemCallbacks)
        }
    }
}
