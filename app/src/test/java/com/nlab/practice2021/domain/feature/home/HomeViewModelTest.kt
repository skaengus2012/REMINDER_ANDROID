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

package com.nlab.practice2021.domain.feature.home

import com.nlab.practice2021.domain.common.effect.android.navigation.TodayEndNavigationMessage
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
    @Before
    fun init() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    private fun createMockingViewModelComponent(
        state: MutableStateFlow<HomeState>
    ): Triple<HomeViewModel, HomeStateMachine, HomeStateMachineFactory> {
        val stateMachine: HomeStateMachine = mock { whenever(mock.state) doReturn state }
        val stateMachineFactory: HomeStateMachineFactory = mock {
            whenever(mock.create(any(), any(), any(), any())) doReturn stateMachine
        }
        val viewModel = HomeViewModel(stateMachineFactory)
        return Triple(viewModel, stateMachine, stateMachineFactory)
    }

    private fun createViewModel(
        getHomeSummary: GetHomeSummaryUseCase = mock(),
        initState: HomeState = HomeState.Init
    ): HomeViewModel = HomeViewModel(HomeStateMachineFactory(getHomeSummary, initState))

    @Test
    fun `notify action to stateMachine when viewModel action invoked`() {
        val stateFlow: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Init)
        val (viewModel, stateMachine) = createMockingViewModelComponent(stateFlow)
        val action = HomeAction.Fetch
        viewModel.onAction(action)
        verify(stateMachine, times(1)).send(action)
    }

    @Test
    fun `invoke fetch when subscribing home state`() = runTest {
        val stateFlow: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Init)
        val (viewModel, stateMachine) = createMockingViewModelComponent(stateFlow)
        CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect() }
        verify(stateMachine, times(1)).send(HomeAction.Fetch)
    }

    @Test
    fun `notify navigation message when onclick today invoked`() = runTest {
        val viewModel: HomeViewModel = createViewModel(
            getHomeSummary = mock {
                whenever(mock()) doReturn flowOf(HomeSummary())
            }
        )

        viewModel.state
            .filterIsInstance<HomeState.Loaded>()
            .first()
            .onTodayClicked()
        assertThat(
            viewModel.navigationEffect
                .event
                .take(1)
                .first(),
            equalTo(TodayEndNavigationMessage)
        )
    }
}