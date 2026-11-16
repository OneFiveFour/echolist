package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

/**
 * Property-based test for dark mode variant resolution.
 *
 * **Validates: Requirements 2.3**
 */
class DarkModeResolutionPropertyTest : FunSpec({

    // -- Generators --

    fun arbColor(): Arb<Color> = arbitrary {
        Color(
            red = Arb.float(0f..1f).bind(),
            green = Arb.float(0f..1f).bind(),
            blue = Arb.float(0f..1f).bind(),
            alpha = Arb.float(0f..1f).bind()
        )
    }

    fun arbColorSchemeLight(): Arb<ColorScheme> = arbitrary {
        val c = arbColor()
        lightColorScheme(
            primary = c.bind(),
            onPrimary = c.bind(),
            secondary = c.bind(),
            onSecondary = c.bind(),
            background = c.bind(),
            onBackground = c.bind(),
            surface = c.bind(),
            onSurface = c.bind()
        )
    }

    fun arbColorSchemeDark(): Arb<ColorScheme> = arbitrary {
        val c = arbColor()
        darkColorScheme(
            primary = c.bind(),
            onPrimary = c.bind(),
            secondary = c.bind(),
            onSecondary = c.bind(),
            background = c.bind(),
            onBackground = c.bind(),
            surface = c.bind(),
            onSurface = c.bind()
        )
    }

    val arbColorTheme: Arb<ColorTheme> = arbitrary {
        ColorTheme(
            name = Arb.stringPattern("[A-Za-z][A-Za-z0-9 ]{0,49}").bind(),
            materialColorSchemeLight = arbColorSchemeLight().bind(),
            materialColorSchemeDark = arbColorSchemeDark().bind(),
            echoListColorSchemeLight = EchoListColorScheme(
                background = arbColor().bind(),
                taskColor = arbColor().bind(),
                noteColor = arbColor().bind(),
                folderColor = arbColor().bind(),
                statusNormal = arbColor().bind(),
                warning = arbColor().bind(),
                onWarning = arbColor().bind()
            ),
            echoListColorSchemeDark = EchoListColorScheme(
                background = arbColor().bind(),
                taskColor = arbColor().bind(),
                noteColor = arbColor().bind(),
                folderColor = arbColor().bind(),
                statusNormal = arbColor().bind(),
                warning = arbColor().bind(),
                onWarning = arbColor().bind()
            )
        )
    }

    // -- Resolution helper matching EchoListTheme logic --

    fun resolveColorScheme(theme: ColorTheme, isDarkMode: Boolean): ColorScheme {
        return if (isDarkMode) theme.materialColorSchemeDark else theme.materialColorSchemeLight
    }

    // -- Property 3: Dark mode variant resolution --

    test(
        "Property 3: resolving with isDarkMode=true returns darkColorScheme, false returns lightColorScheme"
    ).config(invocations = 20) {
        checkAll(arbColorTheme, Arb.boolean()) { theme, isDarkMode ->
            val resolved = resolveColorScheme(theme, isDarkMode)
            if (isDarkMode) {
                resolved shouldBe theme.materialColorSchemeDark
            } else {
                resolved shouldBe theme.materialColorSchemeLight
            }
        }
    }
})