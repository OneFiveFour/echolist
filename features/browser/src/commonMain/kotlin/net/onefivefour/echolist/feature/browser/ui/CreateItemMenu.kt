package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private data class CreateMenuEntry(
    val label: String,
    val icon: DrawableResource,
    val accent: Color,
    val onClick: () -> Unit
)

/**
 * Popup with three stacked icon buttons that opens above its anchor.
 * The gap between the buttons is smaller than the gap between the anchor and the lowest button.
 */
@Composable
internal fun CreateItemMenu(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    createItemCallbacks: CreateItemCallbacks
) {
    if (!isExpanded) return

    val dimensions = EchoListTheme.dimensions
    val colorScheme = EchoListTheme.echoListColorScheme
    val anchorGapPx = with(LocalDensity.current) { dimensions.m.roundToPx() }
    val positionProvider = remember(anchorGapPx) { AboveAnchorPositionProvider(anchorGapPx) }

    val entries = listOf(
        CreateMenuEntry(
            label = stringResource(Res.string.create_folder),
            icon = Res.drawable.browser_folder,
            accent = colorScheme.folderColor,
            onClick = createItemCallbacks.onCreateFolder
        ),
        CreateMenuEntry(
            label = stringResource(Res.string.create_note),
            icon = Res.drawable.browser_note,
            accent = colorScheme.noteColor,
            onClick = createItemCallbacks.onCreateNote
        ),
        CreateMenuEntry(
            label = stringResource(Res.string.create_task_list),
            icon = Res.drawable.browser_tasks,
            accent = colorScheme.taskColor,
            onClick = createItemCallbacks.onCreateTaskList
        )
    )

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensions.s),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (entry in entries) {
                CreateItemMenuButton(
                    entry = entry,
                    onClick = {
                        onDismiss()
                        entry.onClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateItemMenuButton(
    entry: CreateMenuEntry,
    onClick: () -> Unit
) {
    val dimensions = EchoListTheme.dimensions
    val buttonColor = entry.accent
        .copy(alpha = 0.12f)
        .compositeOver(EchoListTheme.materialColors.surface)

    Surface(
        onClick = onClick,
        shape = EchoListTheme.shapes.extraLarge,
        color = buttonColor,
        border = BorderStroke(
            width = dimensions.borderWidth,
            color = EchoListTheme.materialColors.outlineVariant
        ),
        modifier = Modifier
            .size(dimensions.buttonMinHeight)
            .testTag("create-${entry.label}")
            .semantics { contentDescription = entry.label }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(dimensions.xl)) {
                Icon(
                    painter = painterResource(resource = entry.icon),
                    contentDescription = null,
                    tint = entry.accent
                )
                // Filled with the button color so the icon strokes do not shine through the plus.
                Icon(
                    painter = painterResource(resource = Res.drawable.browser_plus),
                    contentDescription = null,
                    tint = entry.accent,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = dimensions.xs, y = dimensions.xs)
                        .size(dimensions.l)
                        .background(buttonColor, CircleShape)
                )
            }
        }
    }
}

/** Centers the popup horizontally over the anchor and places it above, separated by [anchorGapPx]. */
private class AboveAnchorPositionProvider(private val anchorGapPx: Int) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = (anchorBounds.center.x - popupContentSize.width / 2)
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val y = (anchorBounds.top - anchorGapPx - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

@PreviewComponentLightDark
@Composable
private fun CreateItemMenuPreview() {
    EchoListTheme(modifier = Modifier) {
        Box(Modifier.padding(top = EchoListTheme.dimensions.xxxl * 4)) {
            CreateItemMenu(
                isExpanded = true,
                onDismiss = {},
                createItemCallbacks = CreateItemCallbacks()
            )
        }
    }
}
