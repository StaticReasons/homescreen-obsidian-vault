package com.hyperrecursion.home_screen_vault2.widget

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hyperrecursion.home_screen_vault2.widget.WidgetState.SortOrder

class WidgetUiState(
    val rootState: FolderState,
    private var noChangeCount: Int = 0
) {

    fun toWidgetRepoState(resetNoChangeCount: Boolean = true): WidgetState {
        return WidgetState(
            rootState = rootState.toRepoFolderState(),
            noChangeCount = if(resetNoChangeCount) 0 else noChangeCount
        )
    }

    fun updateFromRepoState(widgetState: WidgetState) {
        rootState.updateFromRepoFolderState(widgetState.rootState)
        noChangeCount = widgetState.noChangeCount
    }

    companion object {
        fun fromRepoWidgetState(widgetState: WidgetState): WidgetUiState {
            return WidgetUiState(
                rootState = FolderState.fromRepoFolderState(widgetState.rootState),
                noChangeCount = widgetState.noChangeCount
            )
        }
    }

    interface FileObject {
        var name: String
        var modifiedTime: Long
        var noChangeCount: Int

        fun <T : FileObject> List<T>.sorted(sortOrder: SortOrder): List<T> {
            return when (sortOrder) {
                SortOrder.NAME_ASC -> this.sortedBy { it.name }
                SortOrder.NAME_DESC -> this.sortedByDescending { it.name }
                SortOrder.MODIFIED_ASC -> this.sortedByDescending { it.modifiedTime }
                SortOrder.MODIFIED_DESC -> this.sortedBy { it.modifiedTime }
            }
        }
    }

    class FolderState(
        val path: String,
        initialName: String,
        initialModifiedTime: Long,
        initialIsExpanded: Boolean,
        initialStar: Boolean,
        initialStarredTime: Long,
        initialSortOrder: SortOrder,
        initialSubFolders: List<FolderState>,
        initialFiles: List<FileState>,
        initialCurrentPage: Int = 1,
        override var noChangeCount: Int = 0,
    ) : FileObject {
        override var name by mutableStateOf(initialName)
        override var modifiedTime by mutableLongStateOf(initialModifiedTime)
        var isExpanded by mutableStateOf(initialIsExpanded)
        private val _star = mutableStateOf(initialStar)
        var star: Boolean
            get() = _star.value
            set(value) {
                _star.value = value
                if (value) {
                    starredTime = System.currentTimeMillis()
                }
            }
        var starredTime by mutableLongStateOf(initialStarredTime)
            private set
        var sortOrder by mutableStateOf(initialSortOrder)

        private var _folders = mutableStateListOf<FolderState>().apply { addAll(initialSubFolders) }
        val foldedFolders: List<FolderState>
            get() = _folders.filter { !it.isExpanded }.let { folders ->
                val (starred, unstarred) = folders.partition { it.star }
                return starred.sortedBy { it.starredTime } + unstarred.sorted(sortOrder)
            }
        val expandedFolders: List<FolderState>
            get() = _folders.filter { it.isExpanded }.let { folders ->
                val (starred, unstarred) = folders.partition { it.star }
                return starred.sortedBy { it.starredTime } + unstarred.sorted(sortOrder)
            }

        private var _files = mutableStateListOf<FileState>().apply { addAll(initialFiles) }
        val files: List<FileState>
            get() = _files.let { files ->
                val (starred, unstarred) = files.partition { it.star }
                return starred.sortedByDescending { it.starredTime } + unstarred.sorted(sortOrder)
            }

        var currentPage by mutableStateOf(initialCurrentPage)

        fun checkStructure(visitedPathKeys: MutableSet<String> = mutableSetOf()): Result<Unit> {
            if (visitedPathKeys.contains(path)) {
                return Result.failure(CircularReferenceException("FolderState $this is a circular reference"))
            }
            visitedPathKeys.add(path)

            val failures = _folders
                .mapNotNull { it.checkStructure(visitedPathKeys).exceptionOrNull() }
                .filterIsInstance<CircularReferenceException>()
                .flatMap {
                    when (it) {
                        is AggregateCircularReferenceException -> it.exceptions
                        else -> listOf(it)
                    }
                }
            return if (failures.isEmpty()) {
                Result.success(Unit)
            } else {
                Result.failure(
                    AggregateCircularReferenceException(
                        "Multiple errors occurred during folder structure validation:",
                        failures
                    )
                )
            }
        }

        open class CircularReferenceException(message: String) : Exception(message)
        class AggregateCircularReferenceException(
            message: String,
            val exceptions: List<CircularReferenceException>
        ) : CircularReferenceException(message)

        fun pruneCircularFolders(visitedPathKeys: MutableSet<String> = mutableSetOf()): Boolean {
            if (visitedPathKeys.contains(path)) return true
            visitedPathKeys.add(path)
            _folders.removeAll { it.pruneCircularFolders(visitedPathKeys) }
            return false
        }

        // 猫娘函数
        fun meow() {
            Log.d("WidgetUiState", "${path}: meow!")
            _folders.forEach { it.meow() }
            _files.forEach { it.meow() }
        }

        fun toRepoFolderState(): WidgetState.FolderState {
            return WidgetState.FolderState(
                path = path,
                name = name,
                modifiedTime = modifiedTime,
                isExpanded = isExpanded,
                star = star,
                starredTime = starredTime,
                sortOrder = sortOrder,
                folders = _folders.map { it.toRepoFolderState() },
                files = _files.map { it.toRepoFileState() },
                noChangeCount = noChangeCount,
            )
        }

        fun updateFromRepoFolderState(state: WidgetState.FolderState) {
            if (path != state.path) {
                throw Exception("pathKey mismatch: $path != ${state.path}")
            }
            name = state.name
            modifiedTime = state.modifiedTime
            star = state.star
            starredTime = state.starredTime
            isExpanded = state.isExpanded
            sortOrder = state.sortOrder

            // Folders
            val (existedFolders, newFolders) = state.folders.partition { repoFolder ->
                _folders.any { it ->
                    (it.path == repoFolder.path).apply { if (this) it.updateFromRepoFolderState(repoFolder) }
                }
            }
            _folders.removeAll { existedFolders.all { repoFolder -> it.path != repoFolder.path } }
            _folders.addAll(newFolders.map { FolderState.fromRepoFolderState(it) })

            // Files
            val (existedFiles, newFiles) = state.files.partition { repoFile ->
                _files.any { it ->
                    (it.path == repoFile.path).apply { if(this) it.updateFromRepoFileState(repoFile) }
                }
            }
            _files.removeAll { existedFiles.all { repoFile -> it.path != repoFile.path } }
            _files.addAll(newFiles.map { FileState.fromRepoFileState(it) })

            noChangeCount = state.noChangeCount
        }

        companion object {
            fun fromRepoFolderState(state: WidgetState.FolderState): FolderState {
                return FolderState(
                    path = state.path,
                    initialName = state.name,
                    initialModifiedTime = state.modifiedTime,
                    initialIsExpanded = state.isExpanded,
                    initialStar = state.star,
                    initialStarredTime = state.starredTime,
                    initialSortOrder = state.sortOrder,
                    initialSubFolders = state.folders.map { fromRepoFolderState(it) },
                    initialFiles = state.files.map { FileState.fromRepoFileState(it) },
                    noChangeCount = state.noChangeCount,
                )
            }
        }

    }

    class FileState(
        val path: String,
        initialName: String,
        initialModifiedTime: Long,
        initialStar: Boolean,
        initialStarredTime: Long,
        initialDescription: String,
        override var noChangeCount: Int = 0
    ) : FileObject {
        override var name by mutableStateOf(initialName)
        override var modifiedTime by mutableLongStateOf(initialModifiedTime)
        private val _star = mutableStateOf(initialStar)
        var star: Boolean
            get() = _star.value
            set(value) {
                _star.value = value
                if (value) {
                    starredTime = System.currentTimeMillis()
                }
            }
        var starredTime by mutableLongStateOf(initialStarredTime)
            private set
        var description by mutableStateOf(initialDescription)

        fun meow() {
            Log.d("WidgetUiState", "${path}: meow!")
        }

        fun toRepoFileState(): WidgetState.FileState {
            return WidgetState.FileState(
                path = path,
                name = name,
                modifiedTime = modifiedTime,
                star = star,
                starredTime = starredTime,
                description = description,
                noChangeCount = noChangeCount
            )
        }

        fun updateFromRepoFileState(state: WidgetState.FileState) {
            if (path != state.path) {
                throw Exception("pathKey mismatch: $path != ${state.path}")
            }
            name = state.name
            modifiedTime = state.modifiedTime
            star = state.star
            starredTime = state.starredTime
            description = state.description
            noChangeCount = state.noChangeCount
        }

        companion object {
            fun fromRepoFileState(state: WidgetState.FileState): FileState {
                return FileState(
                    path = state.path,
                    initialName = state.name,
                    initialModifiedTime = state.modifiedTime,
                    initialStar = state.star,
                    initialStarredTime = state.starredTime,
                    initialDescription = state.description,
                    noChangeCount = state.noChangeCount
                )
            }
        }
    }

    class PageParams(
        val totalPagesFolders: Int = 1,
        val pageSizeFolders: Int = 1,
        private val _currentPage: Int = 1,
        val totalPagesFiles: Int = 1,
        val pageSizeFiles: Int = 1,
        val pageColumnsFiles: Int = 1,
    ) {
        // =1: Dont display Page Navigator;
        // =0: Dont display FirstView
        val maxPage: Int = maxOf(totalPagesFolders, totalPagesFiles)
        val currentPage: Int = _currentPage.coerceIn(1.coerceAtMost(maxPage), maxPage)
    }
}