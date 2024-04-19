package com.hyperrecursion.home_screen_vault2.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetState(
    val rootState: FolderState = FolderState.defaultRootState,
    val noChangeCount: Int = 0,
) {

    companion object {
        val defaultState
            get() = WidgetState(
                rootState = FolderState.defaultRootState,
                noChangeCount = 0
            )

        fun fromProto(proto: WidgetStateProto): WidgetState {
            // If rootState's path is not "/", then it's invalid (in case the file havn't been created yet), and use the default one
            return WidgetState(
                rootState = proto.rootState!!.let {
                    if (it.path == "/") FolderState.fromProto(it) else FolderState.testState
                },
                noChangeCount = proto.noChangeCount
            )
        }
    }

    @Serializable
    data class FolderState(
        val path: String,
        val name: String,
        val modifiedTime: Long,
        val isExpanded: Boolean,
        val star: Boolean,
        val starredTime: Long,
        val sortOrder: SortOrder,
        val folders: List<FolderState>,
        val files: List<FileState>,
        val noChangeCount: Int = 0,
    ) {
        fun checkStructure(visitedPathKeys: MutableSet<String> = mutableSetOf()): Boolean {
            if (visitedPathKeys.contains(path)) {
                throw Exception("FolderState $this is a circular reference")
            }
            visitedPathKeys.add(path)
            return folders.all { it.checkStructure(visitedPathKeys) }
        }

        val protoBuilder: FolderStateProto.Builder
            get() = FolderStateProto.newBuilder()
                .setPath(path)
                .setName(name)
                .setModifiedTime(modifiedTime)
                .setIsExpanded(isExpanded)
                .setStar(star)
                .setStarredTime(starredTime)
                .setSortOrder(sortOrder.toProto())
                .apply {
                    folders.forEach { addFolders(it.protoBuilder) }
                    files.forEach { addFiles(it.protoBuilder) }
                }
                .setNoChangeCount(noChangeCount)

        companion object {
            fun fromProto(proto: FolderStateProto): FolderState {
                return FolderState(
                    path = proto.path,
                    name = proto.name,
                    modifiedTime = proto.modifiedTime,
                    isExpanded = proto.isExpanded,
                    star = proto.star,
                    starredTime = proto.starredTime,
                    sortOrder = SortOrder.fromProto(proto.sortOrder),
                    folders = proto.foldersList.map { FolderState.fromProto(it) },
                    files = proto.filesList.map { FileState.fromProto(it) },
                    noChangeCount = proto.noChangeCount
                )
            }

            val defaultRootState
                get() = FolderState(
                    path = "/",
                    name = "Vault 42",
                    modifiedTime = 0,
                    isExpanded = false,
                    star = false,
                    starredTime = 0,
                    sortOrder = SortOrder.NAME_ASC,
                    folders = listOf(),
                    files = listOf(),
                    noChangeCount = 0,
                )
        }
    }

    @Serializable
    data class FileState(
        val path: String,
        val name: String,
        val description: String,
        val modifiedTime: Long,
        val star: Boolean,
        val starredTime: Long,
        val noChangeCount: Int = 0,
    ) {
        val protoBuilder: FileStateProto.Builder
            get() = FileStateProto.newBuilder()
                .setPath(path)
                .setName(name)
                .setDescription(description)
                .setModifiedTime(modifiedTime)
                .setStar(star)
                .setStarredTime(starredTime)
                .setNoChangeCount(noChangeCount)

        companion object {
            fun fromProto(proto: FileStateProto): FileState {
                return FileState(
                    path = proto.path,
                    name = proto.name,
                    description = proto.description,
                    modifiedTime = proto.modifiedTime,
                    star = proto.star,
                    starredTime = proto.starredTime,
                    noChangeCount = proto.noChangeCount
                )
            }
        }
    }

    enum class SortOrder {
        NAME_ASC,
        NAME_DESC,
        MODIFIED_ASC,
        MODIFIED_DESC;

        fun next(): SortOrder {
            val values = SortOrder.entries.toTypedArray()
            val currentIndex = values.indexOf(this)
            return values[(currentIndex + 1) % values.size]
        }

        fun toProto(): SortOrderProto {
            return when (this) {
                NAME_ASC -> SortOrderProto.NAME_ASC
                NAME_DESC -> SortOrderProto.NAME_DESC
                MODIFIED_ASC -> SortOrderProto.MODIFIED_ASC
                MODIFIED_DESC -> SortOrderProto.MODIFIED_DESC
            }
        }

        companion object {
            fun fromProto(proto: SortOrderProto): SortOrder {
                return when (proto) {
                    SortOrderProto.NAME_ASC -> NAME_ASC
                    SortOrderProto.NAME_DESC -> NAME_DESC
                    SortOrderProto.MODIFIED_ASC -> MODIFIED_ASC
                    SortOrderProto.MODIFIED_DESC -> MODIFIED_DESC
                    SortOrderProto.UNRECOGNIZED -> NAME_ASC
                }
            }
        }
    }

}

val WidgetState.Companion.testState
    get() = WidgetState(
        rootState = WidgetState.FolderState.testState,
        noChangeCount = 0,
    )

