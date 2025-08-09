/*
 * Copyright (C) 2023 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.reminder.feature.home.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsStartWidth
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nlab.reminder.core.androidx.compose.ui.DelayedContent
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.androidx.compose.ui.ColorPressButton
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.component.tag.edit.ui.compose.TagEditStateHandler
import com.nlab.reminder.core.component.tag.ui.compose.TagCard
import com.nlab.reminder.core.designsystem.compose.component.PlaneatLoadingContent
import com.nlab.reminder.core.designsystem.compose.icon.PlaneatIcons
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.statekit.store.androidx.lifecycle.compose.retainedStore
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.feature.home.HomeAction
import com.nlab.reminder.feature.home.HomeEnvironment
import com.nlab.reminder.feature.home.HomeReduce
import com.nlab.reminder.feature.home.HomeUiState
import com.nlab.reminder.feature.home.StateSyncFlow
import com.nlab.statekit.bootstrap.DeliveryStarted
import com.nlab.statekit.bootstrap.collectAsBootstrap
import com.nlab.statekit.store.createStore

/**
 * @author Doohyun
 */
@Composable
internal fun HomeScreen(
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier,
    homeEnvironment: HomeEnvironment = hiltViewModel()
) {
    val store = retainedStore {
        createStore(
            coroutineScope = storeMaterialScope,
            initState = HomeUiState.Loading,
            reduce = HomeReduce(environment = homeEnvironment),
            bootstrap = StateSyncFlow(homeEnvironment).collectAsBootstrap(
                started = DeliveryStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
            )
        )
    }
    val uiState: HomeUiState by store.state.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        modifier = modifier,
        onTodayCategoryClicked = {
            store.dispatch(HomeAction.TodayCategoryClicked)
            onTodayCategoryClicked()
        },
        onTimetableCategoryClicked = {
            store.dispatch(HomeAction.TimetableCategoryClicked)
            onTimetableCategoryClicked()
        },
        onAllCategoryClicked = {
            store.dispatch(HomeAction.AllCategoryClicked)
            onAllCategoryClicked()
        },
        onTagClicked = {
            // TODO implements
        },
        onTagLongClicked = { tag -> store.dispatch(HomeAction.TagLongClicked(tag)) },
        onNewPlanClicked = {
            // TODO implements
        },
        onTagRenameRequestClicked = { store.dispatch(HomeAction.TagRenameRequestClicked) },
        onTagDeleteRequestClicked = { store.dispatch(HomeAction.TagDeleteRequestClicked) },
        onTagRenameInputReady = { store.dispatch(HomeAction.TagRenameInputReady) },
        onTagRenameInputted = { input -> store.dispatch(HomeAction.TagRenameInputted(text = input)) },
        onTagRenameConfirmClicked = { store.dispatch(HomeAction.TagRenameConfirmClicked) },
        onTagMergeCancelClicked = { store.dispatch(HomeAction.TagReplaceCancelClicked) },
        onTagMergeConfirmClicked = { store.dispatch(HomeAction.TagReplaceConfirmClicked) },
        onTagDeleteConfirmClicked = { store.dispatch(HomeAction.TagDeleteConfirmClicked) },
        onTagEditCancelClicked = { store.dispatch(HomeAction.TagEditCancelClicked) }
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
    onNewPlanClicked: () -> Unit,
    onTagRenameRequestClicked: () -> Unit,
    onTagDeleteRequestClicked: () -> Unit,
    onTagRenameInputReady: () -> Unit,
    onTagRenameInputted: (String) -> Unit,
    onTagRenameConfirmClicked: () -> Unit,
    onTagMergeCancelClicked: () -> Unit,
    onTagMergeConfirmClicked: () -> Unit,
    onTagDeleteConfirmClicked: () -> Unit,
    onTagEditCancelClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is HomeUiState.Loading -> {
            DelayedContent(delayTimeMillis = 500) {
                PlaneatLoadingContent()
            }
        }

        is HomeUiState.Success -> {
            DelayedContent(delayTimeMillis = 100) {
                HomeContents(
                    modifier = modifier,
                    todayCount = uiState.todayScheduleCount,
                    timetableCount = uiState.timetableScheduleCount,
                    allCount = uiState.allScheduleCount,
                    tags = uiState.tags,
                    onTodayCategoryClicked = onTodayCategoryClicked,
                    onTimetableCategoryClicked = onTimetableCategoryClicked,
                    onAllCategoryClicked = onAllCategoryClicked,
                    onTagClicked = onTagClicked,
                    onTagLongClicked = onTagLongClicked,
                    onNewPlanClicked = onNewPlanClicked
                )
            }
            TagEditStateHandler(
                state = uiState.tagEditState,
                onCompleted = onTagEditCancelClicked,
                onRenameRequestClicked = onTagRenameRequestClicked,
                onDeleteRequestClicked = onTagDeleteRequestClicked,
                onRenameInputReady = onTagRenameInputReady,
                onRenameInputted = onTagRenameInputted,
                onRenameConfirmClicked = onTagRenameConfirmClicked,
                onMergeCancelClicked = onTagMergeCancelClicked,
                onMergeConfirmClicked = onTagMergeConfirmClicked,
                onDeleteConfirmClicked = onTagDeleteConfirmClicked
            )
        }
    }
}

