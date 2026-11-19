package net.onefivefour.echolist.feature.login.ui.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.login.ui.LoginFormLayout

@Composable
internal fun desktopLoginFormLayout(): LoginFormLayout = LoginFormLayout(
    padding = PaddingValues(
        horizontal = EchoListTheme.dimensions.xxxl,
        vertical = EchoListTheme.dimensions.xxl
    ),
    arrangement = Arrangement.Center
)
