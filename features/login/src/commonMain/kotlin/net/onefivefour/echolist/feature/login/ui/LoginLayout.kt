package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import net.onefivefour.echolist.feature.login.ui.desktop.DesktopHeroBanner
import net.onefivefour.echolist.feature.login.ui.desktop.desktopLoginFormLayout
import net.onefivefour.echolist.feature.login.ui.phone.phoneLoginFormLayout

internal data class LoginFormLayout(
    val padding: PaddingValues,
    val arrangement: Arrangement.Vertical
)

/** Keeps the form in one composition location so native field focus survives resizing. */
@Composable
internal fun LoginLayout(
    isDesktop: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(modifier.fillMaxSize().imePadding()) {
        if (isDesktop) {
            DesktopHeroBanner(Modifier.fillMaxWidth(0.44f).fillMaxHeight())
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
            val layout = if (isDesktop) desktopLoginFormLayout(maxWidth) else phoneLoginFormLayout()
            LoginFormContainer(scrollState, maxHeight, layout, content)
        }
    }
}

@Composable
private fun LoginFormContainer(
    scrollState: ScrollState,
    minimumHeight: Dp,
    layout: LoginFormLayout,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState)
            .heightIn(min = minimumHeight)
            .padding(layout.padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = layout.arrangement
    ) {
        content()
    }
}
