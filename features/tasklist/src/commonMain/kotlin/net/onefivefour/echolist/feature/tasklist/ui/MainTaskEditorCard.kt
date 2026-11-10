package net.onefivefour.echolist.feature.tasklist.ui

import net.onefivefour.echolist.core.designsystem.resources.Res as SharedRes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.core.designsystem.resources.ic_delete
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.onefivefour.echolist.core.tasks.domain.DueDateUrgencyCalculator
import net.onefivefour.echolist.core.tasks.domain.model.DueDateUrgency
import net.onefivefour.echolist.core.tasks.domain.model.MainTask
import net.onefivefour.echolist.core.tasks.domain.model.SubTask
import net.onefivefour.echolist.core.designsystem.ui.components.DueDateTag
import net.onefivefour.echolist.core.designsystem.ui.components.relativeDueDateText
import net.onefivefour.echolist.core.designsystem.ui.components.ElTextField
import net.onefivefour.echolist.core.designsystem.ui.components.GradientBackground
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MainTaskCard(
    mainTask: UiMainTask,
    isAutoDelete: Boolean,
    onRemoveMainTask: () -> Unit,
    onMainTaskCheckedChange: (Boolean) -> Unit,
    onMainTaskKeyboardAction: (String) -> Unit,
    onSubTaskCheckedChange: (Int, Boolean) -> Unit,
    onFieldFocusLost: () -> Unit,
    onMainTaskDescriptionFocusChanged: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    shouldFocusMainTask: Boolean,
    onMainTaskFocusHandled: () -> Unit,
    showAddFirstSubTask: Boolean,
    onAddFirstSubTask: () -> Unit,
    focusedSubTaskId: String?,
    onSubTaskFocusHandled: () -> Unit,
    onSubTaskKeyboardAction: (String) -> Unit
) {
    val mainTaskFocusRequester = remember(mainTask.id) { FocusRequester() }

    LaunchedEffect(shouldFocusMainTask) {
        if (shouldFocusMainTask) {
            mainTaskFocusRequester.requestFocus()
            onMainTaskFocusHandled()
        }
    }

    Surface(
        shape = EchoListTheme.shapes.medium,
        color = EchoListTheme.materialColors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = EchoListTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier.padding(EchoListTheme.dimensions.s)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides EchoListTheme.dimensions.xl) {
                    Checkbox(
                        checked = mainTask.isDone,
                        onCheckedChange = onMainTaskCheckedChange
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    ElTextField(
                        state = mainTask.descriptionState,
                        style = EchoListTheme.typography.bodyLarge.copy(
                            textDecoration = if (mainTask.isDone) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            }
                        ),
                        imeAction = ImeAction.Next,
                        onKeyboardAction = { onMainTaskKeyboardAction(mainTask.id) },
                        onFocusChanged = onMainTaskDescriptionFocusChanged,
                        onFocusLost = onFieldFocusLost,
                        focusRequester = mainTaskFocusRequester
                    )

                    val dueDate = remember(mainTask.dueDateState.text.toString()) {
                        runCatching { LocalDate.parse(mainTask.dueDateState.text.toString().trim()) }
                            .getOrNull()
                    }

                    if (dueDate != null) {
                        val today by produceState(Clock.System.todayIn(TimeZone.currentSystemDefault())) {
                            while (true) {
                                value = Clock.System.todayIn(TimeZone.currentSystemDefault())
                                delay(1.minutes)
                            }
                        }
                        val (backgroundColor, contentColor) = resolveUrgencyColors(
                            DueDateUrgencyCalculator.computeUrgency(dueDate, today)
                        )
                        DueDateTag(
                            label = relativeDueDateText(dueDate, today),
                            isRecurring = mainTask.recurrenceState.text.isNotEmpty(),
                            backgroundColor = backgroundColor,
                            contentColor = contentColor,
                            onClick = onNavigateToSettings
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Set due date",
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onNavigateToSettings() }
                            .padding(
                                horizontal = EchoListTheme.dimensions.m,
                                vertical = EchoListTheme.dimensions.m
                            )
                    )

                    if (!isAutoDelete) {
                        Icon(
                            painter = painterResource(SharedRes.drawable.ic_delete),
                            contentDescription = "Delete main task",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onRemoveMainTask() }
                                .padding(
                                    horizontal = EchoListTheme.dimensions.m,
                                    vertical = EchoListTheme.dimensions.m
                                )
                        )
                    }
                }
            }

            if (mainTask.subTasks.isNotEmpty()) {
                Column {
                    mainTask.subTasks.forEachIndexed { subTaskIndex, subTask ->
                        SubTaskRow(
                            subTask = subTask,
                            shouldRequestFocus = focusedSubTaskId == subTask.id,
                            onFocusHandled = onSubTaskFocusHandled,
                            onKeyboardAction = onSubTaskKeyboardAction,
                            onFocusLost = onFieldFocusLost,
                            onCheckedChange = { isChecked ->
                                onSubTaskCheckedChange(subTaskIndex, isChecked)
                            }
                        )
                    }
                }
            } else if (showAddFirstSubTask) {
                TextButton(onClick = onAddFirstSubTask) {
                    Text(
                        text = "Add first subtask",
                        style = EchoListTheme.typography.labelMedium,
                        color = EchoListTheme.materialColors.primary
                    )
                }
            }
        }
    }
}

