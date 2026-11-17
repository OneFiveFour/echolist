package net.onefivefour.echolist.core.designsystem.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeManager(
    val availableThemes: List<ColorTheme>,
    initialTheme: ColorTheme = availableThemes.first()
) {
    val selectedTheme: StateFlow<ColorTheme>
        field = MutableStateFlow(initialTheme)

    fun selectTheme(theme: ColorTheme) {
        if (theme in availableThemes) {
            selectedTheme.value = theme
        }
    }
}