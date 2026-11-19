package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

private data class BrowserLayoutSpacing(
    val horizontal: Dp,
    val top: Dp,
    val contentGap: Dp
)

/** Keep the content at one composition location when the navigation layout changes. */
@Composable
internal fun BrowserLayout(
    isDesktop: Boolean,
    createItemCallbacks: CreateItemCallbacks,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    toolbar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val dimensions = EchoListTheme.dimensions
    val spacing = if (isDesktop) {
        BrowserLayoutSpacing(dimensions.xxxl, dimensions.xxxl, dimensions.xxl)
    } else {
        BrowserLayoutSpacing(dimensions.xl, dimensions.l, dimensions.xl)
    }
    val hasFloatingActions = isDesktop || canNavigateBack
    val actionCount = if (isDesktop && canNavigateBack) 2 else 1
    // Reserve scrollable space so the last entry can move clear of the floating actions.
    val contentPadding = PaddingValues(
        bottom = if (hasFloatingActions) {
            dimensions.buttonMinHeight * actionCount +
                dimensions.m * (actionCount - 1) + dimensions.xl * 2
        } else {
            dimensions.l
        }
    )

    Box(Modifier.fillMaxSize().padding(horizontal = spacing.horizontal)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth().padding(top = spacing.top, bottom = dimensions.m),
                contentAlignment = Alignment.BottomStart
            ) { toolbar() }
            Spacer(Modifier.height(spacing.contentGap))
            Box(Modifier.weight(1f)) { content(contentPadding) }
        }
        Column(
            Modifier.align(Alignment.BottomEnd).padding(bottom = dimensions.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.m)
        ) {
            if (canNavigateBack) BrowserBackButton(onBack)
            if (isDesktop) CreateItemButton(createItemCallbacks)
        }
    }
}
