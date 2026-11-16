package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

/**
 * Property-based test for theme selection updates state.
 *
 * **Validates: Requirements 2.1, 2.2**
 */
class ThemeManagerPropertyTest : FunSpec({

    // -- Generators --

    fun arbColor(): Arb<Color> = arbitrary {
        Color(
            red = Arb.float(0f..1f).bind(),
            green = Arb.float(0f..1f).bind(),
            blue = Arb.float(0f..1f).bind(),
            alpha = Arb.float(0f..1f).bind()
        )
    }

    fun arbColorSchemeLight(): Arb<androidx.compose.material3.ColorScheme> = arbitrary {
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

    fun arbColorSchemeDark(): Arb<androidx.compose.material3.ColorScheme> = arbitrary {
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

    // -- Property 2: Theme selection updates state --

    test("Property 2: selecting a theme from the available list updates selectedTheme").config(invocations = 20) {
        checkAll(Arb.list(arbColorTheme, 2..10)) { themes ->
            val manager = ThemeManager(availableThemes = themes)

            // Pick a random theme from the list to select
            val target = Arb.element(themes).bind()
            manager.selectTheme(target)

            manager.selectedTheme.value shouldBe target
        }
    }
})