package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

/** Keep the content at one composition location when the navigation layout changes. */
@Composable
internal fun BrowserLayout(
    isDesktop: Boolean,
    createItemCallbacks: CreateItemCallbacks,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    toolbar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(horizontal = if (isDesktop) 0.dp else 20.dp)) {
        Column(Modifier.fillMaxSize().padding(horizontal = if (isDesktop) 80.dp else 0.dp)) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = if (isDesktop) 152.dp else 112.dp)
                    .padding(top = if (isDesktop) 40.dp else 16.dp, bottom = if (isDesktop) 24.dp else 12.dp),
                contentAlignment = Alignment.BottomStart
            ) { toolbar() }
            Spacer(Modifier.height(if (isDesktop) EchoListTheme.dimensions.xxl else 20.dp))
            Box(Modifier.weight(1f)) { content() }
        }
        if (isDesktop) {
            Column(
                Modifier.align(Alignment.BottomEnd).padding(end = 68.dp, bottom = EchoListTheme.dimensions.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
            ) {
                if (canNavigateBack) BrowserBackButton(onBack, size = 52.dp, shape = EchoListTheme.shapes.extraLarge)
                CreateItemButton(createItemCallbacks, isDesktop = true)
            }
        } else if (canNavigateBack) {
            BrowserBackButton(
                onBack = onBack,
                size = 48.dp,
                shape = EchoListTheme.shapes.large,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 2.dp)
            )
        }
    }
}
