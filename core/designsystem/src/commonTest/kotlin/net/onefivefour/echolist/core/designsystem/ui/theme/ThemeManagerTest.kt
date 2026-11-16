package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for ThemeManager edge cases.
 *
 * **Validates: Requirements 2.1, 2.2**
 */
class ThemeManagerTest : FunSpec({

    val defaultEchoListColors = EchoListColorScheme(
        background = Color.White,
        taskColor = Color.White,
        noteColor = Color.White,
        folderColor = Color.White,
        statusNormal = Color.White,
        warning = Color.White,
        onWarning = Color.White
    )

    val themeA = ColorTheme(
        name = "Theme A",
        materialColorSchemeLight = lightColorScheme(primary = Color.Red),
        materialColorSchemeDark = darkColorScheme(primary = Color.Blue),
        echoListColorSchemeLight = defaultEchoListColors,
        echoListColorSchemeDark = defaultEchoListColors
    )

    val themeB = ColorTheme(
        name = "Theme B",
        materialColorSchemeLight = lightColorScheme(primary = Color.Green),
        materialColorSchemeDark = darkColorScheme(primary = Color.Yellow),
        echoListColorSchemeLight = defaultEchoListColors,
        echoListColorSchemeDark = defaultEchoListColors
    )

    val themeNotInList = ColorTheme(
        name = "Unknown",
        materialColorSchemeLight = lightColorScheme(primary = Color.Cyan),
        materialColorSchemeDark = darkColorScheme(primary = Color.Magenta),
        echoListColorSchemeLight = defaultEchoListColors,
        echoListColorSchemeDark = defaultEchoListColors
    )

    test("initialization sets selectedTheme to initial theme") {
        val manager = ThemeManager(
            availableThemes = listOf(themeA, themeB),
            initialTheme = themeB
        )
        manager.selectedTheme.value shouldBe themeB
    }

    test("selecting a theme not in the available list is ignored") {
        val manager = ThemeManager(availableThemes = listOf(themeA, themeB))
        manager.selectTheme(themeNotInList)
        manager.selectedTheme.value shouldBe themeA
    }
})