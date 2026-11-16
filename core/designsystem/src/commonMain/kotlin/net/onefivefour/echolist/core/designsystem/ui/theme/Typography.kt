package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.resources.Res
import net.onefivefour.echolist.core.designsystem.resources.space_grotesk_600
import net.onefivefour.echolist.core.designsystem.resources.space_grotesk_620
import net.onefivefour.echolist.core.designsystem.resources.space_grotesk_650
import net.onefivefour.echolist.core.designsystem.resources.work_sans_bold
import net.onefivefour.echolist.core.designsystem.resources.work_sans_medium
import net.onefivefour.echolist.core.designsystem.resources.work_sans_regular
import net.onefivefour.echolist.core.designsystem.resources.work_sans_semibold
import org.jetbrains.compose.resources.Font

val WorkSansFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.work_sans_regular, FontWeight.Normal),
        Font(Res.font.work_sans_medium, FontWeight.Medium),
        Font(Res.font.work_sans_semibold, FontWeight.SemiBold),
        Font(Res.font.work_sans_bold, FontWeight.Bold)
    )

val SpaceGroteskFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.space_grotesk_600, FontWeight.SemiBold),
        Font(Res.font.space_grotesk_620, FontWeight(620)),
        Font(Res.font.space_grotesk_650, FontWeight(650))
    )

/** The six semantic levels from Figma, independently replaceable from the color theme. */
data class EchoListTypography(
    val display: TextStyle,
    val titleLarge: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle
) {
    /** All Material slots resolve to foundation levels; none use platform default fonts. */
    fun toMaterialTypography(): Typography = Typography(
        displayLarge = display,
        displayMedium = display,
        displaySmall = display,
        headlineLarge = titleLarge,
        headlineMedium = titleLarge,
        headlineSmall = titleLarge,
        titleLarge = titleLarge,
        titleMedium = title,
        titleSmall = title,
        bodyLarge = body,
        bodyMedium = body,
        bodySmall = bodySmall,
        labelLarge = label,
        labelMedium = label,
        labelSmall = label
    )
}

/** Sizes, line heights, weights and tracking from Figma node 6:2192. */
@Composable
fun echoListTypography(): EchoListTypography {
    val headingFamily = SpaceGroteskFontFamily
    val textFamily = WorkSansFontFamily
    fun style(family: FontFamily, weight: Int, size: Int, height: Int, tracking: Float) = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight(weight),
        fontSynthesis = FontSynthesis.None,
        fontSize = size.sp,
        lineHeight = height.sp,
        letterSpacing = tracking.sp
    )
    return EchoListTypography(
        display = style(headingFamily, 650, 40, 44, -1f),
        titleLarge = style(headingFamily, 620, 28, 34, -0.7f),
        title = style(headingFamily, 600, 20, 26, -0.5f),
        body = style(textFamily, 400, 16, 24, -0.4f),
        bodySmall = style(textFamily, 400, 14, 20, -0.35f),
        label = style(textFamily, 600, 13, 16, 0.52f)
    )
}

@Composable
fun material3Typography(): Typography = echoListTypography().toMaterialTypography()

internal val LocalEchoListTypography = staticCompositionLocalOf<EchoListTypography> {
    error("EchoListTypography not provided")
}

internal val LocalTypography = staticCompositionLocalOf<Typography> {
    error("Typography not provided")
}
