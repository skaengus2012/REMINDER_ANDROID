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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.R
import com.nlab.reminder.core.state.UserMessage
import com.nlab.reminder.domain.common.data.model.genTag
import com.nlab.reminder.domain.common.data.model.genTagUsageCount
import com.nlab.reminder.test.unconfinedCoroutineScope
import com.nlab.statekit.util.createStore
import com.nlab.testkit.genBothify
import com.nlab.testkit.genInt
import kotlinx.collections.immutable.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class HomeReducerTest {
    @Test
    fun testCompleteWorkflow() = runTest {
        val initState = genHomeUiStateSuccess(workflow = genHomeWorkflow())
        testReduce(
            action = HomeAction.CompleteWorkflow,
            initState = initState,
            expectedState = initState.copy(workflow = null)
        )
    }

    @Test
    fun testUserMessageShown() = runTest {
        val shownMessage = UserMessage(genInt())
        val expectedState = genHomeUiStateSuccess(
            userMessages = emptyList()
        )
        testReduce(
            action = HomeAction.UserMessageShown(shownMessage),
            initState = expectedState.copy(userMessages = persistentListOf(shownMessage)),
            expectedState = expectedState
        )
    }

    @Test
    fun testErrorOccurred() = runTest {
        val errorMessage = UserMessage(R.string.unknown_error)
        val initState = genHomeUiStateSuccess(userMessages = emptyList())
        testReduce(
            action = HomeAction.ErrorOccurred(Throwable()),
            initState = initState,
            expectedState = initState.copy(userMessages = persistentListOf(errorMessage))
        )
    }

    @Test
    fun `Workflow not changed, when workflow exists`() = runTest {
        val initState = genHomeUiStateSuccess(workflow = genHomeWorkflow())
        testReduce(
            action = HomeAction.OnTodayCategoryClicked,
            initState = initState,
            expectedState = initState
        )
    }

    @Test
    fun `Today's schedule was shown, when today category clicked`() = runTest {
        val initState = genHomeUiStateSuccess(workflow = null)
        testReduce(
            action = HomeAction.OnTodayCategoryClicked,
            initState = initState,
            expectedState = initState.copy(workflow = HomeWorkflow.TodaySchedule)
        )
    }

    @Test
    fun `Timetable's schedule was shown, when timetable category clicked`() = runTest {
        val initState = genHomeUiStateSuccess(workflow = null)
        testReduce(
            action = HomeAction.OnTimetableCategoryClicked,
            initState = initState,
            expectedState = initState.copy(workflow = HomeWorkflow.TimetableSchedule)
        )
    }

    @Test
    fun `All's schedule was shown, when all category clicked`() = runTest {
        val initState = genHomeUiStateSuccess(workflow = null)
        testReduce(
            action = HomeAction.OnAllCategoryClicked,
            initState = initState,
            expectedState = initState.copy(workflow = HomeWorkflow.AllSchedule)
        )
    }

    @Test
    fun `Fetched, when summary loaded`() = runTest {
        val expectedState = genHomeUiStateSuccess()
        testReduce(
            action = expectedState.toSummaryLoaded(),
            initState = HomeUiState.Loading,
            expectedState = expectedState.copy(workflow = null)
        )
    }

    @Test
    fun `State was changed, when summary loaded`() = runTest {
        val expectedState = genHomeUiStateSuccess()
        testReduce(
            action = expectedState.toSummaryLoaded(),
            initState = expectedState.withSummaryCleared(),
            expectedState = expectedState
        )
    }

    @Test
    fun `Tag config workflow set, when tag metadata loaded`() = runTest {
        val tag = genTag()
        val usageCount = genTagUsageCount()
        val initState = genHomeUiStateSuccess(tags = listOf(tag), workflow = null)
        testReduce(
            action = HomeAction.TagConfigMetadataLoaded(tag, usageCount),
            initState = initState,
            expectedState = initState.copy(workflow = HomeWorkflow.TagConfig(tag, usageCount))
        )
    }

    @Test
    fun `Show not exists tag message, when no tag used for TagConfig`() = runTest {
        val tag = genTag()
        val initState = genHomeUiStateSuccess(
            tags = emptyList(),
            userMessages = emptyList(),
            workflow = null
        )
        testReduce(
            action = HomeAction.TagConfigMetadataLoaded(tag, genTagUsageCount()),
            initState = initState,
            expectedState = initState.copy(
                userMessages = persistentListOf(UserMessage(R.string.tag_not_exist))
            )
        )
    }

    @Test
    fun `Tag rename workflow set, when tag rename request clicked`() = runTest {
        val tag = genTag()
        val usageCount = genTagUsageCount()
        val initState = genHomeUiStateSuccess(
            tags = emptyList(),
            workflow = HomeWorkflow.TagConfig(tag, usageCount)
        )
        testReduce(
            action = HomeAction.OnTagRenameRequestClicked,
            initState = initState,
            expectedState = initState.copy(
                workflow = HomeWorkflow.TagRename(
                    tag = tag,
                    usageCount = usageCount,
                    renameText = tag.name,
                    shouldKeyboardShown = true
                )
            )
        )
    }

    @Test
    fun `Nothing happened, when tag rename request clicked, but tagConfig workflow not set`() = runTest {
        val initState = genHomeUiStateSuccess(
            workflow = genHomeWorkflow(ignoreCases = setOf(HomeWorkflow.TagConfig::class))
        )
        testReduce(
            action = HomeAction.OnTagRenameRequestClicked,
            initState = initState,
            expectedState = initState
        )
    }

    @Test
    fun `Keyboard shown by Tag rename input box`() = runTest {
        val tagRename = genHomeTagRenameWorkflow(shouldKeyboardShown = true)
        val initState = genHomeUiStateSuccess(workflow = tagRename)
        testReduce(
            action = HomeAction.OnTagRenameInputKeyboardShown,
            initState = initState,
            expectedState = initState.copy(
                workflow = tagRename.copy(shouldKeyboardShown = false)
            )
        )
    }

    @Test
    fun `Nothing happened, when keyboard shown, but tagRename workflow not set`() = runTest {
        val initState = genHomeUiStateSuccess(
            workflow = genHomeWorkflow(ignoreCases = setOf(HomeWorkflow.TagRename::class))
        )
        testReduce(
            action = HomeAction.OnTagRenameInputKeyboardShown,
            initState = initState,
            expectedState = initState
        )
    }

    @Test
    fun `Tag rename inputted`() = runTest {
        val input = genBothify("rename-????")
        val tagRename = genHomeTagRenameWorkflow(renameText = "")
        val initState = genHomeUiStateSuccess(workflow = tagRename)
        testReduce(
            action = HomeAction.OnTagRenameInputted(input),
            initState = initState,
            expectedState = initState.copy(workflow = tagRename.copy(renameText = input))
        )
    }

    @Test
    fun `Nothing happened, when tag rename text inputted, but tagRename workflow not set`() = runTest {
        val initState = genHomeUiStateSuccess(
            workflow = genHomeWorkflow(ignoreCases = setOf(HomeWorkflow.TagRename::class))
        )
        testReduce(
            action = HomeAction.OnTagRenameInputKeyboardShown,
            initState = initState,
            expectedState = initState
        )
    }

    @Test
    fun `Tag delete workflow set, when Tag delete request was Clicked`() = runTest {
        val tag = genTag()
        val usageCount = genTagUsageCount()
        val initState = genHomeUiStateSuccess(
            tags = emptyList(),
            workflow = HomeWorkflow.TagConfig(tag, usageCount)
        )
        testReduce(
            action = HomeAction.OnTagDeleteRequestClicked,
            initState = initState,
            expectedState = initState.copy(
                workflow = HomeWorkflow.TagDelete(
                    tag = tag,
                    usageCount = usageCount
                )
            )
        )
    }

    @Test
    fun `Nothing happened, when tag delete request clicked, but tagConfig workflow not set`() = runTest {
        val initState = genHomeUiStateSuccess(
            workflow = genHomeWorkflow(ignoreCases = setOf(HomeWorkflow.TagConfig::class))
        )
        testReduce(
            action = HomeAction.OnTagDeleteRequestClicked,
            initState = initState,
            expectedState = initState
        )
    }
}

private suspend fun TestScope.testReduce(
    action: HomeAction,
    initState: HomeUiState,
    expectedState: HomeUiState
) {
    val store = createStore(unconfinedCoroutineScope(), initState, HomeReducer())
    store.dispatch(action).join()

    assertThat(store.state.value, equalTo(expectedState))
}

private fun HomeUiState.Success.toSummaryLoaded() = HomeAction.SummaryLoaded(
    todaySchedulesCount = todayScheduleCount,
    timetableSchedulesCount = timetableScheduleCount,
    allSchedulesCount = allScheduleCount,
    tags = tags
)

private fun HomeUiState.Success.withSummaryCleared(): HomeUiState.Success = copy(
    todayScheduleCount = 0,
    timetableScheduleCount = 0,
    allScheduleCount = 0,
    tags = persistentListOf()
)