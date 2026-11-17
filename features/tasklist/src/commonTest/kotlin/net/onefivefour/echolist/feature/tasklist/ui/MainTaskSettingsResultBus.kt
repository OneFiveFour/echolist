package net.onefivefour.echolist.feature.tasklist.ui

import net.onefivefour.echolist.core.tasks.domain.model.TaskSettingsChanges

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Concrete DI type for settings results so Koin never confuses this flow with
 * other generic MutableSharedFlow registrations after JVM type erasure.
 */
internal class MainTaskSettingsResultBus(
    resultFlow: MutableSharedFlow<TaskSettingsChanges> = MutableSharedFlow()
) : net.onefivefour.echolist.core.tasks.domain.TaskSettingsResultSink {

    val results: SharedFlow<TaskSettingsChanges>
        field = resultFlow

    override suspend fun emit(result: TaskSettingsChanges) {
        results.emit(result)
    }
}