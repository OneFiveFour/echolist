package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.login_introduction_accent
import net.onefivefour.echolist.feature.login.resources.login_introduction_description
import net.onefivefour.echolist.feature.login.resources.login_introduction_eyebrow
import net.onefivefour.echolist.feature.login.resources.login_introduction_footer
import net.onefivefour.echolist.feature.login.resources.login_introduction_heading
import org.jetbrains.compose.resources.stringResource

/** This editorial sidebar is the explicitly approved exception to the semantic color/type tokens. */
@Composable
internal fun LoginIntroduction(modifier: Modifier = Modifier) {
    val isDarkTheme = EchoListTheme.isDarkTheme
    val scale = EchoListTheme.typeScale
    BoxWithConstraints(
        modifier.background(if (isDarkTheme) Color(0xFF172D2A) else Color(0xFF244B46))
            .clipToBounds().semantics { testTag = "login-introduction" }
    ) {
        // Fluid sizing inside the Desktop mode keeps the two-pane design usable throughout the Expanded width class.
        val factor = (maxWidth.value / 633.6f).coerceIn(0.5f, 1f)
        val inset = 72.dp * factor
        Column(
            Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = inset)
                .offset(y = 12.dp * factor),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                stringResource(Res.string.login_introduction_eyebrow),
                style = scale.label.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.44.sp),
                color = Color(0xFFA8D6CE)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(22.dp))
            val headingStyle = scale.display.copy(
                fontSize = (72 * factor).sp,
                lineHeight = (70.567f * factor).sp,
                letterSpacing = (-4.32f * factor).sp
            )
            val heading = stringResource(Res.string.login_introduction_heading)
            val accent = stringResource(Res.string.login_introduction_accent)
            Text(
                buildAnnotatedString {
                    append(heading)
                    append("\n")
                    withStyle(SpanStyle(color = Color(0xFF91D5C9))) { append(accent) }
                },
                style = headingStyle,
                color = if (isDarkTheme) Color(0xFFE8ECE9) else Color(0xFFF4F8F6)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(28.dp))
            Text(
                stringResource(Res.string.login_introduction_description),
                modifier = Modifier.fillMaxWidth(0.94f),
                style = scale.body.copy(lineHeight = 25.sp, letterSpacing = 0.sp),
                color = Color(0xFFC6D7D2)
            )
        }
        Text(
            stringResource(Res.string.login_introduction_footer),
            Modifier.align(Alignment.BottomStart).padding(start = inset, bottom = inset),
            style = scale.label.copy(
                fontSize = 11.sp, lineHeight = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, letterSpacing = 0.88.sp
            ),
            color = Color(0xFFA9BFBA)
        )
        IntroductionDecoration(
            Modifier.align(Alignment.BottomEnd).offset(x = 52.dp * factor, y = (-46.3).dp * factor)
                .size(290.dp * factor),
            factor
        )
    }
}

/** Native shapes from the Figma sidebar (rings, connectors and three entity tiles). */
@Composable
private fun IntroductionDecoration(modifier: Modifier, factor: Float) {
    Box(modifier.graphicsLayer { alpha = 0.75f }.clearAndSetSemantics { }) {
        Canvas(Modifier.matchParentSize()) {
            val unit = size.width / 290f
            val ring = Color(0xFFB5D7D0).copy(alpha = 0.16f)
            drawCircle(ring, radius = 144.5f * unit, style = Stroke(unit))
            drawCircle(ring, radius = 98.5f * unit, style = Stroke(unit))
            drawLine(
                Color(0xFFB5D7D0).copy(alpha = 0.22f),
                Offset(47.2f, 199.75f) * unit, Offset(192.8f, 86.47f) * unit, strokeWidth = unit
            )
        }
        for ((index, point) in listOf(18f to 76f, 201f to 37f, 179f to 204f).withIndex()) {
            Box(
                Modifier.offset((point.first * factor).dp, (point.second * factor).dp)
                    .size(46.dp * factor)
                    .shadow(10.dp * factor, RoundedCornerShape(15.dp * factor))
                    .background(Color(0xFF2C544E), RoundedCornerShape(15.dp * factor))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(15.dp * factor)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(17.dp * factor)) {
                    val u = size.width / 17f
                    when (index) {
                        0 -> {
                            drawRoundRect(Color(0xFFFFB56B), Offset(0f, 2f) * u, Size(17f, 13f) * u, CornerRadius(3f * u))
                            drawRoundRect(Color(0xFFFFB56B), Offset(0f, -1f) * u, Size(8f, 5f) * u, CornerRadius(2f * u))
                        }
                        1 -> {
                            val color = Color(0xFFCBB2FF)
                            drawRoundRect(color, Offset(u, u), Size(15f, 15f) * u, CornerRadius(3f * u), style = Stroke(2f * u))
                            drawLine(color, Offset(5f, 7f) * u, Offset(13f, 7f) * u, 2f * u)
                            drawLine(color, Offset(5f, 11f) * u, Offset(13f, 11f) * u, 2f * u)
                        }
                        else -> {
                            val color = Color(0xFF72D5C7)
                            drawCircle(color, 7.5f * u, style = Stroke(2f * u))
                            val check = Path().apply {
                                moveTo(4.6f * u, 8.5f * u)
                                lineTo(7.4f * u, 11.3f * u)
                                lineTo(12.4f * u, 6.3f * u)
                            }
                            drawPath(check, color, style = Stroke(2f * u))
                        }
                    }
                }
            }
        }
    }
}
