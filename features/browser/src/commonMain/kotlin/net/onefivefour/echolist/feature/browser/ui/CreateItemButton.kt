package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CreateItemButton(
    createItemCallbacks: CreateItemCallbacks,
    isDesktop: Boolean
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box {
        Surface(onClick = { isMenuExpanded = true }, shape = EchoListTheme.shapes.extraLarge,
            color = EchoListTheme.materialColors.primary, shadowElevation = 12.dp,
            modifier = Modifier.size(if (isDesktop) 58.dp else 52.dp).testTag("browser-create")) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(Res.drawable.browser_plus), stringResource(Res.string.create_item), Modifier.size(24.dp), tint = EchoListTheme.materialColors.onPrimary)
            }
        }
        CreateItemMenu(
            isExpanded = isMenuExpanded,
            onDismiss = { isMenuExpanded = false },
            createItemCallbacks = createItemCallbacks
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateItemButtonPreview() {
    EchoListTheme(modifier = Modifier) {
        CreateItemButton(
            createItemCallbacks = CreateItemCallbacks(),
            isDesktop = false
        )
    }
}
