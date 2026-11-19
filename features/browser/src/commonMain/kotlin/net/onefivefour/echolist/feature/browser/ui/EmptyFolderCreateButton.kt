package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.core.designsystem.ui.components.ElButton
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.*
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.browser_empty_create
import net.onefivefour.echolist.feature.browser.resources.browser_empty_plus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyFolderCreateButton(createItemCallbacks: CreateItemCallbacks) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box {
        ElButton(
            onClick = { isMenuExpanded = true },
            modifier = Modifier.testTag("browser-empty-create")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                Icon(
                    painterResource(Res.drawable.browser_empty_plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(Res.string.browser_empty_create),
                    style = EchoListTheme.typography.labelLarge
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
private fun EmptyFolderCreateButtonPreview() {
    EchoListTheme(modifier = Modifier) {
        EmptyFolderCreateButton(CreateItemCallbacks())
    }
}
