package com.hyperrecursion.home_screen_vault2.widget

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.glance.appwidget.updateAll
import com.hyperrecursion.home_screen_vault2.AppConfigRepository
import com.hyperrecursion.home_screen_vault2.scanner.ScanningWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WidgetStateRepoModule {

    @Provides
    @Singleton
    fun provideWidgetStateRepository(
        dataStore: DataStore<WidgetStateProto>,
        appConfigRepository: AppConfigRepository,
        @ApplicationContext context: Context
    ): WidgetStateRepository {
        return WidgetStateRepository(
            dataStore = dataStore,
            appConfigRepository = appConfigRepository,
            context = context,
        )
    }
}

class WidgetStateRepository constructor(
    private val dataStore: DataStore<WidgetStateProto>,
    private val appConfigRepository: AppConfigRepository,
    private val context: Context
) {
    companion object {
        fun checkPathExists(path: String): Boolean {
            val publicStorageDir = Environment.getExternalStorageDirectory()
            val folderPath = File(publicStorageDir, path)
            return folderPath.exists()
        }
    }

    fun getWidgetStateFlow(): Flow<WidgetState> = dataStore.data.map { WidgetState.fromProto(it) }

    private suspend fun saveWidgetState(widgetState: WidgetState) {
        dataStore.updateData {
            it.toBuilder()
                .setRootState(widgetState.rootState.protoBuilder)
                .build()
        }
    }

    // for widget's use
    suspend fun updateByWidget(widgetState: WidgetState) {
        saveWidgetState(widgetState)
        coroutineScope {
            delay(10000)
            ScanningWorker.updateWork(
                appConfigRepository.getAppConfigFlow().first().highFreqScanIntervalSecs.toLong(),
                context
            )
            updateByNewScan(updateUI = true)
            Log.d("WidgetStateRepository", "updateByNewScan")
        }
    }

    suspend fun updateByNewScan(updateUI: Boolean = true): Result<Unit> =
        withContext(Dispatchers.IO) {
            val appConfig = appConfigRepository.getAppConfigFlow().first()
            val vaultFolder = File(Environment.getExternalStorageDirectory(), appConfig.vaultPath)
            val prevState = getWidgetStateFlow().first()
            val newWidgetState = scannedAndUpdatedState(prevState, vaultFolder)
            if (newWidgetState != null) {
                saveWidgetState(newWidgetState)
                if (updateUI) {
                    updateUI()
                }
                if (newWidgetState.noChangeCount == 10) {
                    ScanningWorker.updateWork(
                        appConfigRepository.getAppConfigFlow()
                            .first().lowFreqScanIntervalSecs.toLong(),
                        context
                    )
                }
                Log.d(
                    "WidgetStateRepository",
                    "updateByNewScan: newWidgetState.noChangeCount = ${newWidgetState.noChangeCount}"
                )
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to update widget state"))
            }
        }

    // back to update widget
    suspend fun updateUI() {
        Widget().updateAll(context)
    }

    private suspend fun scannedAndUpdatedState(
        prevWidgetState: WidgetState,
        vaultFolder: File
    ): WidgetState? {
        // Read the first N chars as descriptions, using UTF-8
        suspend fun readDescription(
            file: File,
            charCount: Int
        ): String = withContext(Dispatchers.IO) {
            try {
                val inputStream = FileInputStream(file)
                val buffer = ByteArray(charCount)
                val bytesRead = inputStream.read(buffer)
                inputStream.close()
                String(buffer, 0, bytesRead, Charsets.UTF_8).trim() // Trim any trailing whitespace
            } catch (e: Exception) {
                // Handle exceptions gracefully, e.g., log the error or return an empty string
                Log.e("FileScanner", "Error reading file: ${e.message}")
                ""
            }
        }

        suspend fun newFileState(
            file: File,
            sourcePath: String,
        ): WidgetState.FileState = withContext(Dispatchers.IO) {
            WidgetState.FileState(
                path = "$sourcePath${file.name}",
                name = file.name,
                description = readDescription(file, 100),
                modifiedTime = file.lastModified(),
                star = false,
                starredTime = 0,
                noChangeCount = 0,
            )
        }

        suspend fun updatedFileState(
            existingFileState: WidgetState.FileState,
            file: File,
            sourcePath: String,
        ): WidgetState.FileState {
            val path = "$sourcePath${file.name}"
            val name = file.name
            val description = readDescription(file, 100)
            val modifiedTime = file.lastModified()
            val star = existingFileState.star
            val starredTime = existingFileState.starredTime
            val noChangeCount = if (name == existingFileState.name
                && description == existingFileState.description
                && modifiedTime == existingFileState.modifiedTime
            ) existingFileState.noChangeCount + 1 else 0
            return WidgetState.FileState(
                path = path,
                name = name,
                description = description,
                modifiedTime = modifiedTime,
                star = star,
                starredTime = starredTime,
                noChangeCount = noChangeCount,
            )
        }

        suspend fun getFilesAndFolders(vaultPath: File): Pair<List<File>, List<File>>? {
            return vaultPath.listFiles()
                ?.partition { it.isDirectory }
        }

        fun newFolderState(
            folder: File,
            sourcePath: String,
        ): WidgetState.FolderState {
            return WidgetState.FolderState(
                path = "$sourcePath${folder.name}/",
                name = folder.name,
                modifiedTime = folder.lastModified(),
                isExpanded = false,
                star = false,
                starredTime = 0,
                sortOrder = WidgetState.SortOrder.NAME_ASC,
                folders = listOf(),
                files = listOf(),
                noChangeCount = 0,
            )
        }

        suspend fun updatedFolderState(
            prevFolderState: WidgetState.FolderState,
            folder: File,
            sourcePath: String,
            isRoot: Boolean = false
        ): WidgetState.FolderState {
            val path = if (isRoot) "/" else "$sourcePath${folder.name}/"
            var folderStateList = listOf<WidgetState.FolderState>()
            var fileStateList = listOf<WidgetState.FileState>()
            val prevFolderStates = prevFolderState.folders.associateBy({ it.name }, { it })
            val prevFileStates = prevFolderState.files.associateBy({ it.name }, { it })
            var noChangeCount = prevFolderState.noChangeCount + 1
            if (prevFolderState.isExpanded) {
                val filesAndFolders = getFilesAndFolders(folder)
                if (filesAndFolders != null) {
                    val (folders, files) = filesAndFolders.let { (folders, files) ->
                        folders.filter { !it.name.startsWith(".") }
                            .associateBy({ it.name }, { it }) to
                                files.filter { !it.name.startsWith(".")  }
                                    .associateBy({ it.name }, { it })
                    }
                    if (folders.keys != prevFolderStates.keys || files.keys != prevFileStates.keys)
                        noChangeCount = 0
                    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
                    val folderJobs = folders.map {
                        scope.async {
                            if (!it.value.canRead()) return@async null
                            if (prevFolderStates.containsKey(it.key))
                                updatedFolderState(prevFolderStates[it.key]!!, it.value, path)
                            else
                                newFolderState(it.value, path)
                        }
                    }
                    val fileJobs = files.map {
                        scope.async {
                            if (!it.value.canRead()) return@async null
                            if (prevFileStates.containsKey(it.key))
                                updatedFileState(prevFileStates[it.key]!!, it.value, path)
                            else
                                newFileState(it.value, path)
                        }
                    }
                    folderStateList = folderJobs.awaitAll().filterNotNull()
                    fileStateList = fileJobs.awaitAll().filterNotNull()
                    noChangeCount =
                        noChangeCount.coerceAtMost(folderStateList.minOfOrNull { it.noChangeCount }
                            ?: Int.MAX_VALUE)
                            .coerceAtMost(fileStateList.minOfOrNull { it.noChangeCount }
                                ?: Int.MAX_VALUE)
                }
            }

            val name = folder.name
            val modifiedTime = folder.lastModified()
            if (prevFolderState.name != name || prevFolderState.modifiedTime != modifiedTime)
                noChangeCount = 0

            return WidgetState.FolderState(
                path = path,
                name = name,
                modifiedTime = modifiedTime,
                isExpanded = prevFolderState.isExpanded,
                star = prevFolderState.star,
                starredTime = prevFolderState.starredTime,
                sortOrder = prevFolderState.sortOrder,
                folders = folderStateList,
                files = fileStateList,
                noChangeCount = noChangeCount,
            )
        }
        return withContext(Dispatchers.IO) {
            if (!vaultFolder.isDirectory || !vaultFolder.canRead()) return@withContext null
            val updatedRootState =
                updatedFolderState(prevWidgetState.rootState, vaultFolder, "", true)
            return@withContext WidgetState(
                rootState = updatedRootState,
                noChangeCount = (prevWidgetState.noChangeCount + 1).coerceAtMost(updatedRootState.noChangeCount)
            )
        }
    }

}