package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.BrowserBackButton

@Composable
internal fun PhoneBrowserLayout(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    toolbar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = 112.dp).padding(top = 16.dp, bottom = 12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                toolbar()
            }
            Spacer(Modifier.height(20.dp))
            Box(Modifier.weight(1f)) {
                content()
            }
        }
        if (canNavigateBack) {
            BrowserBackButton(
                onBack = onBack,
                size = 48.dp,
                shape = EchoListTheme.shapes.large,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 2.dp)
            )
        }
    }
}
