package net.onefivefour.echolist.feature.browser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.feature.browser.resources.Res
import net.onefivefour.echolist.feature.browser.resources.task_peek_empty
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskPeekOverview(
    uiState: TaskPeekUiState,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> LoadingIndicator(modifier = modifier)
        uiState.error != null -> ErrorContainer(message = uiState.error, modifier = modifier)
        uiState.isEmpty -> TaskPeekEmptyState(modifier = modifier)
        else -> TaskPeekList(groups = uiState.groups, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskPeekList(
    groups: List<TaskPeekGroup>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        groups.forEach { group ->
            stickyHeader(key = "header-${group.taskListId}") {
                TaskPeekHeader(title = group.taskListName)
            }
            items(group.tasks, key = { it.mainTaskId }) { row ->
                TaskPeekItemRow(row = row)
            }
        }
    }
}

@Composable
private fun TaskPeekHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EchoListTheme.materialColors.background
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = EchoListTheme.dimensions.m,
                vertical = EchoListTheme.dimensions.s
            ),
            text = title,
            style = EchoListTheme.typography.titleSmall,
            color = EchoListTheme.materialColors.primary
        )
    }
}

@Composable
private fun TaskPeekEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(EchoListTheme.dimensions.l),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.task_peek_empty),
            style = EchoListTheme.typography.bodyMedium,
            color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.6f)
        )
    }
}