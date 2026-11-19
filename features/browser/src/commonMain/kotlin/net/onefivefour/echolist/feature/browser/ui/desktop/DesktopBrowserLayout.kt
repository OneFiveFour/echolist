package net.onefivefour.echolist.feature.browser.ui.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewDesktopLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.BrowserBackButton
import net.onefivefour.echolist.feature.browser.ui.CreateItemButton
import net.onefivefour.echolist.feature.browser.ui.CreateItemCallbacks
import net.onefivefour.echolist.feature.browser.ui.FileOverview
import net.onefivefour.echolist.feature.browser.ui.browserPreviewState

@Composable
internal fun DesktopBrowserLayout(
    createItemCallbacks: CreateItemCallbacks,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    toolbar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 80.dp)) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = 152.dp).padding(top = 40.dp, bottom = 24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                toolbar()
            }
            Spacer(Modifier.height(EchoListTheme.dimensions.xxl))
            Box(Modifier.weight(1f)) {
                content()
            }
        }
        Column(
            Modifier.align(Alignment.BottomEnd)
                .padding(end = 68.dp, bottom = EchoListTheme.dimensions.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
        ) {
            if (canNavigateBack) {
                BrowserBackButton(onBack = onBack, size = 52.dp, shape = EchoListTheme.shapes.extraLarge)
            }
            CreateItemButton(createItemCallbacks, isDesktop = true)
        }
    }
}

@PreviewDesktopLightDark
@Composable
private fun DesktopBrowserLayoutPreview() {
    EchoListTheme {
        DesktopBrowserLayout(
            CreateItemCallbacks(),
            true,
            {},
            toolbar = { DesktopBrowserHeading(browserPreviewState.breadcrumbs) },
            content = { FileOverview(browserPreviewState.fileEntries, false, null, {}, {}, {}, isDesktop = true) },
        )
    }
}
