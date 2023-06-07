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
import com.nlab.reminder.domain.common.data.model.Tag
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
    fun `Page was cleared, when page shown`() = runTest {
        val initState = genHomeUiStateSuccessWithPageShownTrues()
        testReduce(
            action = HomeAction.PageShown,
            initState = initState,
            expectedState = initState.withShownCleared()
        )
    }

    @Test
    fun testUserMessageShown() = runTest {
        val shownMessage = UserMessage(genInt())
        val expectedState = genHomeUiStateSuccess(userMessages = emptyList())
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
    fun `Today's schedule was shown, when today category clicked`() = runTest {
        val initState = genHomeUiStateSuccess(todayScheduleShown = false)
        testReduce(
            action = HomeAction.OnTodayCategoryClicked,
            initState = initState,
            expectedState = initState.withShownCleared().copy(todayScheduleShown = true)
        )
    }

    @Test
    fun `Timetable's schedule was shown, when timetable category clicked`() = runTest {
        val initState = genHomeUiStateSuccess(timetableScheduleShown = false)
        testReduce(
            action = HomeAction.OnTimetableCategoryClicked,
            initState = initState,
            expectedState = initState.withShownCleared().copy(timetableScheduleShown = true)
        )
    }

    @Test
    fun `All's schedule was shown, when all category clicked`() = runTest {
        val initState = genHomeUiStateSuccess(allScheduleShown = false)
        testReduce(
            action = HomeAction.OnAllCategoryClicked,
            initState = initState,
            expectedState = initState.withShownCleared().copy(allScheduleShown = true)
        )
    }

    @Test
    fun `Fetched, when summary loaded`() = runTest {
        val expectedState = genHomeUiStateSuccess()
        testReduce(
            action = expectedState.toSummaryLoaded(),
            initState = HomeUiState.Loading,
            expectedState = expectedState.withShownCleared()
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
    fun `Tag config target set, when tag metadata loaded`() = runTest {
        val tag = genTag()
        val usageCount = genTagUsageCount()
        val initState = genHomeUiStateSuccess(tags = listOf(tag))
        testReduce(
            action = HomeAction.TagConfigMetadataLoaded(tag, usageCount),
            initState = initState,
            expectedState = initState
                .withShownCleared()
                .copy(tagConfigTarget = TagConfig(tag, usageCount))
        )
    }

    @Test
    fun `Show user message, when no tag used for LongClick`() = runTest {
        testUserMessageShownByTagNotExist { tag ->
            HomeAction.TagConfigMetadataLoaded(tag, genTagUsageCount())
        }
    }

    @Test
    fun `Tag rename target set, when metadata for rename tag loaded`() = runTest {
        val tag = genTag()
        val usageCount = genTagUsageCount()
        val initState = genHomeUiStateSuccess(
            tags = listOf(tag),
            tagConfigTarget = TagConfig(tag, usageCount)
        )
        testReduce(
            action = HomeAction.TagRenameMetadataLoaded(tag, usageCount),
            initState = initState,
            expectedState = initState.withShownCleared().copy(
                tagRenameTarget = TagRenameConfig(tag, usageCount, renameText = "")
            )
        )
    }

    @Test
    fun `Show user message, when no tag used for loaded rename metadata`() = runTest {
        testUserMessageShownByTagNotExist { tag ->
            HomeAction.TagRenameMetadataLoaded(tag, genTagUsageCount())
        }
    }

    @Test
    fun `Tag rename inputted`() = runTest {
        val tag = genTag()
        val input = genBothify("rename-????")
        val curTagRenameConfig = TagRenameConfig(tag, genTagUsageCount(), renameText = "")
        val initState = genHomeUiStateSuccess(tagRenameTarget = curTagRenameConfig)
        testReduce(
            action = HomeAction.OnTagRenameInputted(input),
            initState = initState,
            expectedState = initState.copy(
                tagRenameTarget = curTagRenameConfig.copy(renameText = input)
            )
        )
    }

    @Test
    fun `Tag rename input ignored, when tag rename config not existed`() = runTest {
        val expectedState = genHomeUiStateSuccess(tagRenameTarget = null)
        testReduce(
            action = HomeAction.OnTagRenameInputted(genBothify("rename-????")),
            initState = expectedState,
            expectedState = expectedState
        )
    }

    @Test
    fun `Tag delete target set, when metadata for delete tag loaded`() = runTest {
        val tag = genTag()
        val usageCount = genTagUsageCount()
        val initState = genHomeUiStateSuccess(
            tags = listOf(tag),
            tagConfigTarget = TagConfig(tag, usageCount)
        )
        testReduce(
            action = HomeAction.TagDeleteMetadataLoaded(tag, usageCount),
            initState = initState,
            expectedState = initState.withShownCleared().copy(
                tagDeleteTarget = TagDeleteConfig(tag, usageCount)
            )
        )
    }

    @Test
    fun `Show user message, when no tag used for loaded delete metadata`() = runTest {
        testUserMessageShownByTagNotExist { tag ->
            HomeAction.TagDeleteMetadataLoaded(tag, genTagUsageCount())
        }
    }
}

private fun genHomeUiStateSuccessWithPageShownTrues(): HomeUiState.Success = genHomeUiStateSuccess(
    todayScheduleShown = true,
    timetableScheduleShown = true,
    allScheduleShown = true
)

private fun HomeUiState.Success.withShownCleared(): HomeUiState.Success = copy(
    todayScheduleShown = false,
    timetableScheduleShown = false,
    allScheduleShown = false,
    tagConfigTarget = null,
    tagRenameTarget = null,
    tagDeleteTarget = null
)

private suspend fun TestScope.testReduce(
    action: HomeAction,
    initState: HomeUiState,
    expectedState: HomeUiState
) {
    val store = createStore(unconfinedCoroutineScope(), initState, HomeReducer())
    store.dispatch(action).join()

    assertThat(store.state.value, equalTo(expectedState))
}

private suspend fun TestScope.testUserMessageShownByTagNotExist(
    getAction: suspend (Tag) -> HomeAction
) {
    val tag = genTag()
    val initState = genHomeUiStateSuccess(
        tags = emptyList(),
        userMessages = emptyList(),
        tagConfigTarget = TagConfig(tag, genTagUsageCount()),
        tagRenameTarget = TagRenameConfig(tag, genTagUsageCount(), genBothify())
    )
    testReduce(
        action = getAction(tag),
        initState = initState,
        expectedState = initState.copy(
            userMessages = persistentListOf(UserMessage(R.string.tag_not_exist)),
            tagConfigTarget = null,
            tagRenameTarget = null,
        )
    )
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