@Composable
private fun HomeContents(
    todayCount: NonNegativeLong,
    timetableCount: NonNegativeLong,
    allCount: NonNegativeLong,
    tags: List<Tag>,
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
    onNewPlanClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        val bodyScrollState = rememberScrollState()
        val bodyPaddingTop = 32.dp
        val bodyPaddingBottom = 75.dp
        HomeBody(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(bodyScrollState)
                .safeDrawingPadding()
                .padding(start = 20.dp, top = bodyPaddingTop, end = 20.dp, bottom = bodyPaddingBottom),
            todayCount = todayCount,
            timetableCount = timetableCount,
            allCount = allCount,
            tags = tags,
            onTodayCategoryClicked = onTodayCategoryClicked,
            onTimetableCategoryClicked = onTimetableCategoryClicked,
            onAllCategoryClicked = onAllCategoryClicked,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked,
        )
        HeadBlurLayer(
            modifier = Modifier.align(Alignment.TopCenter),
            startAnimOffset = 20.dp,
            bodyPaddingTop = bodyPaddingTop,
            bodyScrollState = bodyScrollState
        )
        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            contentHeight = 50.dp,
            bodyBottomPadding = bodyPaddingBottom,
            bodyScrollState = bodyScrollState,
            onNewPlanClicked = onNewPlanClicked
        )
    }
}

@Composable
private fun HomeBody(
    todayCount: NonNegativeLong,
    timetableCount: NonNegativeLong,
    allCount: NonNegativeLong,
    tags: List<Tag>,
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Logo()
        HomeTitle(
            textRes = StringIds.home_category_header,
            modifier = Modifier.padding(top = 25.dp)
        )
        CategoryCardsRow(
            modifier = Modifier
                .widthIn(max = 478.dp)
                .fillMaxWidth(),
            todayCount = todayCount,
            timetableCount = timetableCount,
            allCount = allCount,
            onTodayCategoryClicked = onTodayCategoryClicked,
            onTimetableCategoryClicked = onTimetableCategoryClicked,
            onAllCategoryClicked = onAllCategoryClicked
        )
        HomeTitle(
            textRes = StringIds.home_tag_header,
            modifier = Modifier.padding(top = 25.dp)
        )
        TagCards(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            tags = tags,
            onTagClicked = onTagClicked,
            onTagLongClicked = onTagLongClicked
        )
    }
}

@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .width(120.dp)
            .height(30.dp),
        painter = painterResource(id = DrawableIds.ic_logo),
        contentDescription = null,
        tint = PlaneatTheme.colors.content1
    )
}

@Composable
private fun HomeTitle(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(textRes),
        style = PlaneatTheme.typography.titleMedium,
        color = PlaneatTheme.colors.content1,
        modifier = modifier.padding(vertical = 15.dp)
    )
}

@Composable
internal fun CategoryCardsRow(
    todayCount: NonNegativeLong,
    timetableCount: NonNegativeLong,
    allCount: NonNegativeLong,
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        TodayCategoryCard(
            modifier = Modifier.weight(1f),
            remainCount = todayCount,
            onClick = onTodayCategoryClicked
        )

        Spacer(modifier = Modifier.width(14.dp))

        TimetableCategoryCard(
            modifier = Modifier.weight(1f),
            remainCount = timetableCount,
            onClick = onTimetableCategoryClicked
        )

        Spacer(modifier = Modifier.width(14.dp))

        AllCategoryCard(
            modifier = Modifier.weight(1f),
            remainCount = allCount,
            onClick = onAllCategoryClicked
        )
    }
}

