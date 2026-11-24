package net.onefivefour.echolist.feature.browser.data.repository

import `file`.v1.ListFilesRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import net.onefivefour.echolist.core.files.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.feature.browser.data.mapper.FileMapper
import net.onefivefour.echolist.feature.browser.data.source.network.FileRemoteDataSource
import net.onefivefour.echolist.feature.browser.domain.model.CreateFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.DeleteFolderParams
import net.onefivefour.echolist.feature.browser.domain.model.FileEntry
import net.onefivefour.echolist.feature.browser.domain.model.Folder
import net.onefivefour.echolist.feature.browser.domain.model.UpdateFolderParams
import net.onefivefour.echolist.feature.browser.domain.repository.FileRepository

internal class FileRepositoryImpl(
    private val fileRemoteDataSource: FileRemoteDataSource,
    private val directoryChangedNotifier: DirectoryChangeNotifier,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val currentDate: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
) : FileRepository {

    override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                val response = fileRemoteDataSource.createFolder(request)
                val folder = FileMapper.toDomain(response)
                directoryChangedNotifier.notifyChanged(params.parentDir)
                Result.success(folder)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun listFiles(parentDir: String): Result<List<FileEntry>> =
        withContext(dispatcher) {
            try {
                val request = ListFilesRequest(
                    parent_dir = parentDir,
                    reference_date = currentDate().toString()
                )
                val response = fileRemoteDataSource.listFiles(request)
                Result.success(FileMapper.toDomain(response))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                val response = fileRemoteDataSource.updateFolder(request)
                val folder = FileMapper.toDomain(response)
                val parentDir = params.folderPath.substringBeforeLast('/', "")
                directoryChangedNotifier.notifyChanged(parentDir)
                Result.success(folder)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
        withContext(dispatcher) {
            try {
                val request = FileMapper.toProto(params)
                fileRemoteDataSource.deleteFolder(request)
                val parentDir = params.folderPath.substringBeforeLast('/', "")
                directoryChangedNotifier.notifyChanged(parentDir)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}