package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*

@Composable
internal fun NoteItem(id: String, title: String, preview: String?, onClick: (String) -> Unit,
    modifier: Modifier = Modifier) {
    val color = EchoListTheme.echoListColorScheme.noteColor
    BrowserItemCard(title, Res.drawable.browser_note, color, false, { onClick(id) }, modifier) {
        Spacer(Modifier.height(EchoListTheme.dimensions.s))
        Box(Modifier.width(EchoListTheme.dimensions.xxl).height(EchoListTheme.dimensions.xxs).background(color))
        if (!preview.isNullOrEmpty()) {
            Text(preview, modifier = Modifier.padding(top = EchoListTheme.dimensions.s),
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun NoteItemPreview() {
    EchoListTheme(modifier = Modifier) {
        NoteItem(
            id = "note-1",
            title = "Meeting notes",
            preview = "Ideas and decisions for our next release.",
            onClick = {})
    }
}
