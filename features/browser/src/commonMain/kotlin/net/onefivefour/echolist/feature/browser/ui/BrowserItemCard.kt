package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    titleContent: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = EchoListTheme.dimensions
    // Content may grow with font scale, while each item type keeps a consistent minimum.
    val minimumHeight = if (isFolder) {
        dimensions.iconMedium + dimensions.m * 2
    } else {
        dimensions.xxxl * 2
    }
    val sizeModifier = Modifier.height(IntrinsicSize.Min).heightIn(min = minimumHeight)
    val clickModifier = when {
        onClick == null -> Modifier
        onLongClick != null -> Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel
        )
        else -> Modifier.clickable(onClick = onClick)
    }
    val tag = when {
        titleContent != null -> "browser-folder-draft"
        isFolder -> "browser-folder"
        else -> "browser-file"
    }
    Surface(
        modifier = modifier.fillMaxWidth().then(clickModifier).then(sizeModifier).testTag(tag),
        shape = EchoListTheme.shapes.large,
        border = BorderStroke(EchoListTheme.dimensions.borderWidth, EchoListTheme.materialColors.outlineVariant),
        color = EchoListTheme.materialColors.surface
    ) {
        Box {
            Box(Modifier.width(dimensions.xs).fillMaxHeight().background(accent))
            Row(
                Modifier.fillMaxSize().padding(dimensions.m),
                verticalAlignment = if (isFolder) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
            ) {
                Box(
                    Modifier.size(dimensions.iconMedium).clip(EchoListTheme.shapes.medium)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(painterResource(icon), null, Modifier.size(dimensions.xl), tint = accent) }
                Column(Modifier.weight(1f)) {
                    if (titleContent != null) {
                        titleContent()
                    } else {
                        Text(
                            title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = EchoListTheme.typography.titleSmall,
                            color = EchoListTheme.materialColors.onSurface
                        )
                    }
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
            onClick = {},
        ) {
            Text("3 items")
        }
    }
}
