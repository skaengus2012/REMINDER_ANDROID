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

import com.nlab.reminder.test.unconfinedCoroutineScope
import com.nlab.statekit.util.createStore
import kotlinx.collections.immutable.persistentListOf
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
        testPageShown(
            action = HomeAction.PageShown,
            initState = genHomeUiStateSuccessWithPageShownTrues(),
            expectedState = { shownClearedState -> shownClearedState }
        )
    }

    @Test
    fun `Today's schedule was shown, when today category clicked`() = runTest {
        testPageShown(
            action = HomeAction.OnTodayCategoryClicked,
            initState = genHomeUiStateSuccess(todayScheduleShown = false),
            expectedState = { shownClearedState -> shownClearedState.copy(todayScheduleShown = true) }
        )
    }

    @Test
    fun `Timetable's schedule was shown, when timetable category clicked`() = runTest {
        testPageShown(
            action = HomeAction.OnTimetableCategoryClicked,
            initState = genHomeUiStateSuccess(timetableScheduleShown = false),
            expectedState = { shownClearedState -> shownClearedState.copy(timetableScheduleShown = true) }
        )
    }

    @Test
    fun `All's schedule was shown, when all category clicked`() = runTest {
        testPageShown(
            action = HomeAction.OnAllCategoryClicked,
            initState = genHomeUiStateSuccess(allScheduleShown = false),
            expectedState = { shownClearedState -> shownClearedState.copy(allScheduleShown = true) }
        )
    }

    @Test
    fun `Fetched, when summary loaded`() = runTest {
        val expectedState = genHomeUiStateSuccess()
        val store = createStore(unconfinedCoroutineScope(), HomeUiState.Loading, HomeReducer())
        store.dispatch(expectedState.toSummaryLoaded()).join()

        assertThat(
            store.state.value,
            equalTo(expectedState.withShownCleared())
        )
    }

    @Test
    fun `State was changed, when summary loaded`() = runTest {
        val expectedState = genHomeUiStateSuccess()
        val store = createStore(
            unconfinedCoroutineScope(),
            expectedState.withSummaryCleared(),
            HomeReducer()
        )
        store.dispatch(expectedState.toSummaryLoaded()).join()

        assertThat(store.state.value, equalTo(expectedState))
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
    allScheduleShown = false
)

private suspend fun TestScope.testPageShown(
    action: HomeAction,
    initState: HomeUiState.Success,
    expectedState: (shownClearedState: HomeUiState.Success) -> HomeUiState.Success
) {
    val store = createStore(unconfinedCoroutineScope(), initState, HomeReducer())
    store.dispatch(action).join()

    assertThat(
        store.state.value,
        equalTo(expectedState(initState.withShownCleared()))
    )
}

private fun HomeUiState.Success.toSummaryLoaded(): HomeAction.SummaryLoaded = HomeAction.SummaryLoaded(
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