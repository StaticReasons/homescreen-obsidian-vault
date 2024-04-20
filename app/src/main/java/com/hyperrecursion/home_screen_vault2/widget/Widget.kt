package com.hyperrecursion.home_screen_vault2.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.LazyListScope
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentSize
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import com.hyperrecursion.home_screen_vault2.AppConfig
import com.hyperrecursion.home_screen_vault2.AppConfigActivity
import com.hyperrecursion.home_screen_vault2.AppConfigRepository
import com.hyperrecursion.home_screen_vault2.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import java.net.URLEncoder

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetStateRepository(): WidgetStateRepository
    fun appConfigRepository(): AppConfigRepository
}

class Widget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    companion object {
        val foldedFolderHeight: Dp = 36.dp
        val fileHeight: Dp = 54.dp
        val defaultFirstViewHeight: Dp = 162.dp

        val foldedFolderWidth: Dp = 116.dp
        val fileMinWidth: Dp = 144.dp

        val defaultPadding: Dp = 6.dp

        const val ROOT_PATH: String = "/"   // idk how to deal with it. Current path mechanisms are apparently not elegant enough
        val sortOrderIconResIds: Map<WidgetState.SortOrder, Int> = mapOf(
            WidgetState.SortOrder.NAME_ASC to R.drawable.alphabetical_sorting,
            WidgetState.SortOrder.NAME_DESC to R.drawable.alphabetical_sorting_2,
            WidgetState.SortOrder.MODIFIED_ASC to R.drawable.sort_clock_ascending_outline,
            WidgetState.SortOrder.MODIFIED_DESC to R.drawable.sort_clock_descending_outline,
        )
    }

    /*
        About dimensions...
        On my Honor Phone (Android 12) the width in dp w=kx+b, x=Cells Columns, k=80dp, b=-16dp
        On Pixel 8 API 34 Emulator k=76.5714dp, b=-16dp
        We need to determine a file cell's minimum width s.t. it could fit the case of 1 FoldedFolder Column + 1 Column of Files
        Also we can determine the width of FoldedFolder Column --- which allows a way to eliminate the damnOON b bias
        So we can just assume the file's cell width to be 80dp...or 72dp?
        Just in case for the damnOON k's smaller than 80dp like in the emulator. Also we like multiples of 6.
        Then, the FoldedFolder Column's width = -16dp - 2 * 6dp (initial padding) + 2 * 72dp = 116dp

        Summary: FoldedFolder Column's width = 116dp; File Column's width = 72dp
     */

    private fun getWidgetStateRepository(context: Context): WidgetStateRepository {
        return EntryPointAccessors
            .fromApplication(context, WidgetEntryPoint::class.java)
            .widgetStateRepository()
    }

    private fun getAppConfigRepository(context: Context): AppConfigRepository {
        return EntryPointAccessors
            .fromApplication(context, WidgetEntryPoint::class.java)
            .appConfigRepository()
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
//            val widgetState = WidgetState.testState
            val coroutineScope = rememberCoroutineScope()
            val widgetStateRepository = remember { getWidgetStateRepository(context) }
            val repoWidgetState by widgetStateRepository.getWidgetStateFlow()
                .collectAsState(initial = WidgetState.defaultState)
            val appConfigRepository = remember { getAppConfigRepository(context) }
            val appConfig =
                appConfigRepository.getAppConfigFlow().collectAsState(initial = AppConfig()).value
            val uiState by remember {
                mutableStateOf(WidgetUiState.fromRepoWidgetState(repoWidgetState))
            }
            uiState.updateFromRepoState(repoWidgetState)
            GlanceTheme(colors = widgetColors) {
//                ExampleContent(onOpen = {})
                WidgetContent(
                    uiState = uiState,
                    widthCorrectionFactor = appConfig.widthCorrection,
                    enableDebugLine = appConfig.enableDebugLine,
                    makeOpenAction = { path: String ->
                        // Reference: https://help.obsidian.md/Extending+Obsidian/Obsidian+URI
                        val vault = appConfig.vaultPath.substringAfterLast("/")
                        val encodedVault = URLEncoder.encode(vault, "UTF-8").replace("+", "%20")
                        val file = path.removePrefix("/")
                        val encodedFile = URLEncoder.encode(file, "UTF-8").replace("+", "%20")
                        val uri = Uri.parse("obsidian://open?vault=$encodedVault&file=$encodedFile")
                        val openNote = Intent(Intent.ACTION_VIEW, uri)
//                        Log.d("WidgetOpenIntent", "Opening Uri: $uri")
                        return@WidgetContent actionStartActivity(openNote)
                    },
                    toSaveState = {
                        coroutineScope.launch {
                            widgetStateRepository.updateByWidget(uiState.toWidgetRepoState())
                        }
                    },
                    toRefresh = {
                        coroutineScope.launch {
                            widgetStateRepository.updateByNewScan()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WidgetContent(
    uiState: WidgetUiState,
    enableDebugLine: Boolean = false,
    widthCorrectionFactor: Double = 1.0,
    makeOpenAction: (String) -> Action,
    toRefresh: () -> Unit = {},
    toSaveState: () -> Unit = {}
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(widgetColors.background),
        verticalAlignment = Alignment.Top
    ) {

        val rootState = uiState.rootState
        val widgetWidth = LocalSize.current.width * widthCorrectionFactor.toFloat()

        val pageParams = calculatePageParams(
            state = rootState,
            currentPage = rootState.currentPage,
            depth = 0,
            widgetWidth = widgetWidth,
        )

        if (enableDebugLine)
            DebugLine(width = widgetWidth)

        RootBar(
            vaultName = rootState.name,
            sortOrder = rootState.sortOrder,
            pageParams = pageParams,
            onSortClicked = { rootState.sortOrder = rootState.sortOrder.next(); toSaveState() },
            onPageChanged = { rootState.currentPage = it },
            toRefresh = toRefresh
        )
        LazyColumn(
            modifier = GlanceModifier.fillMaxSize(),
        ) {
            folderView(
                state = rootState,
                pageParams = pageParams,
                widgetWidth = widgetWidth,
                makeOpenAction = makeOpenAction,
                toSaveState = toSaveState,
            )
        }
    }
}

fun Int.ceilDiv(divisor: Int): Int {
    return (this + divisor - 1) / divisor
}

fun calculateFirstViewHeight(
    folderState: WidgetUiState.FolderState,
    fileColumns: Int,
): Dp {
    return minOf(
        maxOf(
            folderState.foldedFolders.count { it.star } * Widget.foldedFolderHeight,
            folderState.files.count { it.star }.ceilDiv(fileColumns) * Widget.fileHeight,
            Widget.defaultFirstViewHeight
        ),
        maxOf(
            folderState.foldedFolders.count() * Widget.foldedFolderHeight,
            folderState.files.count().ceilDiv(fileColumns) * Widget.fileHeight
        )
    )
}

// Always return currentPage as 1;
// Use it update existed pageParams
fun calculatePageParams(
    state: WidgetUiState.FolderState,
    currentPage: Int = 1,
    depth: Int = 0,
    widgetWidth: Dp? = null,
): WidgetUiState.PageParams {
    val fileGridColumns = if (widgetWidth == null) 1
    else ((widgetWidth - 2 * depth * Widget.defaultPadding
            - if (state.foldedFolders.isEmpty()) 0.dp else Widget.foldedFolderWidth) / Widget.fileMinWidth).toInt()
        .coerceAtLeast(1)
//    Log.d("FolderView", "fileGridColumns: $fileGridColumns, widgetWidth: $widgetWidth")

    val firstViewHeight = calculateFirstViewHeight(state, fileGridColumns)
    if (firstViewHeight < maxOf(Widget.foldedFolderHeight, Widget.fileHeight))
        return WidgetUiState.PageParams(
            0, 0,
            currentPage,
            0, 0
        )
    val foldersPageSize = (firstViewHeight / Widget.foldedFolderHeight).toInt()
    val totalPagesFolders =
        if (state.foldedFolders.isEmpty()) 0 else state.foldedFolders.count()
            .ceilDiv(foldersPageSize)

    val filesPageSize =
        (firstViewHeight / Widget.fileHeight).toInt() * fileGridColumns
    val totalPagesFiles =
        if (state.files.isEmpty()) 0 else state.files.count().ceilDiv(filesPageSize)
    return WidgetUiState.PageParams(
        totalPagesFolders, foldersPageSize,
        currentPage,
        totalPagesFiles, filesPageSize,
        fileGridColumns
    )
}

// Debug Line: A horizontal line with specified width,
// to show how long is the system provided width after correction
@Composable
fun DebugLine(
    width: Dp,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(widgetColors.onPrimary)
            .width(width)
    ) {}
}

// Page Navigator
@Composable
fun PageNavigator(
    pageParams: WidgetUiState.PageParams,
    onPageChanged: (Int) -> Unit = {},
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier
            .fillMaxHeight().wrapContentWidth()
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val leftEnabled = pageParams.currentPage > 1
        val rightEnabled = pageParams.currentPage < pageParams.maxPage
        CircleIconButton(
            ImageProvider(
                if (leftEnabled) R.drawable.round_keyboard_arrow_left_24
                else R.drawable.empty_drawble
            ),
            contentDescription = "Prev",
            contentColor = widgetColors.onPrimary,
            backgroundColor = null,
            onClick = { if (leftEnabled) onPageChanged(maxOf(1, pageParams.currentPage - 1)) },
            modifier = modifier.defaultWeight().wrapContentHeight(),
        )
        // text: "totalPagesFolder / currentPage / totalPagesFiles"
        Text(
            text = "${pageParams.totalPagesFolders} / ${pageParams.currentPage} / ${pageParams.totalPagesFiles}",
//            text = "${pageParams.totalPagesFolders + 9}/${pageParams.currentPage}9/${pageParams.totalPagesFiles}9",    //Test
            style = WidgetTypography.labelNormal.copy(
                color = widgetColors.onPrimary,
                textAlign = TextAlign.Center,
            ),
            modifier = modifier.wrapContentHeight().width(60.dp),
            maxLines = 1,
        )
        CircleIconButton(
            ImageProvider(
                if (rightEnabled) R.drawable.round_keyboard_arrow_right_24
                else R.drawable.empty_drawble
            ),
            contentDescription = "Prev",
            contentColor = widgetColors.onPrimary,
            backgroundColor = null,
            onClick = {
                if (rightEnabled) onPageChanged(
                    minOf(
                        pageParams.maxPage,
                        pageParams.currentPage + 1
                    )
                )
            },
            modifier = modifier.defaultWeight().wrapContentHeight(),
        )
    }
}


@Composable
fun RootBar(
    vaultName: String,
    sortOrder: WidgetState.SortOrder,
    pageParams: WidgetUiState.PageParams,
    onSortClicked: () -> Unit,
    onPageChanged: (Int) -> Unit,
    toRefresh: () -> Unit,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth().height(36.dp)
            .padding(start = 6.dp, end = 6.dp)
            .background(widgetColors.primary)
            .cornerRadius(12.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            modifier = modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {
            // Button - Settings
            CircleIconButton(
                ImageProvider(R.drawable.round_settings_24),
                contentDescription = "Settings",
                contentColor = widgetColors.onPrimary,
                backgroundColor = null,
                onClick = actionStartActivity<AppConfigActivity>(),
                modifier = modifier.wrapContentSize(),
            )
            // Title
            Text(
                text = vaultName,
                modifier = modifier.defaultWeight().padding(start = 6.dp),
                style = WidgetTypography.labelNormal.copy(
                    color = widgetColors.onPrimary,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
            )
        }
        Row(
            modifier = modifier.fillMaxHeight().wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.End,
        ) {
            if (pageParams.maxPage > 1)
                PageNavigator(pageParams = pageParams, onPageChanged = onPageChanged)
            // Button - Sort
            CircleIconButton(
                ImageProvider(Widget.sortOrderIconResIds[sortOrder] ?: R.drawable.round_sort_24),
                contentDescription = "Sort",
                contentColor = widgetColors.onPrimary,
                backgroundColor = null,
                onClick = { onSortClicked() },
                modifier = modifier.wrapContentSize(),
            )
            // Button - Refresh
            CircleIconButton(
                ImageProvider(R.drawable.round_refresh_24),
                contentDescription = "Refresh",
                contentColor = widgetColors.onPrimary,
                backgroundColor = null,
                onClick = toRefresh,
                modifier = modifier.wrapContentSize(),
            )
        }
    }
}


// Magic
fun LazyListScope.folderView(
    state: WidgetUiState.FolderState,
    sourcePath: String = Widget.ROOT_PATH,
    pageParams: WidgetUiState.PageParams,
    depth: Int = 0,
    widgetWidth: Dp? = null,
    modifier: GlanceModifier = GlanceModifier,
    makeOpenAction: (String) -> Action,
    toSaveState: () -> Unit,
) {
    val lazyHeight = calculateFirstViewHeight(state, pageParams.pageColumnsFiles)
    val displayFolders = state.foldedFolders.subList(
        ((pageParams.currentPage.coerceAtMost(pageParams.totalPagesFolders) - 1)
                * pageParams.pageSizeFolders)
            .coerceIn(0, state.foldedFolders.count()),
        (pageParams.currentPage.coerceAtMost(pageParams.totalPagesFolders)
                * pageParams.pageSizeFolders)
            .coerceIn(0, state.foldedFolders.count()),
    )
    val displayFiles = state.files.subList(
        ((pageParams.currentPage.coerceAtMost(pageParams.totalPagesFiles) - 1)
                * pageParams.pageSizeFiles)
            .coerceIn(0, state.files.count()),
        (pageParams.currentPage.coerceAtMost(pageParams.totalPagesFiles)
                * pageParams.pageSizeFiles)
            .coerceIn(0, state.files.count()),
    ).chunked(pageParams.pageColumnsFiles)
//    Log.d(
//        "FolderView",
//        "FolderView: $sourcePath, $depth, ${pageParams.pageColumnsFiles}, ${pageParams.pageSizeFiles}"
//    )
    item {
        // FirstView = Folded Folders + File Grid
        if (pageParams.maxPage > 0) {
            Row(
                modifier = modifier
                    .fillMaxWidth().height(lazyHeight + 2 * 6.dp)
                    .padding(
                        horizontal = Widget.defaultPadding * (depth + 1),
                        vertical = Widget.defaultPadding
                    ),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // Folded Folders
                if (pageParams.totalPagesFolders != 0) {
                    Column(
                        modifier = modifier
                            .width(Widget.foldedFolderWidth).fillMaxHeight()
                            .padding(end = Widget.defaultPadding),
                    ) {
                        displayFolders.forEach {
                            FoldedFolder(
                                state = it,
                                sourcePath = sourcePath,
                                modifier = modifier,
                                toSaveState = toSaveState,
                            )
                        }
                    }
                }
                // File Grid
                Column(
                    modifier = modifier
                        .defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.Top,
                ) {
                    displayFiles.map { rowChunk ->
                        Row(
                            modifier = modifier
                                .fillMaxWidth().height(Widget.fileHeight)
                        ) {
                            rowChunk.map {
                                File(
                                    state = it,
                                    sourcePath = sourcePath,
                                    modifier = modifier
                                        .defaultWeight().height(Widget.fileHeight),
                                    makeOpenAction = makeOpenAction,
                                    toSaveState = toSaveState,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    state.expandedFolders.forEach {
        expandedFolder(
            state = it,
            sourcePath = sourcePath,
            sortOrder = it.sortOrder,
            depth = depth + 1,
            widgetWidth = widgetWidth,
            modifier = modifier,
            makeOpenAction = makeOpenAction,
            toSaveState = toSaveState,
        )
    }

}

@Composable
fun ExpandedFolderBar(
    title: String,
    sortOrder: WidgetState.SortOrder,
    star: Boolean,
    pageParams: WidgetUiState.PageParams,
    depth: Int = 0,
    onStarClicked: () -> Unit,
    onSortClicked: () -> Unit,
    onPageChanged: (Int) -> Unit,
    toFold: () -> Unit = {},
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth().height(36.dp)
            .padding(horizontal = Widget.defaultPadding * depth)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(start = 6.dp, end = 6.dp)
                .background(widgetColors.primary)
                .cornerRadius(12.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row(
                modifier = modifier.fillMaxSize()
                    .clickable { toFold() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                // Button - Fold
                CircleIconButton(
                    ImageProvider(R.drawable.round_keyboard_arrow_down_24),
                    contentDescription = "Fold",
                    contentColor = widgetColors.onPrimary,
                    backgroundColor = null,
                    onClick = { toFold(); },
                    modifier = modifier.wrapContentSize(),
                )
                // Title
                Text(
                    text = title,
                    modifier = modifier.defaultWeight(),
                    style = WidgetTypography.labelNormal.copy(
                        color = widgetColors.onPrimary,
                    ),
                    maxLines = 1,
                )
            }
            Row(
                modifier = modifier.fillMaxHeight().wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.End,
            ) {
                // PageNavigator
                if (pageParams.maxPage > 1)
                    PageNavigator(pageParams = pageParams, onPageChanged = onPageChanged)
                // Button - Sort
                CircleIconButton(
                    ImageProvider(
                        Widget.sortOrderIconResIds[sortOrder] ?: R.drawable.round_sort_24
                    ),
                    contentDescription = "Sort",
                    contentColor = widgetColors.onPrimary,
                    backgroundColor = null,
                    onClick = { onSortClicked() },
                    modifier = modifier.wrapContentSize(),
                )
                // Button - Star
                if (star)
                    CircleIconButton(
                        ImageProvider(R.drawable.round_star_24),
                        contentDescription = "Unstar",
                        contentColor = starColor, // FaQ!!!!!!!!!!!!!
                        backgroundColor = null,
                        onClick = { onStarClicked() },
                        modifier = modifier.wrapContentSize(),
                    )
                else
                    CircleIconButton(
                        ImageProvider(R.drawable.round_star_border_24),
                        contentDescription = "Star",
                        contentColor = widgetColors.onPrimary,
                        backgroundColor = null,
                        onClick = { onStarClicked() },
                        modifier = modifier.wrapContentSize(),
                    )
            }
        }
    }
}

// Bar + view
fun LazyListScope.expandedFolder(
    state: WidgetUiState.FolderState,
    sourcePath: String,
    sortOrder: WidgetState.SortOrder,
    depth: Int = 0,
    widgetWidth: Dp? = null,
    modifier: GlanceModifier = GlanceModifier,
    makeOpenAction: (String) -> Action,
    toSaveState: () -> Unit = {},
) {
    val pageParams = calculatePageParams(
        state = state,
        currentPage = state.currentPage,
        depth = depth,
        widgetWidth = widgetWidth,
    )
    item {
        ExpandedFolderBar(
            title = "$sourcePath${state.name}".replaceFirst(Widget.ROOT_PATH, ""),
            sortOrder = sortOrder,
            star = state.star,
            pageParams = pageParams,
            depth = depth,
            onStarClicked = { state.star = state.star.not(); toSaveState() },
            onSortClicked = { state.sortOrder = state.sortOrder.next(); toSaveState() },
            onPageChanged = { state.currentPage = it },
            toFold = { state.isExpanded = false; toSaveState() },
            modifier = modifier,
        )
    }
    folderView(
        state = state,
        pageParams = pageParams,
        sourcePath = "$sourcePath${state.name}/",
        depth = depth,
        widgetWidth = widgetWidth,
        modifier = modifier,
        makeOpenAction = makeOpenAction,
        toSaveState = toSaveState,
    )
}

// Tip: Use clickable modifier instead of onClick for buttons in lists!

@Composable
fun FoldedFolder(
    state: WidgetUiState.FolderState,
    sourcePath: String,
    toSaveState: () -> Unit = {},
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth().height(Widget.foldedFolderHeight)
            .background(widgetColors.primary)
            .cornerRadius(12.dp)
            .padding(horizontal = 6.dp)
            .clickable {
//                Log.d("FoldedFolder", "Folder clicked: $sourcePath${state.name}");
                state.isExpanded = true; toSaveState()
            },
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            ImageProvider(R.drawable.round_keyboard_arrow_right_24),
            contentDescription = "Fold",
            contentColor = widgetColors.onPrimary,
            backgroundColor = null,
            onClick = { state.isExpanded = true; toSaveState() },
            modifier = modifier.wrapContentSize(),
            key = "$sourcePath/${state.name} - arrow"
        )
        Text(
            text = state.name,
            modifier = modifier.defaultWeight(),
            style = WidgetTypography.labelNormal.copy(
                color = widgetColors.onPrimary,
            ),
            maxLines = 1,
        )
        CircleIconButton(
            ImageProvider(if (state.star) R.drawable.round_star_24 else R.drawable.round_star_border_24),
            contentDescription = if (state.star) "Unstar" else "Star",
            contentColor = if (state.star) starColor else widgetColors.onPrimary, // FaQ!!!!!!!!!!!!!
            backgroundColor = null,
            onClick = { state.star = !state.star; toSaveState() },
            modifier = modifier.wrapContentSize(),
            key = "$sourcePath/${state.name} - star"
        )
    }
}

@Composable
fun File(
    state: WidgetUiState.FileState,
    sourcePath: String,
    modifier: GlanceModifier = GlanceModifier,
    makeOpenAction: (String) -> Action,
    toSaveState: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .background(widgetColors.primary)
            .cornerRadius(12.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 12.dp)
                .cornerRadius(12.dp)
                .clickable(makeOpenAction("$sourcePath${state.name}")),
        ) {
            // It's really hard to solve the INCONSISTENT behavior of sp-based font sizes!!!
            // Title
            Row(
                modifier = modifier
                    .fillMaxWidth().height(28.dp)
                    .padding(top = 7.5.dp, bottom = 0.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                Column(
                    modifier = modifier.defaultWeight().fillMaxHeight()
                ) {
                    Text(
                        text = state.name.substringBeforeLast(".").ifEmpty { state.name },
                        style = WidgetTypography.titleSmall.copy(
                            color = widgetColors.onPrimary,
                            textAlign = TextAlign.Left,
                        ),
                        maxLines = 1,
                        modifier = modifier.wrapContentWidth().fillMaxHeight(),
                        )
                }
                Spacer(modifier = modifier.width(18.dp))
            }
            // Description
            Row(
                modifier = modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = state.description.substringBeforeLast("."),
                    style = WidgetTypography.labelSmall.copy(
                        color = widgetColors.onPrimary,
                        textAlign = TextAlign.Left,
                    ),
                    maxLines = 1,
                    modifier = modifier.fillMaxWidth().wrapContentHeight(),
                )
            }
        }
        val starModifier = modifier.wrapContentSize().padding(end = 6.dp, top = 6.dp)
        CircleIconButton(
            ImageProvider(if (state.star) R.drawable.round_star_24 else R.drawable.round_star_border_24),
            contentDescription = if (state.star) "Unstar" else "Star",
            contentColor = if (state.star) starColor else widgetColors.onPrimary, // FaQ!!!!!!!!!!!!!
            backgroundColor = null,
            onClick = {
                state.star = !state.star
                toSaveState()
            },
            modifier = starModifier,
            key = "$sourcePath/${state.name} - star"
        )
    }
}

// Gosh How to preview it? I could see nothing!
//@OptIn(ExperimentalGlancePreviewApi::class)
//@Preview(
//    surface = "1913"
//)
//@Composable
//fun MyContentPreview() {
//    ExampleContent()
//}