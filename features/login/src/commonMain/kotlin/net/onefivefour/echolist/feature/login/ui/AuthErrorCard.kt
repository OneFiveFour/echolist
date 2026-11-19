package net.onefivefour.echolist.feature.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.echolist.core.designsystem.ui.preview.PreviewComponentLightDark
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.session.domain.AuthError
import net.onefivefour.echolist.feature.login.resources.Res
import net.onefivefour.echolist.feature.login.resources.error_auth_invalid_credentials
import net.onefivefour.echolist.feature.login.resources.error_auth_invalid_credentials_description
import net.onefivefour.echolist.feature.login.resources.error_auth_network
import net.onefivefour.echolist.feature.login.resources.error_auth_network_description
import net.onefivefour.echolist.feature.login.resources.error_auth_server
import net.onefivefour.echolist.feature.login.resources.error_auth_server_description
import net.onefivefour.echolist.feature.login.resources.error_auth_unknown
import net.onefivefour.echolist.feature.login.resources.error_auth_unknown_description
import org.jetbrains.compose.resources.stringResource

/** Presentation text for the existing authentication error types; validation stays in the ViewModel. */
@Composable
internal fun authErrorMessage(error: AuthError): String {
    val (title, description) = when (error) {
        is AuthError.InvalidCredentials -> {
            stringResource(Res.string.error_auth_invalid_credentials) to
                stringResource(Res.string.error_auth_invalid_credentials_description)
        }
        is AuthError.ServerError -> {
            stringResource(Res.string.error_auth_server) to
                stringResource(Res.string.error_auth_server_description)
        }
        is AuthError.NetworkError -> {
            stringResource(Res.string.error_auth_network) to
                stringResource(Res.string.error_auth_network_description)
        }
        is AuthError.Unknown -> {
            stringResource(Res.string.error_auth_unknown) to
                stringResource(Res.string.error_auth_unknown_description)
        }
    }

    return "$title. $description"
}

@Composable
internal fun LoginErrorSummary(messages: List<String>, modifier: Modifier = Modifier) {
    val colors = EchoListTheme.materialColors
    Row(
        modifier.fillMaxWidth().heightIn(min = 36.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            Modifier.size(20.dp).background(colors.error, CircleShape)
                .semantics { testTag = "login-error-icon" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "!",
                style = EchoListTheme.echoListTypography.label.copy(
                    fontSize = 11.sp, lineHeight = 16.5.sp,
                    letterSpacing = 0.sp, fontWeight = FontWeight.Bold
                ),
                color = colors.onError
            )
        }
        Text(
            if (messages.size == 1) messages.single() else messages.joinToString("\n") { "• $it" },
            style = EchoListTheme.echoListTypography.bodySmall.copy(
                fontSize = 12.sp, lineHeight = 17.sp,
                letterSpacing = 0.sp, fontWeight = FontWeight.Medium
            ),
            color = colors.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@PreviewComponentLightDark
@Composable
private fun LoginErrorSummaryPreview() {
    EchoListTheme(modifier = Modifier) {
        LoginErrorSummary(listOf("Please enter a server address.", "Please enter your username."))
    }
}
