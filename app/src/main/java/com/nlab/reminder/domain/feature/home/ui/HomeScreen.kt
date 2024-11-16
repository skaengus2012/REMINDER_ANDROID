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

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * @author Doohyun
 */
@Composable
internal fun HomeRoute(
    navigateToAllScheduleEnd: () -> Unit,
    modifier: Modifier = Modifier,

) {
    /**
    HomeScreen(
        uiState = viewModel.uiState.collectAsStateWithLifecycle(
            lifecycleOwner = LocalLifecycleOwner.current // error : CompositionLocal LocalLifecycleOwner not present
        ),
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
        userMessageShown = viewModel::userMessageShown,
        navigateToAllScheduleEnd = navigateToAllScheduleEnd,
        modifier = modifier
    )*/
}
/**
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
    userMessageShown: (UserMessage) -> Unit,
    navigateToAllScheduleEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isLoadedDelayNeeded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when (val curUi = uiState.value) {
            HomeUiState.Loading -> {
                val visibleState = rememberDelayedVisibleState()
                LaunchedEffect(curUi) {
                    snapshotFlow { visibleState.isVisible }
                        .distinctUntilChanged()
                        .collect { isLoadedDelayNeeded = it }
                }
                DelayedVisibleContent(
                    delayTimeMillis = 500,
                    visibleState = visibleState,
                    key = curUi
                ) {
                    ReminderLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            is HomeUiState.Success -> {
                DelayedVisibleContent(
                    delayTimeMillis = if (isLoadedDelayNeeded) 100 else 0,
                    key = curUi
                ) {
                    HomeContent(
                        todayScheduleCount = curUi.todayScheduleCount,
                        timetableScheduleCount = curUi.timetableScheduleCount,
                        allScheduleCount = curUi.allScheduleCount,
                        tags = curUi.tags,
                        onTodayCategoryClicked = onTodayCategoryClicked,
                        onTimetableCategoryClicked = onTimetableCategoryClicked,
                        onAllCategoryClicked = onAllCategoryClicked,
                        onTagLongClicked = onTagLongClicked
                    )
                }

                UserMessageHandler(
                    messages = curUi.userMessages,
                    onMessageReleased = userMessageShown
                ) { context.showToast(displayMessage) }

                HomeWorkflowHandler(
                    workflow = curUi.interaction,
                    completeWorkflow = completeWorkflow,
                    onTagRenameRequestClicked = onTagRenameRequestClicked,
                    onTagRenameTextChanged = onTagRenameTextChanged,
                    onTagRenameConfirmClicked = onTagRenameConfirmClicked,
                    onTagRenameKeyboardShown = onTagRenameKeyboardShown,
                    onTagDeleteRequestClicked = onTagDeleteRequestClicked,
                    onTagDeleteConfirmClicked = onTagDeleteConfirmClicked,
                    navigateToAllScheduleEnd = navigateToAllScheduleEnd
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    todayScheduleCount: Long,
    timetableScheduleCount: Long,
    allScheduleCount: Long,
    tags: ImmutableList<Tag>,
    modifier: Modifier = Modifier,
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    onTagLongClicked: (Tag) -> Unit,
) {
    val homeContentPaddingBottom = 76.dp
    val homeContentScrollState = rememberScrollState()
    Box(modifier = modifier) {
        HomeItems(
            todayScheduleCount = todayScheduleCount,
            timetableScheduleCount = timetableScheduleCount,
            allScheduleCount = allScheduleCount,
            tags = tags,
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
        Logo(modifier = Modifier.padding(top = 32.dp))
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
            tags = tags, // Tag 가 모듈로 분리되면서, skip 작동하지 않음. strong-skip 존버.
            onTagClicked = {},
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
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = null,
        tint = ReminderTheme.colors.content1
    )
}

@Composable
private fun HomeTitle(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(textRes),
        style = ReminderTheme.typography.titleMedium,
        color = ReminderTheme.colors.content1,
        modifier = modifier
    )
}

@Composable
private fun BottomContent(
    homeContentPaddingBottom: Dp,
    homeContentScrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
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

@Composable
private fun HomeWorkflowHandler(
    workflow: HomeInteraction,
    completeWorkflow: () -> Unit,
    onTagRenameRequestClicked: () -> Unit,
    onTagRenameTextChanged: (String) -> Unit,
    onTagRenameConfirmClicked: () -> Unit,
    onTagRenameKeyboardShown: () -> Unit,
    onTagDeleteRequestClicked: () -> Unit,
    onTagDeleteConfirmClicked: () -> Unit,
    navigateToAllScheduleEnd: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    when (workflow) {
        is HomeInteraction.Empty -> {}
        is HomeInteraction.TagConfig -> {
            HomeTagConfigDialog(
                tagName = workflow.tag.name.value,
                usageCount = DisplayUsageCount(workflow.usageCount),
                onDismiss = completeWorkflow,
                onRenameRequestClicked = onTagRenameRequestClicked,
                onDeleteRequestClicked = onTagDeleteRequestClicked
            )
        }

        is HomeInteraction.TagRename -> {
            TagRenameDialog(
                initText = workflow.renameText,
                tagName = workflow.tag.name.value,
                usageCount = DisplayUsageCount(workflow.usageCount),
                shouldKeyboardShown = workflow.shouldUserInputReady,
                onCancel = completeWorkflow,
                onTextChanged = onTagRenameTextChanged,
                onConfirm = {
                    onTagRenameConfirmClicked()
                    completeWorkflow()
                }
            )
            if (workflow.shouldUserInputReady) {
                SideEffect { onTagRenameKeyboardShown() }
            }
        }

        is HomeInteraction.TagDelete -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ReminderBottomSheet(
                onDismissRequest = completeWorkflow,
                sheetState = sheetState
            ) {
                TagDeleteBottomSheetContent(
                    tagName = workflow.tag.name.value,
                    usageCount = DisplayUsageCount(workflow.usageCount),
                    onCancelClicked = {
                        coroutineScope.launch {
                            sheetState.hide()
                            completeWorkflow()
                        }
                    },
                    onConfirmClicked = {
                        onTagDeleteConfirmClicked()
                        coroutineScope.launch {
                            sheetState.hide()
                            completeWorkflow()
                        }
                    }
                )
            }
        }

        else -> {
            LaunchedEffect(workflow) {
                completeWorkflow()
                if (workflow is HomeInteraction.AllSchedule) navigateToAllScheduleEnd()
            }
        }
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
            todayScheduleCount = 10,
            timetableScheduleCount = 20,
            allScheduleCount = 30,
            tags = (1L..100)
                .map { index -> Tag(id = TagId(rawId = 1), name = "TagName $index".toNonBlankString()) }
                .toImmutableList(),
            onTodayCategoryClicked = {},
            onTimetableCategoryClicked = {},
            onAllCategoryClicked = {},
            onTagLongClicked = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}*/