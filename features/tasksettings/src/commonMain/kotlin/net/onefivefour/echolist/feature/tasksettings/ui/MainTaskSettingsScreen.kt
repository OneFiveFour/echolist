package net.onefivefour.echolist.feature.tasksettings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.DailyDetailContent
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.MonthlyDetailContent
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.RecurrenceInterval
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.RecurrenceIntervalPicker
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.RecurrenceState
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.WeeklyDetailContent
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.isValidDayOfMonth
import net.onefivefour.echolist.feature.tasksettings.ui.recurrence.isValidPositiveInt
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun MainTaskSettingsScreen(
    uiState: MainTaskSettingsUiState,
    onDateSelected: (Long) -> Unit,
    onRecurrenceIntervalSelected: (RecurrenceInterval) -> Unit,
    onRecurrenceDetailChanged: (RecurrenceState) -> Unit,
    onNotificationToggleChanged: (Boolean) -> Unit
) {
    when (uiState) {
        is MainTaskSettingsUiState.Loading -> Unit
        is MainTaskSettingsUiState.Ready -> MainTaskSettingsContent(
            uiState = uiState,
            onDateSelected = onDateSelected,
            onRecurrenceIntervalSelected = onRecurrenceIntervalSelected,
            onRecurrenceDetailChanged = onRecurrenceDetailChanged,
            onNotificationToggleChanged = onNotificationToggleChanged
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTaskSettingsContent(
    uiState: MainTaskSettingsUiState.Ready,
    onDateSelected: (Long) -> Unit,
    onRecurrenceIntervalSelected: (RecurrenceInterval) -> Unit,
    onRecurrenceDetailChanged: (RecurrenceState) -> Unit,
    onNotificationToggleChanged: (Boolean) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.initialDateMillis
    )

    LaunchedEffect(datePickerState) {
        snapshotFlow { datePickerState.selectedDateMillis }
            .drop(1)
            .filterNotNull()
            .collect { selectedMillis ->
                onDateSelected(selectedMillis)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.l)
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxWidth(),
            title = null,
            headline = null,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = Color.Transparent
            )
        )

        SettingsSection(title = "Repeat") {
            RecurrenceIntervalPicker(
                selectedInterval = uiState.recurrenceState.interval,
                onIntervalSelected = onRecurrenceIntervalSelected
            )

            RecurrenceDetail(
                recurrenceState = uiState.recurrenceState,
                showValidationErrors = uiState.showRecurrenceValidationErrors,
                onRecurrenceDetailChanged = onRecurrenceDetailChanged
            )
        }

        SettingsSection(title = "Notifications") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Erinnern",
                    style = EchoListTheme.typography.bodyMedium
                )
                Switch(
                    checked = uiState.isNotificationEnabled,
                    onCheckedChange = onNotificationToggleChanged,
                    enabled = uiState.isNotificationToggleEnabled
                )
            }

            AnimatedVisibility(visible = !uiState.isNotificationToggleEnabled) {
                Text(
                    text = "Zum Erinnern bitte ein Fälligkeitsdatum wählen.",
                    style = EchoListTheme.typography.bodySmall,
                    color = EchoListTheme.materialColors.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(visible = uiState.showDueDateRequiredError) {
            Text(
                text = "A due date is required when recurrence is active.",
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.error,
                modifier = Modifier.padding(horizontal = EchoListTheme.dimensions.l)
            )
        }
    }
}

@Composable
private fun RecurrenceDetail(
    recurrenceState: RecurrenceState,
    showValidationErrors: Boolean,
    onRecurrenceDetailChanged: (RecurrenceState) -> Unit
) {
    val hasDetail = recurrenceState is RecurrenceState.Daily ||
        recurrenceState is RecurrenceState.Weekly ||
        recurrenceState is RecurrenceState.Monthly

    AnimatedVisibility(visible = hasDetail) {
        Column(
            modifier = Modifier.padding(top = EchoListTheme.dimensions.l)
        ) {
            when (recurrenceState) {
                is RecurrenceState.Off -> Unit
                is RecurrenceState.Yearly -> Unit
                is RecurrenceState.Daily -> {
                    DailyDetailContent(
                        selectedDays = recurrenceState.selectedDays,
                        onDayToggled = { day, checked ->
                            val updatedDays = if (checked) {
                                recurrenceState.selectedDays + day
                            } else {
                                recurrenceState.selectedDays - day
                            }
                            onRecurrenceDetailChanged(recurrenceState.copy(selectedDays = updatedDays))
                        }
                    )
                }

                is RecurrenceState.Weekly -> {
                    WeeklyDetailContent(
                        everyNWeeks = recurrenceState.everyNWeeks,
                        onWeekCountChanged = { newCount ->
                            onRecurrenceDetailChanged(recurrenceState.copy(everyNWeeks = newCount))
                        },
                        isError = showValidationErrors && !isValidPositiveInt(recurrenceState.everyNWeeks)
                    )
                }

                is RecurrenceState.Monthly -> {
                    MonthlyDetailContent(
                        everyNMonths = recurrenceState.everyNMonths,
                        dayOfMonth = recurrenceState.dayOfMonth,
                        onMonthIntervalChanged = { newInterval ->
                            onRecurrenceDetailChanged(recurrenceState.copy(everyNMonths = newInterval))
                        },
                        onDayOfMonthChanged = { newDay ->
                            onRecurrenceDetailChanged(recurrenceState.copy(dayOfMonth = newDay))
                        },
                        isMonthIntervalError =
                        showValidationErrors && !isValidPositiveInt(recurrenceState.everyNMonths),
                        isDayOfMonthError = showValidationErrors && !isValidDayOfMonth(recurrenceState.dayOfMonth)
                    )
                }
            }
        }
    }
}