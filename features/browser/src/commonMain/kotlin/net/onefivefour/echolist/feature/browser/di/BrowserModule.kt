package net.onefivefour.echolist.feature.browser.di

import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.feature.browser.data.repository.FileRepositoryImpl
import net.onefivefour.echolist.feature.browser.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.feature.browser.data.source.network.FileRemoteDataSourceImpl
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository
import net.onefivefour.echolist.feature.browser.ui.BrowserViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val browserModule = module {
    single<FileRemoteDataSource> { FileRemoteDataSourceImpl(client = get()) }
    single<FileRepository> {
        FileRepositoryImpl(
            fileRemoteDataSource = get(),
            directoryChangedNotifier = get(),
            dispatcher = Dispatchers.Default
        )
    }
    viewModel { params ->
        BrowserViewModel(
            parentDir = params.get(),
            fileRepository = get(),
            directoryChangeNotifier = get()
        )
    }
}
