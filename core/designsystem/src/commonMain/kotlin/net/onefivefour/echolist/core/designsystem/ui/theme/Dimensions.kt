package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    val iconSmall: Dp = 36.dp,
    val iconMedium: Dp = 40.dp,
    val borderWidth: Dp = 1.dp,
    val formFieldMinHeight: Dp = 52.dp,
    val formFieldLeadingIconWidth: Dp = 22.dp,
    val buttonMinHeight: Dp = 52.dp,
    val buttonPaddingStart: Dp = 21.dp,
    val buttonPaddingEnd: Dp = 14.dp
)

val LocalDimensions = staticCompositionLocalOf { Dimensions() }
