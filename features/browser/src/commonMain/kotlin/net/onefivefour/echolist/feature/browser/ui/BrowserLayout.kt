package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import net.onefivefour.echolist.core.designsystem.ui.components.ElBackButton
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

private data class BrowserLayoutSpacing(
    val horizontal: Dp,
    val top: Dp,
    val contentGap: Dp
)

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

    val spacing = when {
        isDesktop -> BrowserLayoutSpacing(
            horizontal = dimensions.xxxl,
            top = dimensions.xxxl,
            contentGap = dimensions.xxl
        )

        else -> BrowserLayoutSpacing(
            horizontal = dimensions.xl - dimensions.xs,
            top = dimensions.l,
            contentGap = dimensions.xl
        )
    }

    val hasFloatingActions = isDesktop || canNavigateBack
    val actionCount = if (isDesktop && canNavigateBack) 2 else 1

    val contentPadding = PaddingValues(
        bottom = when {
            hasFloatingActions -> {
                dimensions.buttonMinHeight * actionCount +
                    dimensions.m * (actionCount - 1) +
                    dimensions.l * 2
            }

            else -> dimensions.l
        }
    )

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.horizontal)
    ) {

        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = spacing.top,
                        bottom = dimensions.m
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                toolbar()
            }
            Spacer(Modifier.height(spacing.contentGap))
            Box(Modifier.weight(1f)) {
                content(contentPadding)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = dimensions.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.m)
        ) {
            if (canNavigateBack) {
                ElBackButton(onBack)
            }
            if (isDesktop) {
                CreateItemButton(createItemCallbacks)
            }
        }
    }
}
