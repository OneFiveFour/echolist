package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.reflect.Modifier as JavaModifier
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListDefaultTheme

class FoundationIntegrationTest : FunSpec({
    test("Undefined Material roles remain magenta in both modes, including future roles") {
        val definedRoles = setOf(
            "primary", "onPrimary", "secondary", "onSecondary", "background", "onBackground",
            "surface", "onSurface", "surfaceVariant", "onSurfaceVariant", "error", "onError",
            "outline", "outlineVariant", "surfaceContainerHigh"
        )
        val roles = ColorScheme::class.java.declaredFields.filter {
            it.type == Long::class.javaPrimitiveType && !JavaModifier.isStatic(it.modifiers)
        }
        (roles.size > definedRoles.size) shouldBe true
        for (scheme in listOf(
            EchoListDefaultTheme.materialColorSchemeLight,
            EchoListDefaultTheme.materialColorSchemeDark
        )) {
            for (role in roles) {
                role.isAccessible = true
                (role.getLong(scheme) == Color.Magenta.value.toLong()) shouldBe
                    (role.name !in definedRoles)
            }
        }
    }

    test("bundled type scale renders in both modes and the app background is solid") {
        SwingUtilities.invokeAndWait {
            for (isDarkTheme in listOf(false, true)) {
                val manager = ThemeManager(listOf(EchoListDefaultTheme))
                val scheme = if (isDarkTheme) EchoListDefaultTheme.materialColorSchemeDark
                    else EchoListDefaultTheme.materialColorSchemeLight
                val image = renderComposeScene(900, 500) {
                    EchoListTheme(isDarkTheme = isDarkTheme, themeManager = manager) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            val material = EchoListTheme.typography
                            material shouldBe MaterialTheme.typography
                            val headingFamily = SpaceGroteskFontFamily
                            val textFamily = WorkSansFontFamily
                            val expectedStyles = listOf(
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(650), fontSynthesis = FontSynthesis.None, fontSize = 72.sp, lineHeight = 71.sp, letterSpacing = (-4.32).sp) to material.displayLarge,
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(650), fontSynthesis = FontSynthesis.None, fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-2.34).sp) to material.displayMedium,
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(620), fontSynthesis = FontSynthesis.None, fontSize = 42.sp, lineHeight = 46.sp, letterSpacing = (-1.89).sp) to material.displaySmall,
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(620), fontSynthesis = FontSynthesis.None, fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-1.71).sp) to material.headlineLarge,
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(620), fontSynthesis = FontSynthesis.None, fontSize = 31.sp, lineHeight = 37.sp, letterSpacing = (-1.4).sp) to material.headlineMedium,
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(620), fontSynthesis = FontSynthesis.None, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.7).sp) to material.headlineSmall,
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(600), fontSynthesis = FontSynthesis.None, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.6).sp) to material.titleLarge,
                                TextStyle(fontFamily = headingFamily, fontWeight = FontWeight(600), fontSynthesis = FontSynthesis.None, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.5).sp) to material.titleMedium,
                                TextStyle(fontFamily = textFamily, fontWeight = FontWeight(600), fontSynthesis = FontSynthesis.None, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = (0).sp) to material.titleSmall,
                                TextStyle(fontFamily = textFamily, fontWeight = FontWeight(400), fontSynthesis = FontSynthesis.None, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = (-0.4).sp) to material.bodyLarge,
                                TextStyle(fontFamily = textFamily, fontWeight = FontWeight(400), fontSynthesis = FontSynthesis.None, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = (0).sp) to material.bodyMedium,
                                TextStyle(fontFamily = textFamily, fontWeight = FontWeight(400), fontSynthesis = FontSynthesis.None, fontSize = 13.sp, lineHeight = 20.sp, letterSpacing = (0).sp) to material.bodySmall,
                                TextStyle(fontFamily = textFamily, fontWeight = FontWeight(600), fontSynthesis = FontSynthesis.None, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = (0).sp) to material.labelLarge,
                                TextStyle(fontFamily = textFamily, fontWeight = FontWeight(600), fontSynthesis = FontSynthesis.None, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = (0).sp) to material.labelMedium,
                                TextStyle(fontFamily = textFamily, fontWeight = FontWeight(600), fontSynthesis = FontSynthesis.None, fontSize = 10.sp, lineHeight = 15.sp, letterSpacing = (0).sp) to material.labelSmall
                            )
                            for ((expected, style) in expectedStyles) {
                                style shouldBe expected
                            }
                            for ((text, style) in listOf(
                                "A clearer day" to material.displayLarge,
                                "Plans and thoughts" to material.titleLarge,
                                "Weekend errands" to material.titleMedium,
                                "Keep the details close, calm, and easy to scan." to material.bodyLarge,
                                "Updated a few minutes ago · Äöü ß" to material.bodySmall,
                                "DUE TOMORROW" to material.labelLarge
                            )) {
                                Text(text, style = style, color = EchoListTheme.materialColors.onBackground)
                            }
                        }
                    }
                }
                image.use {
                    val bytes = requireNotNull(it.encodeToData()).bytes
                    val rendered = ImageIO.read(ByteArrayInputStream(bytes))
                    for ((x, y) in listOf(0 to 0, 899 to 0, 0 to 499, 899 to 499)) {
                        rendered.getRGB(x, y) shouldBe scheme.background.toArgb()
                    }
                    File("build/foundation-${if (isDarkTheme) "dark" else "light"}.png").writeBytes(bytes)
                }
            }
        }
    }
})
