package net.onefivefour.echolist.di

import net.onefivefour.echolist.core.deeplinks.di.deepLinkModule
import net.onefivefour.echolist.core.designsystem.di.designSystemModule
import net.onefivefour.echolist.core.files.di.filesModule
import net.onefivefour.echolist.core.session.di.sessionModule
import net.onefivefour.echolist.core.tasks.di.tasksModule
import net.onefivefour.echolist.feature.browser.di.browserModule
import net.onefivefour.echolist.feature.taskssummary.di.tasksSummaryModule
import net.onefivefour.echolist.feature.login.di.loginModule
import net.onefivefour.echolist.feature.note.di.noteModule
import net.onefivefour.echolist.feature.tasklist.di.taskListModule
import net.onefivefour.echolist.feature.tasksettings.di.taskSettingsModule
import net.onefivefour.echolist.feature.reminderssummary.di.remindersSummaryModule
import net.onefivefour.echolist.ui.navigation.MainTaskSettingsResults
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import net.onefivefour.echolist.core.notifications.domain.NotificationAccountProvider

private val compositionModule = module {
    single<NotificationAccountProvider> { NotificationAccount(get()) }
    viewModel { MainTaskSettingsResults() }
}

val appModules: List<Module> = listOf(
    compositionModule,
    deepLinkModule,
    sessionModule,
    filesModule,
    tasksModule,
    designSystemModule,
    browserModule,
    tasksSummaryModule,
    loginModule,
    noteModule,
    taskListModule,
    taskSettingsModule,
    remindersSummaryModule
)