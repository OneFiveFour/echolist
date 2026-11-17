package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun NoteItem(id: String, title: String, preview: String?, onClick: (String) -> Unit,
    modifier: Modifier = Modifier, isWide: Boolean = false) {
    val color = EchoListTheme.echoListColorScheme.noteColor
    BrowserItemCard(title, Res.drawable.browser_note, color, false, isWide, { onClick(id) }, modifier) {
        Spacer(Modifier.height(EchoListTheme.dimensions.s))
        Box(Modifier.width(30.dp).height(2.dp).background(color))
        if (!preview.isNullOrEmpty()) {
            Text(preview, modifier = Modifier.padding(top = EchoListTheme.dimensions.s),
                style = EchoListTheme.typography.bodySmall.copy(letterSpacing = 0.sp, fontSize = 11.sp, lineHeight = 16.sp),
                color = EchoListTheme.materialColors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