@Composable
internal fun resolveUrgencyColors(urgency: DueDateUrgency): Pair<Color, Color> {
    return when (urgency) {
        DueDateUrgency.Normal -> Pair(
            EchoListTheme.materialColors.surfaceVariant,
            EchoListTheme.materialColors.onSurfaceVariant
        )
        DueDateUrgency.Warning -> Pair(
            EchoListTheme.echoListColorScheme.warning,
            EchoListTheme.echoListColorScheme.onWarning
        )
        DueDateUrgency.Overdue -> Pair(
            EchoListTheme.materialColors.error,
            EchoListTheme.materialColors.onError
        )
    }
}

@Preview
@Composable
private fun MainTaskCardPreview() {
    val task = remember {
        UiMainTask.fromDomain(
            domain = MainTask(
                id = "preview-1",
                description = "Plan launch with a longer title sentence",
                isDone = false,
                dueDate = "2026-04-01",
                recurrence = "",
                subTasks = listOf(
                    SubTask(id = "sub-1", description = "Draft checklist", isDone = true),
                    SubTask(id = "sub-2", description = "Review copy", isDone = false)
                )
            )
        )
    }
    EchoListTheme {
        GradientBackground {
            MainTaskCard(
                mainTask = task,
                isAutoDelete = false,
                onRemoveMainTask = {},
                onMainTaskCheckedChange = {},
                onMainTaskKeyboardAction = {},
                onSubTaskCheckedChange = { _, _ -> },
                onFieldFocusLost = {},
                onMainTaskDescriptionFocusChanged = {},
                onNavigateToSettings = {},
                shouldFocusMainTask = false,
                onMainTaskFocusHandled = {},
                showAddFirstSubTask = false,
                onAddFirstSubTask = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainTaskCardEmptyPreview() {
    val task = remember {
        UiMainTask(id = "preview-2")
    }
    EchoListTheme {
        GradientBackground {
            MainTaskCard(
                mainTask = task,
                isAutoDelete = false,
                onRemoveMainTask = {},
                onMainTaskCheckedChange = {},
                onMainTaskKeyboardAction = {},
                onSubTaskCheckedChange = { _, _ -> },
                onFieldFocusLost = {},
                onMainTaskDescriptionFocusChanged = {},
                onNavigateToSettings = {},
                shouldFocusMainTask = false,
                onMainTaskFocusHandled = {},
                showAddFirstSubTask = true,
                onAddFirstSubTask = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainTaskCardNoDueDatePreview() {
    val task = remember {
        UiMainTask(id = "preview-3", description = "Schedule follow-up")
    }
    EchoListTheme {
        GradientBackground {
            MainTaskCard(
                mainTask = task,
                isAutoDelete = false,
                onRemoveMainTask = {},
                onMainTaskCheckedChange = {},
                onMainTaskKeyboardAction = {},
                onSubTaskCheckedChange = { _, _ -> },
                onFieldFocusLost = {},
                onMainTaskDescriptionFocusChanged = {},
                onNavigateToSettings = {},
                shouldFocusMainTask = false,
                onMainTaskFocusHandled = {},
                showAddFirstSubTask = false,
                onAddFirstSubTask = {},
                focusedSubTaskId = null,
                onSubTaskFocusHandled = {},
                onSubTaskKeyboardAction = {}
            )
        }
    }
}