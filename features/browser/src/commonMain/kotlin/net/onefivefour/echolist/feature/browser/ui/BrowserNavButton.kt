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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
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
    val buttonModifier = modifier.height(if (isDesktop) 48.dp else 54.dp).clip(EchoListTheme.shapes.medium)
        .background(containerColor).clickable(onClick = onClick).testTag("nav-$label")
    if (isDesktop) {
        Row(buttonModifier.fillMaxWidth().padding(horizontal = EchoListTheme.dimensions.m),
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.size(19.dp), tint = contentColor)
            Text(label, Modifier.weight(1f), style = EchoListTheme.typography.bodyMedium.copy(letterSpacing = 0.sp, fontSize = 14.sp), color = contentColor)
            if (hasDueBadge) {
                // TODO backend: supply the global due-task count.
                Box(Modifier.size(22.dp).background(EchoListTheme.echoListColorScheme.taskColor, CircleShape), contentAlignment = Alignment.Center) {
                    Text("4", style = EchoListTheme.typography.labelSmall.copy(letterSpacing = 0.sp, fontSize = 10.sp), color = colors.onPrimary)
                }
            }
        }
    } else {
        Column(buttonModifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(painterResource(icon), null, Modifier.size(21.dp), tint = contentColor)
            Text(label, Modifier.padding(top = 2.dp), style = EchoListTheme.typography.labelSmall.copy(letterSpacing = 0.sp, fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold), color = contentColor)
        }
    }
}
