package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
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

@Composable
fun material3Typography(): Typography {
    val headingFamily = SpaceGroteskFontFamily
    val textFamily = WorkSansFontFamily
    fun style(
        family: FontFamily,
        weight: Int,
        size: Int,
        height: Int,
        tracking: Float
    ) = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight(weight),
        fontSynthesis = FontSynthesis.None,
        fontSize = size.sp,
        lineHeight = height.sp,
        letterSpacing = tracking.sp
    )
    return Typography(
        displayLarge = style(headingFamily, 650, 72, 71, -4.32f),
        displayMedium = style(headingFamily, 650, 52, 56, -2.34f),
        displaySmall = style(headingFamily, 620, 42, 46, -1.89f),
        headlineLarge = style(headingFamily, 620, 38, 44, -1.71f),
        headlineMedium = style(headingFamily, 620, 31, 37, -1.4f),
        headlineSmall = style(headingFamily, 620, 28, 34, -0.7f),
        titleLarge = style(headingFamily, 600, 24, 30, -0.6f),
        titleMedium = style(headingFamily, 600, 20, 26, -0.5f),
        titleSmall = style(textFamily, 600, 15, 20, 0f),
        bodyLarge = style(textFamily, 400, 16, 24, -0.4f),
        bodyMedium = style(textFamily, 400, 15, 22, 0f),
        bodySmall = style(textFamily, 400, 13, 20, 0f),
        labelLarge = style(textFamily, 600, 15, 20, 0f),
        labelMedium = style(textFamily, 600, 12, 16, 0f),
        labelSmall = style(textFamily, 600, 10, 15, 0f)
    )
}
