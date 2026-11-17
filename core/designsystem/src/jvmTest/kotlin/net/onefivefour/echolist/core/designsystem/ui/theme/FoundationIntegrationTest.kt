package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.renderComposeScene
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
    test("Material roles absent from Figma remain magenta in both modes, including future roles") {
        val definedRoles = setOf(
            "primary", "onPrimary", "secondary", "onSecondary", "background", "onBackground",
            "surface", "onSurface", "surfaceVariant", "onSurfaceVariant", "error", "onError",
            "outline", "outlineVariant"
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
                            val scale = EchoListTheme.echoListTypography
                            val material = EchoListTheme.typography
                            material.displayLarge shouldBe scale.display
                            material.titleLarge shouldBe scale.titleLarge
                            material.titleMedium shouldBe scale.title
                            material.bodyMedium shouldBe scale.body
                            material.bodySmall shouldBe scale.bodySmall
                            material.labelMedium shouldBe scale.label
                            for ((text, style) in listOf(
                                "A clearer day" to scale.display,
                                "Plans and thoughts" to scale.titleLarge,
                                "Weekend errands" to scale.title,
                                "Keep the details close, calm, and easy to scan." to scale.body,
                                "Updated a few minutes ago · Äöü ß" to scale.bodySmall,
                                "DUE TOMORROW" to scale.label
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
