package net.onefivefour.echolist.feature.reminderssummary.ui

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
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTagForDate
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTagUrgency
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import net.onefivefour.echolist.core.tasks.domain.DueDateUrgencyCalculator
import net.onefivefour.echolist.core.tasks.domain.model.DueDateUrgency
import net.onefivefour.echolist.feature.reminderssummary.resources.Res
import net.onefivefour.echolist.feature.reminderssummary.resources.retry
import net.onefivefour.echolist.feature.reminderssummary.resources.reminders_summary_empty
import net.onefivefour.echolist.feature.reminderssummary.resources.reminders_summary_load_error
import net.onefivefour.echolist.feature.reminderssummary.resources.reminders_summary_permission_denied
import net.onefivefour.echolist.feature.reminderssummary.resources.reminders_summary_save_error
import net.onefivefour.echolist.feature.reminderssummary.resources.reminders_summary_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@Composable
fun RemindersSummaryFeature() {
    val viewModel = koinViewModel<RemindersSummaryViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RemindersSummary(
        state = state,
        onToggle = viewModel::setEnabled,
        onRetry = viewModel::load
    )
}

@Composable
internal fun RemindersSummary(
    state: RemindersSummaryUiState,
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
            text = stringResource(Res.string.reminders_summary_title),
            style = EchoListTheme.typography.headlineSmall,
            color = EchoListTheme.materialColors.primary
        )
        state.error?.let { error ->
            Text(
                text = stringResource(
                    when (error) {
                        NotificationError.Load -> Res.string.reminders_summary_load_error
                        NotificationError.Save -> Res.string.reminders_summary_save_error
                        NotificationError.Permission -> Res.string.reminders_summary_permission_denied
                    }
                ),
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
                text = stringResource(Res.string.reminders_summary_empty),
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
                                style = EchoListTheme.typography.bodyLarge,
                                color = EchoListTheme.materialColors.onBackground
                            )
                            DueDateTagForDate(
                                dueDate = task.dueDate,
                                today = today,
                                urgency = when (DueDateUrgencyCalculator.computeUrgency(task.dueDate, today)) {
                                    DueDateUrgency.Normal -> DueDateTagUrgency.Normal
                                    DueDateUrgency.Warning -> DueDateTagUrgency.Warning
                                    DueDateUrgency.Overdue -> DueDateTagUrgency.Overdue
                                },
                                isRecurring = task.isRecurring
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