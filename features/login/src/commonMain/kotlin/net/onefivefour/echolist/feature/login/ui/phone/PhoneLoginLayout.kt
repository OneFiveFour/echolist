package net.onefivefour.echolist.feature.login.ui.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.login.ui.LoginFormLayout

@Composable
internal fun phoneLoginFormLayout(): LoginFormLayout = LoginFormLayout(
    padding = PaddingValues(
        start = EchoListTheme.dimensions.xl,
        end = EchoListTheme.dimensions.xl,
        top = EchoListTheme.dimensions.xl,
        bottom = EchoListTheme.dimensions.xxxl
    ),
    arrangement = Arrangement.Bottom
)
