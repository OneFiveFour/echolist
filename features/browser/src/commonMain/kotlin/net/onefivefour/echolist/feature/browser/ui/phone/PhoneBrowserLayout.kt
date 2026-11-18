package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem

@Composable
internal fun PhoneBrowserLayout(
    breadcrumbs: List<BreadcrumbItem>,
    toolbar: @Composable (TextStyle) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = EchoListTheme.dimensions.l)
    ) {
        if (breadcrumbs.size > 1) {
            BreadcrumbBar(breadcrumbs)
        }
        Spacer(Modifier.height(EchoListTheme.dimensions.s))
        toolbar(EchoListTheme.typography.titleLarge.copy(fontSize = 26.sp, lineHeight = 31.sp))
        Spacer(Modifier.height(EchoListTheme.dimensions.xl))
        Box(Modifier.weight(1f)) {
            content()
        }
    }
}
