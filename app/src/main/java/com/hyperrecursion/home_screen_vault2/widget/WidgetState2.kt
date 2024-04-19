//package com.hyperrecursion.home_screen_vault2.widget
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class WidgetState(
//    val rootState: FolderState = FolderState.defaultRootState
//) {
//
//    companion object {
//        val defaultState
//            get() = WidgetState(
//                rootState = FolderState.defaultRootState
//            )
//    }
//
//    @Serializable
//    data class FolderState(
//        val path: String,
//        val name: String,
//        val modifiedTime: Long,
//        val createdTime: Long,
//        val isExpanded: Boolean,
//        val star: Boolean,
//        val starredTime: Long,
//        val sortOrder: SortOrder,
//        val folders: List<FolderState>,
//        val files: List<FileState>,
//    ) {
//        companion object {
//            val defaultRootState
//                get() = FolderState(
//                    path = "/",
//                    name = "Vault 42",
//                    modifiedTime = 0,
//                    createdTime = 0,
//                    isExpanded = false,
//                    star = false,
//                    starredTime = 0,
//                    sortOrder = SortOrder.NAME_ASC,
//                    folders = listOf(),
//                    files = listOf(),
//                )
//        }
//    }
//
//    @Serializable
//    data class FileState(
//        val path: String,
//        val name: String,
//        val description: String,
//        val modifiedTime: Long,
//        val createdTime: Long,
//        val star: Boolean,
//        val starredTime: Long,
//    ) {
//    }
//
//    enum class SortOrder {
//        NAME_ASC,
//        NAME_DESC,
//        MODIFIED_ASC,
//        MODIFIED_DESC,
//        CREATED_ASC,
//        CREATED_DESC;
//    }
//
//}
//
//val WidgetState.Companion.testState
//    get() = WidgetState(
//        rootState = WidgetState.FolderState.testState,
//    )
//
//val WidgetState.FolderState.Companion.testState
//    get() = WidgetState.FolderState(
//        path = "/",
//        name = "Vault 42",
//        modifiedTime = 0,
//        createdTime = 0,
//        star = false,
//        starredTime = 30,
//        isExpanded = true,
//        sortOrder = WidgetState.SortOrder.NAME_ASC,
//        folders = listOf(
//            WidgetState.FolderState(
//                path = "/folder1",
//                name = "folder1",
//                modifiedTime = 200,
//                createdTime = 10,
//                isExpanded = true,
//                star = false,
//                starredTime = 30,
//                sortOrder = WidgetState.SortOrder.NAME_ASC,
//                files = listOf(
//                    WidgetState.FileState(
//                        path = "/folder1/file1",
//                        name = "file1",
//                        modifiedTime = 200,
//                        createdTime = 10,
//                        star = false,
//                        starredTime = 30,
//                        description = "11xxxxxxxxxxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxxx",
//                    )
//                ),
//                folders = listOf(
//                    WidgetState.FolderState(
//                        path = "/folder1/folder2",
//                        name = "folder2",
//                        modifiedTime = 100,
//                        createdTime = 20,
//                        isExpanded = false,
//                        star = true,
//                        starredTime = 30,
//                        sortOrder = WidgetState.SortOrder.NAME_ASC,
//                        files = listOf(
//                            WidgetState.FileState(
//                                path = "/folder1/folder2/file1",
//                                name = "file1",
//                                modifiedTime = 0,
//                                createdTime = 0,
//                                star = false,
//                                starredTime = 30,
//                                description = "121xxxxxxxxxxxxx",
//                            )
//                        ),
//                        folders = listOf()
//                    ),
//                    WidgetState.FolderState(
//                        path = "/folder1/folder3",
//                        name = "folder3",
//                        modifiedTime = 200,
//                        createdTime = 10,
//                        isExpanded = false,
//                        star = false,
//                        starredTime = 30,
//                        sortOrder = WidgetState.SortOrder.NAME_ASC,
//                        files = listOf(),
//                        folders = listOf()
//                    ),
//                    WidgetState.FolderState(
//                        path = "/folder1/folder4",
//                        name = "folder4",
//                        modifiedTime = 200,
//                        createdTime = 10,
//                        isExpanded = false,
//                        star = false,
//                        starredTime = 30,
//                        sortOrder = WidgetState.SortOrder.NAME_ASC,
//                        files = listOf(),
//                        folders = listOf()
//                    ),
//                    WidgetState.FolderState(
//                        path = "/folder1/folder5",
//                        name = "folder5",
//                        modifiedTime = 200,
//                        createdTime = 10,
//                        isExpanded = false,
//                        star = false,
//                        starredTime = 30,
//                        sortOrder = WidgetState.SortOrder.NAME_ASC,
//                        files = listOf(),
//                        folders = listOf()
//                    ),
//                    WidgetState.FolderState(
//                        path = "/folder1/folder6",
//                        name = "folder6",
//                        modifiedTime = 200,
//                        createdTime = 10,
//                        isExpanded = false,
//                        star = false,
//                        starredTime = 30,
//                        sortOrder = WidgetState.SortOrder.NAME_ASC,
//                        files = listOf(),
//                        folders = listOf()
//                    ),
//                    WidgetState.FolderState(
//                        path = "/folder1/folder7",
//                        name = "folder7",
//                        modifiedTime = 200,
//                        createdTime = 10,
//                        isExpanded = false,
//                        star = false,
//                        starredTime = 30,
//                        sortOrder = WidgetState.SortOrder.NAME_ASC,
//                        files = listOf(),
//                        folders = listOf()
//                    ),
//                    WidgetState.FolderState(
//                        path = "/folder1/folder8",
//                        name = "folder8",
//                        modifiedTime = 200,
//                        createdTime = 10,
//                        isExpanded = false,
//                        star = false,
//                        starredTime = 30,
//                        sortOrder = WidgetState.SortOrder.NAME_ASC,
//                        files = listOf(),
//                        folders = listOf()
//                    ),
//                )
//            ),
//            WidgetState.FolderState(
//                path = "/folder2",
//                name = "folder2",
//                modifiedTime = 100,
//                createdTime = 20,
//                isExpanded = false,
//                star = false,
//                starredTime = 30,
//                sortOrder = WidgetState.SortOrder.NAME_ASC,
//                files = listOf(),
//                folders = listOf()
//            )
//        ),
//        files = listOf(
//            WidgetState.FileState(
//                path = "/file1",
//                name = "file1",
//                modifiedTime = 200,
//                createdTime = 10,
//                star = false,
//                starredTime = 30,
//                description = "1xxxxxxxxxxxxx",
//            ),
//            WidgetState.FileState(
//                path = "/file2",
//                name = "file2",
//                modifiedTime = 100,
//                createdTime = 20,
//                star = true,
//                starredTime = 30,
//                description = "2xxxxxxxxxxxxx",
//            ),
//            WidgetState.FileState(
//                path = "/file3",
//                name = "file3",
//                modifiedTime = 100,
//                createdTime = 20,
//                star = true,
//                starredTime = 30,
//                description = "2xxxxxxxxxxxxx",
//            ),
//            WidgetState.FileState(
//                path = "/file4",
//                name = "file4",
//                modifiedTime = 100,
//                createdTime = 20,
//                star = false,
//                starredTime = 30,
//                description = "2xxxxxxxxxxxxx",
//            ),
//            WidgetState.FileState(
//                path = "/file5",
//                name = "file5",
//                modifiedTime = 100,
//                createdTime = 20,
//                star = false,
//                starredTime = 30,
//                description = "2xxxxxxxxxxxxx",
//            ),
//            WidgetState.FileState(
//                path = "/file6",
//                name = "file6",
//                modifiedTime = 100,
//                createdTime = 20,
//                star = false,
//                starredTime = 30,
//                description = "2xxxxxxxxxxxxx",
//            ),
//            WidgetState.FileState(
//                path = "/file7",
//                name = "file7",
//                modifiedTime = 100,
//                createdTime = 20,
//                star = false,
//                starredTime = 30,
//                description = "2xxxxxxxxxxxxx",
//            ),
//        )
//    )