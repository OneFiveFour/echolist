package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_plus
import net.onefivefour.echolist.feature.browser.resources.create_item
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CreateItemButton(
    createItemCallbacks: CreateItemCallbacks
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { isMenuExpanded = true },
            shape = EchoListTheme.shapes.extraLarge,
            color = EchoListTheme.materialColors.primary,
            shadowElevation = EchoListTheme.dimensions.m,
            modifier = Modifier.size(EchoListTheme.dimensions.buttonMinHeight).testTag("browser-create")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(resource = Res.drawable.browser_plus),
                    contentDescription = stringResource(Res.string.create_item),
                    modifier = Modifier.size(EchoListTheme.dimensions.xl),
                    tint = EchoListTheme.materialColors.onPrimary
                )
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
            createItemCallbacks = CreateItemCallbacks()
        )
    }
}
