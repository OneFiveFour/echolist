@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import javax.swing.SwingUtilities
import net.onefivefour.echolist.core.designsystem.ui.components.EchoListBackground
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.designsystem.ui.theme.ThemeManager
import net.onefivefour.echolist.core.designsystem.ui.theme.colorscheme.EchoListDefaultTheme
import net.onefivefour.echolist.core.session.domain.AuthError

class LoginLayoutTest : FunSpec({
    test("Light and Dark layouts render from phone to tablet and desktop, including errors and loading") {
        SwingUtilities.invokeAndWait {
            for (isDarkTheme in listOf(false, true)) {
                for ((name, width, height, state, fontScale) in listOf(
                    RenderCase("phone", 402, 874, LoginUiState(backendUrl = "")),
                    RenderCase("desktop", 1440, 1158, LoginUiState(backendUrl = "")),
                    RenderCase("compact-upper", 599, 874, LoginUiState()),
                    RenderCase("medium", 600, 874, LoginUiState()),
                    RenderCase("former-breakpoint", 720, 874, LoginUiState()),
                    RenderCase("medium-upper", 839, 874, LoginUiState()),
                    RenderCase("tablet", 840, 874, LoginUiState()),
                    RenderCase("short", 402, 420, LoginUiState()),
                    RenderCase("narrow", 320, 874, LoginUiState()),
                    RenderCase("large-text", 402, 1000, LoginUiState(), 1.5f),
                    RenderCase("field-error", 402, 874, LoginUiState(passwordError = "Password is required")),
                    RenderCase("auth-error", 840, 600, LoginUiState(authError = AuthError.InvalidCredentials("Invalid credentials"))),
                    RenderCase("loading", 402, 874, LoginUiState(isLoading = true))
                )) {
                    ImageComposeScene(width, height, density = Density(1f, fontScale)) {
                        EchoListTheme(isDarkTheme = isDarkTheme, themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                            EchoListBackground { LoginScreen(state, {}, {}, {}, {}) }
                        }
                    }.use { scene ->
                        scene.render().use { image ->
                            val target = File("build/login-$name-${if (isDarkTheme) "dark" else "light"}.png")
                            target.parentFile.mkdirs()
                            target.writeBytes(requireNotNull(image.encodeToData()).bytes)
                        }
                        val nodes = scene.nodes()
                        (nodes.tagged("login-introduction") != null) shouldBe (width >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
                        val button = requireNotNull(nodes.tagged("login-connect"))
                        (button.config.getOrNull(SemanticsProperties.Disabled) != null) shouldBe state.isLoading
                        val errors = nodes.tagged("login-errors")
                        (errors != null) shouldBe (state.passwordError != null || state.authError != null)
                        if (errors != null) {
                            val server = requireNotNull(nodes.tagged("login-server"))
                            (errors.boundsInRoot.bottom <= server.boundsInRoot.top) shouldBe true
                            nodes.count { it.config.getOrNull(SemanticsProperties.TestTag) == "login-error-icon" } shouldBe 1
                        }
                        if (name != "short") {
                            (button.boundsInRoot.bottom <= height) shouldBe true
                            (button.boundsInRoot.left >= 0) shouldBe true
                            (button.boundsInRoot.right <= width) shouldBe true
                        }
                        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                        nodes.forEach { node ->
                            if (node.config.getOrNull(SemanticsProperties.EditableText) == null) {
                                node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
                            }
                        }
                        results.forEach {
                            // Skia may round a fractional line box down by less than one raster pixel.
                            check((0 until it.lineCount).all { line ->
                                it.getLineRight(line) <= it.size.width + 1f &&
                                    it.getLineBottom(line) <= it.size.height + 1f
                            }) {
                                "$name: text '${it.layoutInput.text}' overflows ${it.size}, paragraph height ${it.multiParagraph.height}; constraints ${it.layoutInput.constraints}"
                            }
                        }
                    }
                }
            }
        }
    }

    test("one error summary and loading keep the header, inputs and primary action anchored") {
        SwingUtilities.invokeAndWait {
            for (isDarkTheme in listOf(false, true)) {
                for ((width, height, fontScale) in listOf(
                    Triple(402, 874, 1f), Triple(840, 874, 1f),
                    Triple(1440, 1158, 1f), Triple(402, 1000, 1.5f)
                )) {
                    val state = mutableStateOf(LoginUiState())
                    ImageComposeScene(width, height, density = Density(1f, fontScale)) {
                        EchoListTheme(isDarkTheme = isDarkTheme, themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                            EchoListBackground { LoginScreen(state.value, {}, {}, {}, {}) }
                        }
                    }.use { scene ->
                        scene.render().close()
                        val anchors = listOf("login-welcome", "login-heading", "login-server", "login-username", "login-password", "login-connect")
                        val restingBounds = anchors.associateWith { requireNotNull(scene.nodes().tagged(it)).boundsInRoot }
                        for (nextState in listOf(
                            LoginUiState(backendUrlError = "Server address is required", usernameError = "Username is required", passwordError = "Password is required"),
                            LoginUiState(authError = AuthError.NetworkError("Unavailable")),
                            LoginUiState(isLoading = true),
                            LoginUiState()
                        )) {
                            state.value = nextState
                            androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
                            scene.render().use { image ->
                                val buttonBounds = requireNotNull(scene.nodes().tagged("login-connect")).boundsInRoot
                                val rendered = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(requireNotNull(image.encodeToData()).bytes))
                                val expectedColor = if (isDarkTheme) EchoListDefaultTheme.materialColorSchemeDark.primary
                                    else EchoListDefaultTheme.materialColorSchemeLight.primary
                                rendered.getRGB((buttonBounds.left + 10).toInt(), buttonBounds.center.y.toInt()) shouldBe expectedColor.toArgb()
                            }
                            val nodes = scene.nodes()
                            anchors.forEach { tag -> requireNotNull(nodes.tagged(tag)).boundsInRoot shouldBe restingBounds[tag] }
                            val hasErrors = nextState.backendUrlError != null || nextState.authError != null
                            nodes.count { it.config.getOrNull(SemanticsProperties.TestTag) == "login-error-icon" } shouldBe if (hasErrors) 1 else 0
                            if (nextState.backendUrlError != null) {
                                val summary = nodes.mapNotNull { it.config.getOrNull(SemanticsProperties.Text)?.joinToString { text -> text.text } }
                                    .single { it.contains("Server address is required") }
                                summary.contains("Username is required") shouldBe true
                                summary.contains("Password is required") shouldBe true
                            }
                            (requireNotNull(nodes.tagged("login-connect")).config.getOrNull(SemanticsProperties.Disabled) != null) shouldBe nextState.isLoading
                            if (nextState.isLoading) {
                                nodes.count { it.config.getOrNull(SemanticsProperties.ProgressBarRangeInfo) != null } shouldBe 1
                            }
                        }
                    }
                }
            }
        }
    }

    test("resizing across official size classes keeps edits, password visibility and form focus") {
        SwingUtilities.invokeAndWait {
            val state = mutableStateOf(LoginUiState(password = "secret"))
            var loginClicks = 0
            val windowInfo = TestWindowInfo(IntSize(599, 874))
            ImageComposeScene(1600, 874) {
                CompositionLocalProvider(LocalWindowInfo provides windowInfo) {
                    EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                        LoginScreen(
                            state.value,
                            { state.value = state.value.copy(backendUrl = it) },
                            { state.value = state.value.copy(username = it) },
                            { state.value = state.value.copy(password = it) },
                            { loginClicks++ }
                        )
                    }
                }
            }.use { scene ->
                scene.constraints = Constraints(maxWidth = 599, maxHeight = 874)
                scene.render().close()
                requireNotNull(scene.nodes().tagged("login-username"))
                    .config[SemanticsActions.SetText].action!!.invoke(AnnotatedString("alice"))
                requireNotNull(scene.nodes().tagged("login-visibility"))
                    .config[SemanticsActions.OnClick].action!!.invoke()
                requireNotNull(scene.nodes().tagged("login-password"))
                    .config[SemanticsActions.RequestFocus].action!!.invoke()
                scene.render().close()
                for (width in listOf(600, 839, 840, 1200, 1600, 599)) {
                    // A native resize changes both WindowInfo and the scene's render constraints.
                    windowInfo.containerSize = IntSize(width, 874)
                    scene.constraints = Constraints(maxWidth = width, maxHeight = 874)
                    scene.render().close()
                    val nodes = scene.nodes()
                    (nodes.tagged("login-introduction") != null) shouldBe (width >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
                    val password = requireNotNull(nodes.tagged("login-password"))
                    requireNotNull(nodes.tagged("login-visibility"))
                        .config[SemanticsProperties.ContentDescription] shouldBe listOf("Hide password")
                    password.config[SemanticsProperties.Focused] shouldBe true
                    state.value.username shouldBe "alice"
                    state.value.backendUrl shouldBe "https://"
                    state.value.password shouldBe "secret"
                }
                requireNotNull(scene.nodes().tagged("login-connect"))
                    .config[SemanticsActions.OnClick].action!!.invoke()
                loginClicks shouldBe 1
            }
        }
    }

    test("a short viewport can scroll the Connect action into view") {
        SwingUtilities.invokeAndWait {
            ImageComposeScene(402, 420) {
                EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                    LoginScreen(LoginUiState(), {}, {}, {}, {})
                }
            }.use { scene ->
                scene.render().close()
                val scroll = scene.nodes().first { it.config.getOrNull(SemanticsActions.ScrollBy) != null }
                scroll.config[SemanticsActions.ScrollBy].action!!.invoke(0f, 10000f)
                androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
                scene.render(1_000_000L).close()
                val button = requireNotNull(scene.nodes().tagged("login-connect"))
                (button.boundsInRoot.top >= 0) shouldBe true
                (button.boundsInRoot.bottom <= 420) shouldBe true
            }
        }
    }

    test("official adaptive info uses window density and ignores child pane width") {
        SwingUtilities.invokeAndWait {
            var observed: WindowSizeClass? = null
            val windowInfo = TestWindowInfo(IntSize(1680, 1000), density = 2f)
            ImageComposeScene(1680, 1000, density = Density(2f)) {
                CompositionLocalProvider(LocalWindowInfo provides windowInfo) {
                    EchoListTheme(themeManager = ThemeManager(listOf(EchoListDefaultTheme))) {
                        Box(Modifier.size(100.dp)) {
                            observed = EchoListTheme.adaptiveInfo.windowSizeClass
                            Text("Nested pane")
                        }
                    }
                }
            }.use { scene ->
                scene.render().close()
                observed?.minWidthDp shouldBe WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
                windowInfo.containerSize = IntSize(1678, 1000)
                scene.constraints = Constraints(maxWidth = 1678, maxHeight = 1000)
                scene.render().close()
                observed?.minWidthDp shouldBe WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
            }
        }
    }

})

private data class RenderCase(val name: String, val width: Int, val height: Int, val state: LoginUiState, val fontScale: Float = 1f)

private fun ImageComposeScene.nodes(): List<SemanticsNode> {
    fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
    return semanticsOwners.flatMap { descendants(it.unmergedRootSemanticsNode) }
}

private fun List<SemanticsNode>.tagged(tag: String) = firstOrNull {
    it.config.getOrNull(SemanticsProperties.TestTag) == tag
}

/** ImageComposeScene keeps a fixed WindowInfo; this fixture simulates native resize events. */
private class TestWindowInfo(initialSize: IntSize, private val density: Float = 1f) : WindowInfo {
    override val isWindowFocused: Boolean = true
    override var containerSize: IntSize by mutableStateOf(initialSize)
    override val containerDpSize: DpSize
        get() = DpSize((containerSize.width / density).dp, (containerSize.height / density).dp)
}
