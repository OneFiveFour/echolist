package net.onefivefour.echolist.feature.login.ui.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.feature.login.ui.LoginFormLayout

internal fun desktopLoginFormLayout(availableWidth: Dp): LoginFormLayout = LoginFormLayout(
    padding = PaddingValues(
        horizontal = (availableWidth * 0.125f).coerceAtMost(100.8.dp),
        vertical = 36.dp
    ),
    arrangement = Arrangement.Center
)
