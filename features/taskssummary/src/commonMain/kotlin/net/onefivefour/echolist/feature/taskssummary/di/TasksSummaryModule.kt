package net.onefivefour.echolist.feature.taskssummary.di

import net.onefivefour.echolist.feature.taskssummary.ui.TasksSummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tasksSummaryModule = module {
    viewModel { params -> TasksSummaryViewModel(parentDir = params.get(), taskListRepository = get()) }
}
