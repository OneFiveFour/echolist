package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BrowserItemCard(
    title: String,
    icon: DrawableResource,
    accent: Color,
    isFolder: Boolean,
    isDesktop: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compactHeight: Dp = 96.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(if (isFolder) {
            if (isDesktop) 76.dp else 68.dp
        } else if (isDesktop) 118.dp else compactHeight).testTag(if (isFolder) "browser-folder" else "browser-file"),
        shape = EchoListTheme.shapes.large,
        border = BorderStroke(EchoListTheme.dimensions.borderWidth, EchoListTheme.materialColors.outlineVariant),
        color = EchoListTheme.materialColors.surface
    ) {
        Box {
            Box(Modifier.width(3.dp).fillMaxHeight().background(accent))
            Row(
                Modifier.fillMaxSize().padding(if (isDesktop) EchoListTheme.dimensions.l else EchoListTheme.dimensions.m),
                verticalAlignment = if (isFolder) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
            ) {
                Box(
                    Modifier.size(if (isDesktop) 42.dp else 40.dp).clip(EchoListTheme.shapes.medium)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(painterResource(icon), null, Modifier.size(21.dp), tint = accent) }
                Column(Modifier.weight(1f)) {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = EchoListTheme.typography.titleSmall,
                        color = EchoListTheme.materialColors.onSurface)
                    content()
                }
            }
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun BrowserItemCardPreview() {
    EchoListTheme(modifier = Modifier) {
        BrowserItemCard(
            title = "Design",
            icon = Res.drawable.browser_home,
            accent = EchoListTheme.echoListColorScheme.folderColor,
            isFolder = true,
            isDesktop = false,
            onClick = {},
        ) {
            Text("3 items")
        }
    }
}
