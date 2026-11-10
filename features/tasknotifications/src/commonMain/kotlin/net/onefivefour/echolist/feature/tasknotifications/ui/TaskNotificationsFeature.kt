package net.onefivefour.echolist.feature.tasknotifications.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTag
import net.onefivefour.echolist.core.designsystem.ui.components.relativeDueDateText
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.tasks.domain.DueDateUrgencyCalculator
import net.onefivefour.echolist.core.tasks.domain.model.DueDateUrgency
import net.onefivefour.echolist.feature.tasknotifications.resources.Res
import net.onefivefour.echolist.feature.tasknotifications.resources.retry
import net.onefivefour.echolist.feature.tasknotifications.resources.task_notifications_empty
import net.onefivefour.echolist.feature.tasknotifications.resources.task_notifications_load_error
import net.onefivefour.echolist.feature.tasknotifications.resources.task_notifications_permission_denied
import net.onefivefour.echolist.feature.tasknotifications.resources.task_notifications_save_error
import net.onefivefour.echolist.feature.tasknotifications.resources.task_notifications_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@Composable
fun TaskNotificationsFeature() {
    val viewModel = koinViewModel<TaskNotificationsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TaskNotifications(
        state = state,
        onToggle = viewModel::setEnabled,
        onRetry = viewModel::load
    )
}

@Composable
internal fun TaskNotifications(
    state: TaskNotificationsUiState,
    onToggle: (String, Boolean) -> Unit,
    onRetry: () -> Unit
) {
    val today by produceState(Clock.System.todayIn(TimeZone.currentSystemDefault())) {
        while (true) {
            value = Clock.System.todayIn(TimeZone.currentSystemDefault())
            delay(1.minutes)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.l)
    ) {
        Text(
            text = stringResource(Res.string.task_notifications_title),
            style = EchoListTheme.typography.titleLarge,
            color = EchoListTheme.materialColors.primary
        )
        state.error?.let { error ->
            Text(
                text = stringResource(when (error) {
                    NotificationError.Load -> Res.string.task_notifications_load_error
                    NotificationError.Save -> Res.string.task_notifications_save_error
                    NotificationError.Permission -> Res.string.task_notifications_permission_denied
                }),
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.error
            )
            if (error == NotificationError.Load) {
                TextButton(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
            }
        }
        if (state.isLoading) {
            CircularProgressIndicator(color = EchoListTheme.materialColors.primary)
        } else if (state.tasks.isEmpty() && state.error == null) {
            Text(
                text = stringResource(Res.string.task_notifications_empty),
                style = EchoListTheme.typography.bodyMedium,
                color = EchoListTheme.materialColors.onBackground
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.tasks, key = { it.taskId }) { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = EchoListTheme.dimensions.s),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.l)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
                        ) {
                            Text(
                                text = task.title,
                                style = EchoListTheme.typography.bodyMedium,
                                color = EchoListTheme.materialColors.onBackground
                            )
                            val (backgroundColor, contentColor) = when (
                                DueDateUrgencyCalculator.computeUrgency(task.dueDate, today)
                            ) {
                                DueDateUrgency.Normal -> EchoListTheme.materialColors.surfaceVariant to
                                    EchoListTheme.materialColors.onSurfaceVariant
                                DueDateUrgency.Warning -> EchoListTheme.echoListColorScheme.warning to
                                    EchoListTheme.echoListColorScheme.onWarning
                                DueDateUrgency.Overdue -> EchoListTheme.materialColors.error to
                                    EchoListTheme.materialColors.onError
                            }
                            DueDateTag(
                                label = relativeDueDateText(task.dueDate, today),
                                isRecurring = task.isRecurring,
                                backgroundColor = backgroundColor,
                                contentColor = contentColor
                            )
                        }
                        Switch(
                            checked = task.taskId in state.enabledTaskIds,
                            onCheckedChange = { onToggle(task.taskId, it) },
                            enabled = task.taskId !in state.currentlyChangingTaskIds,
                            modifier = Modifier.semantics { contentDescription = task.title }
                        )
                    }
                }
            }
        }
    }
}
