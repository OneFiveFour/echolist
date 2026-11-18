package net.onefivefour.echolist.feature.login.ui.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.feature.login.ui.LoginFormLayout

internal fun phoneLoginFormLayout(): LoginFormLayout = LoginFormLayout(
    padding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 56.dp),
    arrangement = Arrangement.Bottom
)
