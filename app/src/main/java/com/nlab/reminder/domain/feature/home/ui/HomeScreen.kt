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

package com.nlab.reminder.domain.feature.home.ui

import android.content.res.Configuration.*
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nlab.reminder.R
import com.nlab.reminder.core.android.compose.runtime.LoadedContent
import com.nlab.reminder.core.android.compose.runtime.rememberDelayedVisibleState
import com.nlab.reminder.core.android.designsystem.component.ThemeBottomSheetLayout
import com.nlab.reminder.core.android.designsystem.component.ThemeLoadingIndicator
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme
import com.nlab.reminder.domain.common.data.model.Tag
import com.nlab.reminder.domain.common.tag.ui.TagDeleteBottomSheetContent
import com.nlab.reminder.domain.common.tag.ui.TagRenameDialog
import com.nlab.reminder.domain.feature.home.*
import kotlinx.collections.immutable.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
@Composable
internal fun HomeRoot(
    navigateToAllScheduleEnd: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    HomeScreen(
        uiState = viewModel.uiState.collectAsStateWithLifecycle(),
        onTodayCategoryClicked = viewModel::onTodayCategoryClicked,
        onTimetableCategoryClicked = viewModel::onTimetableCategoryClicked,
        onAllCategoryClicked = viewModel::onAllCategoryClicked,
        onTagLongClicked = viewModel::onTagLongClicked,
        onTagRenameRequestClicked = viewModel::onTagRenameRequestClicked,
        onTagRenameKeyboardShown = viewModel::onTagRenameInputKeyboardShown,
        onTagRenameTextChanged = viewModel::onTagRenameInputted,
        onTagRenameConfirmClicked = viewModel::onTagRenameConfirmClicked,
        onTagDeleteRequestClicked = viewModel::onTagDeleteRequestClicked,
        onTagDeleteConfirmClicked = viewModel::onTagDeleteConfirmClicked,
        completeWorkflow = viewModel::completeWorkflow,
        navigateToAllScheduleEnd = navigateToAllScheduleEnd
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeScreen(
    uiState: State<HomeUiState>,
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    onTagLongClicked: (Tag) -> Unit,
    onTagRenameRequestClicked: () -> Unit,
    onTagRenameKeyboardShown: () -> Unit,
    onTagRenameTextChanged: (String) -> Unit,
    onTagRenameConfirmClicked: () -> Unit,
    onTagDeleteRequestClicked: () -> Unit,
    onTagDeleteConfirmClicked: () -> Unit,
    completeWorkflow: () -> Unit,
    navigateToAllScheduleEnd: () -> Unit
) {
    val windowModifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
    val loadingVisibleState = rememberDelayedVisibleState()

    when (val curUi = uiState.value) {
        is HomeUiState.Loading -> Box(modifier = windowModifier) {
            ThemeLoadingIndicator(
                modifier = Modifier.align(Alignment.Center),
                visibleState = loadingVisibleState
            )
        }

        is HomeUiState.Success -> LoadedContent(isDelay = loadingVisibleState.value) {
            val coroutineScope = rememberCoroutineScope()
            val sheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                skipHalfExpanded = true
            )
            ThemeBottomSheetLayout(
                sheetState = sheetState,
                sheetContent = {
                    if (curUi.workflow is HomeWorkflow.TagDelete) {
                        TagDeleteBottomSheetContent(
                            tagName = curUi.workflow.tag.name,
                            usageCount = curUi.workflow.usageCount,
                            modifier = Modifier.navigationBarsPadding(),
                            onCancelClicked = { coroutineScope.launch { sheetState.hide() } },
                            onConfirmClicked = {
                                onTagDeleteConfirmClicked()
                                coroutineScope.launch { sheetState.hide() }
                            }
                        )
                    }
                },
                onHide = completeWorkflow
            ) {
                HomeContent(
                    uiState = curUi,
                    modifier = windowModifier,
                    onTodayCategoryClicked = onTodayCategoryClicked,
                    onTimetableCategoryClicked = onTimetableCategoryClicked,
                    onAllCategoryClicked = onAllCategoryClicked,
                    onTagLongClicked = onTagLongClicked
                )
            }

            when (val workflow = curUi.workflow) {
                is HomeWorkflow.TodaySchedule,
                is HomeWorkflow.TimetableSchedule -> {
                    LaunchedEffect(workflow) { completeWorkflow() }
                }

                is HomeWorkflow.AllSchedule -> {
                    LaunchedEffect(workflow) {
                        completeWorkflow()
                        navigateToAllScheduleEnd()
                    }
                }

                is HomeWorkflow.TagConfig -> {
                    HomeTagConfigDialog(
                        tagName = workflow.tag.name,
                        usageCount = workflow.usageCount,
                        onDismiss = completeWorkflow,
                        onRenameRequestClicked = onTagRenameRequestClicked,
                        onDeleteRequestClicked = onTagDeleteRequestClicked
                    )
                }

                is HomeWorkflow.TagRename -> {
                    TagRenameDialog(
                        initText = workflow.renameText,
                        tagName = workflow.tag.name,
                        usageCount = workflow.usageCount,
                        shouldKeyboardShown = workflow.shouldKeyboardShown,
                        onCancel = completeWorkflow,
                        onTextChanged = onTagRenameTextChanged,
                        onConfirm = {
                            onTagRenameConfirmClicked()
                            completeWorkflow()
                        }
                    )
                    if (workflow.shouldKeyboardShown) {
                        LaunchedEffect(Unit) { onTagRenameKeyboardShown() }
                    }
                }

                is HomeWorkflow.TagDelete -> {
                    LaunchedEffect(workflow) {
                        delay(200) // for animation showing, with HomeTagConfigDialog..
                        sheetState.show()
                    }
                }

                null -> {}
            }

            BackHandler(enabled = sheetState.isVisible) {
                coroutineScope.launch { sheetState.hide() }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState.Success,
    modifier: Modifier = Modifier,
    onTodayCategoryClicked: () -> Unit = {},
    onTimetableCategoryClicked: () -> Unit = {},
    onAllCategoryClicked: () -> Unit = {},
    onTagLongClicked: (Tag) -> Unit = {},
) {
    val homeContentPaddingBottom = 76.dp
    val homeContentScrollState = rememberScrollState()
    Box(modifier = modifier) {
        HomeItems(
            todayScheduleCount = uiState.todayScheduleCount,
            timetableScheduleCount = uiState.timetableScheduleCount,
            allScheduleCount = uiState.allScheduleCount,
            tags = uiState.tags,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(homeContentScrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = homeContentPaddingBottom),
            onTodayCategoryClicked = onTodayCategoryClicked,
            onTimetableCategoryClicked = onTimetableCategoryClicked,
            onAllCategoryClicked = onAllCategoryClicked,
            onTagLongClicked = onTagLongClicked
        )

        BottomContent(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter),
            homeContentPaddingBottom = homeContentPaddingBottom,
            homeContentScrollState = homeContentScrollState
        )
    }
}

@Composable
private fun HomeItems(
    todayScheduleCount: Long,
    timetableScheduleCount: Long,
    allScheduleCount: Long,
    tags: ImmutableList<Tag>,
    modifier: Modifier = Modifier,
    onTodayCategoryClicked: () -> Unit = {},
    onTimetableCategoryClicked: () -> Unit = {},
    onAllCategoryClicked: () -> Unit = {},
    onTagLongClicked: (Tag) -> Unit = {},
) {
    Column(modifier = modifier) {
        Logo(modifier = Modifier.padding(top = 37.dp))
        HomeTitle(
            textRes = R.string.home_category_header,
            modifier = Modifier.padding(top = 42.5.dp)
        )
        CategoryCardsRow(
            modifier = Modifier.padding(top = 14.dp),
            todayCount = todayScheduleCount,
            timetableCount = timetableScheduleCount,
            allCount = allScheduleCount,
            onTodayCategoryClicked = onTodayCategoryClicked,
            onTimetableCategoryClicked = onTimetableCategoryClicked,
            onAllCategoryClicked = onAllCategoryClicked
        )
        HomeTitle(
            textRes = R.string.home_tag_header,
            modifier = Modifier.padding(top = 59.dp)
        )
        TagTextsBox(
            modifier = Modifier
                .padding(top = 14.dp, bottom = 10.dp)
                .fillMaxWidth(),
            tags = tags,
            onTagClicked = {},
            onTagLongClicked = onTagLongClicked
        )
    }
}

@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .width(126.dp)
            .height(25.dp),
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = null,
        tint = ReminderTheme.colors.font1
    )
}

@Composable
private fun HomeTitle(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(textRes),
        color = ReminderTheme.colors.font1,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun BottomContent(
    homeContentPaddingBottom: Dp,
    homeContentScrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    /**
                isPushOn = isPushOn.not()
                deleteTag = DeleteTagEvent(Tag(tagId = 1, name = "삭제시도중.."))*/
    /**
                isPushOn = isPushOn.not()
                deleteTag = DeleteTagEvent(Tag(tagId = 1, name = "삭제시도중.."))*/
    BottomContainer(
        contentPaddingBottom = homeContentPaddingBottom,
        contentScrollState = homeContentScrollState,
        modifier = modifier
    ) {
        NewPlanButton(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp),
            onClick = {
                // configTag = Tag(tagId = 1, name = "Config시도중..")
            }
        )

        /**
                isPushOn = isPushOn.not()
                deleteTag = DeleteTagEvent(Tag(tagId = 1, name = "삭제시도중.."))*/
        /**
                isPushOn = isPushOn.not()
                deleteTag = DeleteTagEvent(Tag(tagId = 1, name = "삭제시도중.."))*/
        /**
                isPushOn = isPushOn.not()
                deleteTag = DeleteTagEvent(Tag(tagId = 1, name = "삭제시도중.."))*/
        TimePushSwitchButton(
            isPushOn = true,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            onClick = {
                /**
                isPushOn = isPushOn.not()
                deleteTag = DeleteTagEvent(Tag(tagId = 1, name = "삭제시도중.."))*/
            },
        )
    }
}

@Preview(
    name = "LightHomeContentPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkHomeContentPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun HomeContentPreview() {
    ReminderTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                todayScheduleCount = 10,
                timetableScheduleCount = 20,
                allScheduleCount = 30,
                tags = (0L..100)
                    .map { index -> Tag(tagId = index, name = "TagName $index") }
                    .toImmutableList(),
                userMessages = persistentListOf()
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}