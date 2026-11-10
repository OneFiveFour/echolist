package net.onefivefour.echolist.feature.tasknotifications.di

import net.onefivefour.echolist.feature.tasknotifications.ui.TaskNotificationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val taskNotificationsModule = module {
    viewModel { TaskNotificationsViewModel(get(), get(), get(), get(), get()) }
}
