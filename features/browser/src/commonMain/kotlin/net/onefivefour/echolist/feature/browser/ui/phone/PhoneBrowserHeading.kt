package net.onefivefour.echolist.feature.browser.ui.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.ui.BreadcrumbItem
import net.onefivefour.echolist.feature.browser.ui.BrowserHeading

@Composable
internal fun PhoneBrowserHeading(
    breadcrumbs: List<BreadcrumbItem>,
    onFolderClick: (String) -> Unit
) {
    Column {
        if (breadcrumbs.size > 1) {
            BreadcrumbBar(breadcrumbs, onFolderClick)
        }
        BrowserHeading(
            breadcrumbs,
            EchoListTheme.typography.titleLarge.copy(
                fontSize = 26.sp, lineHeight = 31.sp, letterSpacing = (-1.17).sp
            )
        )
    }
}
