package net.onefivefour.echolist.feature.reminderssummary.di

import net.onefivefour.echolist.feature.reminderssummary.ui.RemindersSummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val remindersSummaryModule = module {
    viewModel { RemindersSummaryViewModel(get(), get(), get(), get(), get()) }
}