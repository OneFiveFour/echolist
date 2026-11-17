package net.onefivefour.echolist.feature.browser.di

import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.feature.browser.data.repository.FileRepositoryImpl
import net.onefivefour.echolist.feature.browser.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.feature.browser.data.source.network.FileRemoteDataSourceImpl
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository
import net.onefivefour.echolist.feature.browser.ui.BrowserViewModel
import net.onefivefour.echolist.feature.browser.ui.CreateFolderViewModel
import net.onefivefour.echolist.feature.browser.ui.RenameFolderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val browserModule = module {
    single<FileRemoteDataSource> { FileRemoteDataSourceImpl(client = get()) }
    single<FileRepository> {
        FileRepositoryImpl(networkDataSource = get(), directoryChangeNotifier = get(), dispatcher = Dispatchers.Default)
    }
    viewModel { params ->
        BrowserViewModel(
            parentDir = params.get(),
            fileRepository = get(),
            directoryChangeNotifier = get()
        )
    }
    viewModel { params -> CreateFolderViewModel(parentDir = params.get(), fileRepository = get()) }
    viewModel { params -> RenameFolderViewModel(parentDir = params.get(), fileRepository = get()) }
}