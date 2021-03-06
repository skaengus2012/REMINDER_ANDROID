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
import com.nlab.reminder.test.dummyTag
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
        modifyTagName: ModifyTagNameUseCase = mock(),
        deleteTag: DeleteTagUseCase = mock(),
        initState: HomeState = HomeState.Init
    ): HomeViewModel = HomeViewModel(HomeStateMachineFactory(getHomeSummary, modifyTagName, deleteTag, initState))

    @Before
    fun init() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Test
    fun `notify action to stateMachine when viewModel action invoked`() {
        val (viewModel, stateMachine) = createMockingViewModelComponent()
        val action = HomeAction.Fetch
        viewModel.onAction(action)
        verify(stateMachine, times(1)).send(action)
    }

    @Test
    fun `invoke fetch when subscribing home state`() = runTest {
        val (viewModel, stateMachine) = createMockingViewModelComponent()
        CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect() }
        verify(stateMachine, times(1)).send(HomeAction.Fetch)
    }

    @Test
    fun `Notify state when state subscribed`() = runTest {
        val actualHomeStates = mutableListOf<HomeState>()
        val expectedSummary = HomeSummary(todayNotificationCount = 1)

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
        val viewModel: HomeViewModel = createViewModel(initState = HomeState.Loaded(HomeSummary()))

        viewModel.onTodayCategoryClicked()
        viewModel.onTimetableCategoryClicked()
        viewModel.onAllCategoryClicked()
        viewModel.onTagClicked(dummyTag)
        viewModel.onTagLongClicked(dummyTag)
        viewModel.onTagRenameRequestClicked(dummyTag)
        viewModel.onTagDeleteRequestClicked(dummyTag)
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
                    TagEndNavigationMessage(dummyTag),
                    HomeTagConfigNavigationMessage(dummyTag),
                    HomeTagRenameNavigationMessage(dummyTag),
                    HomeTagDeleteNavigationMessage(dummyTag)
                )
            )
        )
    }

    @Test
    fun testExtraExtensions() {
        val renameText = "fix"
        val (viewModel, stateMachine) = createMockingViewModelComponent()

        viewModel.onTagRenameConfirmClicked(dummyTag, renameText)
        viewModel.onTagDeleteConfirmClicked(dummyTag)

        verify(stateMachine, times(1))
            .send(HomeAction.OnTagRenameConfirmClicked(dummyTag, renameText))
        verify(stateMachine, times(1))
            .send(HomeAction.OnTagDeleteConfirmClicked(dummyTag))
    }
}