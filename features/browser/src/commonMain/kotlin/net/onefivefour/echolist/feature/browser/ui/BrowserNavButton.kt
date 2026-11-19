package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BrowserNavButton(
    label: String,
    icon: DrawableResource,
    isSelected: Boolean,
    isDesktop: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasDueBadge: Boolean = false
) {
    val colors = EchoListTheme.materialColors
    val contentColor = if (isSelected) { if (isDesktop) colors.onPrimary else colors.primary } else colors.onSurfaceVariant
    val containerColor = if (isSelected) colors.primary.copy(alpha = if (isDesktop) 1f else 0.1f) else colors.surface
    val buttonModifier = modifier.height(EchoListTheme.dimensions.buttonMinHeight).clip(EchoListTheme.shapes.medium)
        .background(containerColor).clickable(onClick = onClick).testTag("nav-$label")
    if (isDesktop) {
        Row(buttonModifier.fillMaxWidth().padding(horizontal = EchoListTheme.dimensions.m),
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.size(EchoListTheme.dimensions.xl), tint = contentColor)
            Text(label, Modifier.weight(1f), style = EchoListTheme.typography.labelLarge, color = contentColor)
            if (hasDueBadge) {
                // TODO backend: supply the global due-task count.
                Box(Modifier.size(EchoListTheme.dimensions.xl).background(EchoListTheme.echoListColorScheme.taskColor, CircleShape), contentAlignment = Alignment.Center) {
                    Text("4", style = EchoListTheme.typography.labelSmall, color = colors.onPrimary)
                }
            }
        }
    } else {
        Column(buttonModifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(painterResource(icon), null, Modifier.size(EchoListTheme.dimensions.xl), tint = contentColor)
            Text(label, Modifier.padding(top = EchoListTheme.dimensions.xxs), style = EchoListTheme.typography.labelLarge, color = contentColor)
        }
    }
}

@PreviewComponentLightDark
@Composable
private fun BrowserNavButtonPreview() {
    EchoListTheme(modifier = Modifier) {
        BrowserNavButton(
            label = "Home",
            icon = Res.drawable.browser_home,
            isSelected = true,
            isDesktop = false,
            onClick = {})
    }
}
