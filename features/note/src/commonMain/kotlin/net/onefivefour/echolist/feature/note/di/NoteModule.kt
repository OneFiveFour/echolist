package net.onefivefour.echolist.feature.note.di

import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.core.session.domain.SessionDataCleaner
import net.onefivefour.echolist.feature.note.data.repository.NotesRepositoryImpl
import net.onefivefour.echolist.feature.note.data.source.cache.CacheDataSource
import net.onefivefour.echolist.feature.note.data.source.cache.CacheDataSourceImpl
import net.onefivefour.echolist.feature.note.data.source.network.NoteRemoteDataSource
import net.onefivefour.echolist.feature.note.data.source.network.NoteRemoteDataSourceImpl
import net.onefivefour.echolist.feature.note.domain.repository.NotesRepository
import net.onefivefour.echolist.feature.note.ui.EditNoteMode
import net.onefivefour.echolist.feature.note.ui.EditNoteViewModel
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module

val noteModule = module {
    single<NoteRemoteDataSource> { NoteRemoteDataSourceImpl(client = get()) }
    single<CacheDataSource> { CacheDataSourceImpl(databaseProvider = get()) }
    single<SessionDataCleaner>(named("note-cache-cleaner")) {
        val cache = get<CacheDataSource>()
        SessionDataCleaner { cache.clear() }
    }
    single<NotesRepository> {
        NotesRepositoryImpl(
            noteRemoteDataSource = get(),
            cacheDataSource = get(),
            directoryChangeNotifier = get(),
            dispatcher = Dispatchers.Default
        )
    } withOptions { onClose { (it as? AutoCloseable)?.close() } }
    viewModel { params -> EditNoteViewModel(mode = params.get<EditNoteMode>(), notesRepository = get()) }
}