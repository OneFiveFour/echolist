package net.onefivefour.echolist.feature.taskssummary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.taskssummary.resources.Res
import net.onefivefour.echolist.feature.taskssummary.resources.navigate_back
import net.onefivefour.echolist.feature.taskssummary.resources.tasks_summary_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TasksSummaryFeature(parentDir: String, onBack: () -> Unit) {
    val viewModel = koinViewModel<TasksSummaryViewModel>(key = parentDir) { parametersOf(parentDir) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TasksSummaryScreen(uiState, onBack)
}

@Composable
internal fun TasksSummaryScreen(uiState: TasksSummaryUiState, onBackClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .background(EchoListTheme.materialColors.background)
            .padding(EchoListTheme.dimensions.l)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.navigate_back),
                    tint = EchoListTheme.materialColors.onBackground
                )
            }
            Text(
                text = stringResource(Res.string.tasks_summary_title),
                style = EchoListTheme.typography.titleLarge,
                color = EchoListTheme.materialColors.onBackground
            )
        }
        TasksSummaryOverview(uiState, Modifier.fillMaxWidth().weight(1f))
    }
}
