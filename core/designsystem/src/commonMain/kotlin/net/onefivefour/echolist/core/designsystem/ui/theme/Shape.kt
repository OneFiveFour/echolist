package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

internal fun material3Shapes() = Shapes(
    small = RoundedCornerShape(Dimensions().s),
    medium = RoundedCornerShape(Dimensions().m),
    large = RoundedCornerShape(15.dp),
    extraLarge = RoundedCornerShape(17.dp)
)

internal val LocalShapes = staticCompositionLocalOf { material3Shapes() }