val WidgetState.FolderState.Companion.testState
    get() = WidgetState.FolderState(
        path = "/",
        name = "Vault 42",
        modifiedTime = 0,
        star = false,
        starredTime = 30,
        isExpanded = true,
        sortOrder = WidgetState.SortOrder.NAME_ASC,
        folders = listOf(
            WidgetState.FolderState(
                path = "/folder1",
                name = "folder1",
                modifiedTime = 200,
                isExpanded = true,
                star = false,
                starredTime = 30,
                sortOrder = WidgetState.SortOrder.NAME_ASC,
                files = listOf(
                    WidgetState.FileState(
                        path = "/folder1/file1",
                        name = "file1",
                        modifiedTime = 200,
                        star = false,
                        starredTime = 30,
                        description = "11xxxxxxxxxxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxxx",
                    )
                ),
                folders = listOf(
                    WidgetState.FolderState(
                        path = "/folder1/folder2",
                        name = "folder2",
                        modifiedTime = 100,
                        isExpanded = false,
                        star = true,
                        starredTime = 30,
                        sortOrder = WidgetState.SortOrder.NAME_ASC,
                        files = listOf(
                            WidgetState.FileState(
                                path = "/folder1/folder2/file1",
                                name = "file1",
                                modifiedTime = 0,
                                star = false,
                                starredTime = 30,
                                description = "121xxxxxxxxxxxxx",
                            )
                        ),
                        folders = listOf()
                    ),
                    WidgetState.FolderState(
                        path = "/folder1/folder3",
                        name = "folder3",
                        modifiedTime = 200,
                        isExpanded = false,
                        star = false,
                        starredTime = 30,
                        sortOrder = WidgetState.SortOrder.NAME_ASC,
                        files = listOf(),
                        folders = listOf()
                    ),
                    WidgetState.FolderState(
                        path = "/folder1/folder4",
                        name = "folder4",
                        modifiedTime = 200,
                        isExpanded = false,
                        star = false,
                        starredTime = 30,
                        sortOrder = WidgetState.SortOrder.NAME_ASC,
                        files = listOf(),
                        folders = listOf()
                    ),
                    WidgetState.FolderState(
                        path = "/folder1/folder5",
                        name = "folder5",
                        modifiedTime = 200,
                        isExpanded = false,
                        star = false,
                        starredTime = 30,
                        sortOrder = WidgetState.SortOrder.NAME_ASC,
                        files = listOf(),
                        folders = listOf()
                    ),
                    WidgetState.FolderState(
                        path = "/folder1/folder6",
                        name = "folder6",
                        modifiedTime = 200,
                        isExpanded = false,
                        star = false,
                        starredTime = 30,
                        sortOrder = WidgetState.SortOrder.NAME_ASC,
                        files = listOf(),
                        folders = listOf()
                    ),
                    WidgetState.FolderState(
                        path = "/folder1/folder7",
                        name = "folder7",
                        modifiedTime = 200,
                        isExpanded = false,
                        star = false,
                        starredTime = 30,
                        sortOrder = WidgetState.SortOrder.NAME_ASC,
                        files = listOf(),
                        folders = listOf()
                    ),
                    WidgetState.FolderState(
                        path = "/folder1/folder8",
                        name = "folder8",
                        modifiedTime = 200,
                        isExpanded = false,
                        star = false,
                        starredTime = 30,
                        sortOrder = WidgetState.SortOrder.NAME_ASC,
                        files = listOf(),
                        folders = listOf()
                    ),
                )
            ),
            WidgetState.FolderState(
                path = "/folder2",
                name = "folder2",
                modifiedTime = 100,
                isExpanded = false,
                star = false,
                starredTime = 30,
                sortOrder = WidgetState.SortOrder.NAME_ASC,
                files = listOf(),
                folders = listOf()
            )
        ),
        files = listOf(
            WidgetState.FileState(
                path = "/file1",
                name = "file1",
                modifiedTime = 200,
                star = false,
                starredTime = 30,
                description = "1xxxxxxxxxxxxx",
            ),
            WidgetState.FileState(
                path = "/file2",
                name = "file2",
                modifiedTime = 100,
                star = true,
                starredTime = 30,
                description = "2xxxxxxxxxxxxx",
            ),
            WidgetState.FileState(
                path = "/file3",
                name = "file3",
                modifiedTime = 100,
                star = true,
                starredTime = 30,
                description = "2xxxxxxxxxxxxx",
            ),
            WidgetState.FileState(
                path = "/file4",
                name = "file4",
                modifiedTime = 100,
                star = false,
                starredTime = 30,
                description = "2xxxxxxxxxxxxx",
            ),
            WidgetState.FileState(
                path = "/file5",
                name = "file5",
                modifiedTime = 100,
                star = false,
                starredTime = 30,
                description = "2xxxxxxxxxxxxx",
            ),
            WidgetState.FileState(
                path = "/file6",
                name = "file6",
                modifiedTime = 100,
                star = false,
                starredTime = 30,
                description = "2xxxxxxxxxxxxx",
            ),
            WidgetState.FileState(
                path = "/file7",
                name = "file7",
                modifiedTime = 100,
                star = false,
                starredTime = 30,
                description = "2xxxxxxxxxxxxx",
            ),
        )
    )