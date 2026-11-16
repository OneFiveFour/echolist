package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class EchoListColorScheme(
    val background: Color,
    val taskColor: Color,
    val noteColor: Color,
    val folderColor: Color,
    val statusNormal: Color,
    val warning: Color,
    val onWarning: Color
)

val LocalEchoListColors = staticCompositionLocalOf {
    EchoListColorScheme(
        background = Color.Unspecified,
        taskColor = Color.Unspecified,
        noteColor = Color.Unspecified,
        folderColor = Color.Unspecified,
        statusNormal = Color.Unspecified,
        warning = Color.Unspecified,
        onWarning = Color.Unspecified
    )
}
