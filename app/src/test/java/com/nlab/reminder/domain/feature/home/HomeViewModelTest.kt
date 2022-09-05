/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

import com.nlab.reminder.domain.common.effect.message.navigation.AllEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TagEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TimetableEndNavigationMessage
import com.nlab.reminder.domain.common.effect.message.navigation.TodayEndNavigationMessage
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genLong
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    /**
    private fun createMockingViewModelComponent(
        state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Init)
    ): Triple<HomeViewModel, HomeStateMachine, HomeStateMachineFactory> {
        val stateMachine: HomeStateMachine = mock { whenever(mock.state) doReturn state }
        val stateMachineFactory: HomeStateMachineFactory = mock {
            whenever(
                mock.create(
                    scope = any(),
                    navigationEffect = any()
                )
            ) doReturn stateMachine
        }

        val viewModel = HomeViewModel(stateMachineFactory)
        return Triple(viewModel, stateMachine, stateMachineFactory)
    }

    private fun createViewModel(
        getHomeSummary: GetHomeSummaryUseCase = mock(),
        getTagUsageCount: GetTagUsageCountUseCase = mock(),
        modifyTagName: ModifyTagNameUseCase = mock(),
        deleteTag: DeleteTagUseCase = mock(),
        initState: HomeState = HomeState.Init
    ): HomeViewModel = HomeViewModel(
        HomeStateMachineFactory(getHomeSummary, getTagUsageCount, modifyTagName, deleteTag, initState)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `notify event to stateMachine when viewModel event invoked`() {
        val (viewModel, stateMachine) = createMockingViewModelComponent()
        val event = HomeEvent.Fetch
        viewModel.invoke(event)
        verify(stateMachine, times(1)).send(event)
    }

    @Test
    fun `invoke fetch when subscribing home state`() = runTest {
        val (viewModel, stateMachine) = createMockingViewModelComponent()
        CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect() }
        verify(stateMachine, times(1)).send(HomeEvent.Fetch)
    }

    @Test
    fun `Notify state when state subscribed`() = runTest {
        val actualHomeStates = mutableListOf<HomeState>()
        val expectedSummary: HomeSummary = genHomeSummary()

        val viewModel: HomeViewModel = createViewModel(
            getHomeSummary = mock {
                whenever(mock()) doReturn flowOf(expectedSummary)
            }
        )
        CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect(actualHomeStates::add) }
        assertThat(
            actualHomeStates,
            equalTo(buildList {
                add(HomeState.Init)
                add(HomeState.Loading)
                add(HomeState.Loaded(expectedSummary))
            })
        )
    }

    @Test
    fun `notify navigation message when navigation event invoked`() = runTest {
        val testUsageCount: Long = genLong()
        val testTag: Tag = genTag()
        val viewModel: HomeViewModel = createViewModel(
            initState = HomeState.Loaded(genHomeSummary()),
            getTagUsageCount = mock {
                whenever(mock(testTag)) doReturn testUsageCount
            }
        )

        viewModel.onTodayCategoryClicked()
        viewModel.onTimetableCategoryClicked()
        viewModel.onAllCategoryClicked()
        viewModel.onTagClicked(testTag)
        viewModel.onTagLongClicked(testTag)
        viewModel.onTagRenameRequestClicked(testTag)
        viewModel.onTagDeleteRequestClicked(testTag)
        assertThat(
            viewModel.navigationEffect
                .event
                .take(7)
                .toList(),
            equalTo(
                listOf(
                    TodayEndNavigationMessage,
                    TimetableEndNavigationMessage,
                    AllEndNavigationMessage,
                    TagEndNavigationMessage(testTag),
                    HomeTagConfigNavigationMessage(testTag),
                    HomeTagRenameNavigationMessage(testTag, testUsageCount),
                    HomeTagDeleteNavigationMessage(testTag, testUsageCount)
                )
            )
        )
    }

    @Test
    fun testExtraExtensions() {
        val renameText = genBothify()
        val testTag: Tag = genTag()
        val (viewModel, stateMachine) = createMockingViewModelComponent()

        viewModel.onTagRenameConfirmClicked(testTag, renameText)
        viewModel.onTagDeleteConfirmClicked(testTag)

        verify(stateMachine, times(1))
            .send(HomeEvent.OnTagRenameConfirmClicked(testTag, renameText))
        verify(stateMachine, times(1))
            .send(HomeEvent.OnTagDeleteConfirmClicked(testTag))
    }*/
}