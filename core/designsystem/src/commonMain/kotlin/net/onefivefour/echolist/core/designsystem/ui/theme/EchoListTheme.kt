package net.onefivefour.echolist.core.designsystem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListDefaultTheme
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

internal val LocalAdaptiveInfo = compositionLocalOf<WindowAdaptiveInfo> {
    error("WindowAdaptiveInfo not provided by EchoListTheme")
}

internal val LocalDarkTheme = androidx.compose.runtime.compositionLocalOf { false }

object EchoListTheme {
    val isDarkTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalDarkTheme.current
    val adaptiveInfo: WindowAdaptiveInfo
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptiveInfo.current
    val echoListColorScheme: EchoListColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalEchoListColors.current
    val materialColors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMaterialColors.current
    val typeScale: EchoListTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalEchoListTypography.current
    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
    val dimensions: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current
    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}

@Composable
fun EchoListTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    themeManager: ThemeManager? = null,
    typeScale: EchoListTypography = echoListTypography(),
    content: @Composable () -> Unit
) {
    val isKoinStarted = runCatching { KoinPlatform.getKoin() }.isSuccess
    val resolvedThemeManager = themeManager ?: if (isKoinStarted) {
        koinInject<ThemeManager>()
    } else {
        // use default theme for previews where we cannot use Koin
        ThemeManager(
            availableThemes = listOf(EchoListDefaultTheme),
            initialTheme = EchoListDefaultTheme
        )
    }
    val colorTheme by resolvedThemeManager.selectedTheme.collectAsState()
    val materialColorScheme = when {
        isDarkTheme -> colorTheme.materialColorSchemeDark
        else -> colorTheme.materialColorSchemeLight
    }
    val echoListColorScheme = when {
        isDarkTheme -> colorTheme.echoListColorSchemeDark
        else -> colorTheme.echoListColorSchemeLight
    }

    val typography = typeScale.toMaterialTypography()
    val adaptiveInfo = currentWindowAdaptiveInfoV2()

    CompositionLocalProvider(
        LocalAdaptiveInfo provides adaptiveInfo,
        LocalDarkTheme provides isDarkTheme,
        LocalEchoListTypography provides typeScale,
        LocalTypography provides typography,
        LocalMaterialColors provides materialColorScheme,
        LocalEchoListColors provides echoListColorScheme
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = typography,
            shapes = EchoListTheme.shapes,
            content = content
        )
    }
}