@Composable
private fun TodayCategoryCard(
    remainCount: NonNegativeLong,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    BasicCategoryCard(
        name = stringResource(StringIds.home_category_today),
        remainCount = remainCount.value,
        icon = {
            Image(
                imageVector = PlaneatIcons.todayLogo,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4883f)
                    .aspectRatio(1f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun TimetableCategoryCard(
    remainCount: NonNegativeLong,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    BasicCategoryCard(
        name = stringResource(StringIds.home_category_timetable),
        remainCount = remainCount.value,
        icon = {
            // case1: If you use webp, the image quality is not good when in landscape mode.
            // case2: When using svg, the square is not drawn properly. (No problem when using view system)
            // case3: Resolve when using image vector
            Image(
                imageVector = PlaneatIcons.timetableLogo,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun AllCategoryCard(
    remainCount: NonNegativeLong,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    BasicCategoryCard(
        name = stringResource(StringIds.home_category_all),
        remainCount = remainCount.value,
        icon = {
            Image(
                imageVector = PlaneatIcons.allLogo,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4232f)
                    .aspectRatio(18.22f / 15.93f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun BasicCategoryCard(
    name: String,
    remainCount: Long,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onClickLabel: String = name,
) {
    Box(modifier = modifier) {
        CategoryCardBackground(onClick, onClickLabel)
        Column(
            modifier = Modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.1346f))
            CategoryIcon(icon)
            Spacer(modifier = Modifier.fillMaxHeight(0.048f))
            CategoryTitleText(name)
            Spacer(modifier = Modifier.fillMaxHeight(0.17f))
            CategoryCountText(remainCount)
        }
    }
}

@Composable
private fun CategoryCardBackground(
    onClick: () -> Unit = {},
    onClickLabel: String? = null
) {
    Spacer(
        modifier = Modifier
            .aspectRatio(1 / 1.625f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = PlaneatTheme.colors.bgCard1Ripple),
                onClick = onClick,
                onClickLabel = onClickLabel,
                role = Role.Tab
            )
            .background(PlaneatTheme.colors.bgCard1)
    )
}

@Composable
private fun CategoryIcon(icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.4479f)
            .aspectRatio(1f)
            .background(PlaneatTheme.colors.bg1, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun CategoryTitleText(text: String) {
    Text(
        text = text,
        style = PlaneatTheme.typography.bodyMedium,
        color = PlaneatTheme.colors.content2
    )
}

@Composable
private fun CategoryCountText(count: Long) {
    Text(
        text = count.toString(),
        style = PlaneatTheme.typography.bodyLarge,
        color = PlaneatTheme.colors.content1,
        fontFamily = PlaneatTheme.extraFont.aggro
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagCards(
    tags: List<Tag>,
    modifier: Modifier = Modifier,
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PlaneatTheme.colors.bgCard1),
    ) {
        if (tags.isEmpty()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = LocalContext.current.getString(StringIds.common_tag_empty),
                style = PlaneatTheme.typography.bodyMedium,
                color = PlaneatTheme.colors.content2,
            )
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 11.5.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.Start)
            ) {
                tags.forEach { tag ->
                    key(tag.id) {
                        TagCard(
                            tag = tag,
                            modifier = Modifier.padding(vertical = 6.5.dp),
                            onClick = { onTagClicked(tag) },
                            onLongClick = { onTagLongClicked(tag) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeadBlurLayer(
    startAnimOffset: Dp,
    bodyPaddingTop: Dp,
    bodyScrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val containerColor = PlaneatTheme.colors.bgCard1
    val lineColor = PlaneatTheme.colors.bgLine1
    var computedHeightToPx by remember { mutableIntStateOf(0) }
    val startAnimOffsetToPx = with(LocalDensity.current) { startAnimOffset.toPx() }
    val bodyPaddingTopToPx = with(LocalDensity.current) { bodyPaddingTop.toPx() }
    val alphaState by remember(computedHeightToPx, startAnimOffsetToPx, bodyPaddingTopToPx, bodyScrollState) {
        val maxBottomContainerAnimPx = (bodyPaddingTopToPx - startAnimOffsetToPx)
        derivedStateOf {
            val curState = bodyScrollState.value
            when {
                computedHeightToPx == 0 -> 0f
                curState < startAnimOffsetToPx -> 0f
                curState > bodyPaddingTopToPx -> 1f
                else -> (curState - startAnimOffsetToPx) / maxBottomContainerAnimPx
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .onSizeChanged { computedHeightToPx = it.height }
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(color = containerColor.copy(alpha = 0.96f * alphaState)) }
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.BottomCenter)
                .drawBehind { drawRect(color = lineColor.copy(alpha = alphaState)) }
        )
    }
}

@Composable
private fun BottomActions(
    contentHeight: Dp,
    bodyBottomPadding: Dp,
    bodyScrollState: ScrollState,
    onNewPlanClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var computedHeightToPx by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { computedHeightToPx = it.height }
    ) {
        if (computedHeightToPx != 0) {
            BottomBlurLayer(
                modifier = Modifier.matchParentSize(),
                computedHeightToPx = computedHeightToPx,
                alphaAnimScrollTriggerOffsetToPx = with(LocalDensity.current) {
                    remember(contentHeight, bodyBottomPadding) { (bodyBottomPadding - contentHeight).toPx().toInt() }
                },
                bodyScrollState = bodyScrollState,
            )
        }
        Row {
            Spacer(modifier = Modifier.windowInsetsStartWidth(WindowInsets.displayCutout))
            Column {
                NewPlanButton(
                    modifier = Modifier
                        .height(contentHeight)
                        .padding(start = 20.dp, end = 10.dp),
                    onClick = onNewPlanClicked
                )
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun BottomBlurLayer(
    bodyScrollState: ScrollState,
    computedHeightToPx: Int,
    alphaAnimScrollTriggerOffsetToPx: Int,
    modifier: Modifier = Modifier
) {
    val containerColor = PlaneatTheme.colors.bgCard1
    val lineColor = PlaneatTheme.colors.bgLine1
    val alphaState by remember(computedHeightToPx, alphaAnimScrollTriggerOffsetToPx, bodyScrollState) {
        val computedHeightWithOffsetToPx = computedHeightToPx + alphaAnimScrollTriggerOffsetToPx
        val maxBottomContainerAnimPx = (computedHeightWithOffsetToPx - computedHeightToPx).toFloat()
        derivedStateOf {
            if (bodyScrollState.maxValue == Int.MAX_VALUE) 0f
            else {
                val remainScrollToPx = bodyScrollState.maxValue - bodyScrollState.value
                val visibleClipToPaddingHeight =
                    maxOf(computedHeightWithOffsetToPx - remainScrollToPx, 0)
                val bottomContainerAnimPx = maxOf(visibleClipToPaddingHeight - computedHeightToPx, 0)
                1f - bottomContainerAnimPx / maxBottomContainerAnimPx
            }
        }
    }
    Box(modifier = modifier) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .drawBehind { drawRect(color = containerColor.copy(alpha = 0.925f * alphaState)) }
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.TopCenter)
                .drawBehind { drawRect(color = lineColor.copy(alpha = alphaState)) }
        )
    }
}

@Composable
private fun NewPlanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ColorPressButton(
        contentColor = PlaneatTheme.colors.point1,
        modifier = modifier,
        onClick = throttleClick(onClick = onClick)
    ) { contentColor ->
        Icon(
            modifier = Modifier
                .width(35.73.dp)
                .height(20.69.dp),
            painter = painterResource(id = DrawableIds.ic_new_plan),
            contentDescription = null,
            tint = contentColor
        )
        Text(
            text = stringResource(StringIds.new_schedule_label),
            style = PlaneatTheme.typography.titleMedium,
            color = contentColor
        )
    }
}

@Previews
@Composable
private fun HomeContentsPreview() {
    PlaneatTheme {
        Box(modifier = Modifier.background(PlaneatTheme.colors.bg1)) {
            HomeContents(
                todayCount = 10L.toNonNegativeLong(),
                timetableCount = 20L.toNonNegativeLong(),
                allCount = 30L.toNonNegativeLong(),
                tags = (1L..100).map { index ->
                    Tag(
                        id = TagId(rawId = index),
                        name = "TagName $index".toNonBlankString()
                    )
                },
                onTodayCategoryClicked = {},
                onTimetableCategoryClicked = {},
                onAllCategoryClicked = {},
                onTagClicked = {},
                onTagLongClicked = {},
                onNewPlanClicked = {}
            )
        }
    }
}