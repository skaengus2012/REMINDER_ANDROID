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

package com.nlab.reminder.domain.feature.schedule.all

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllScheduleViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `notify action to stateMachine when viewModel action invoked`() {
        val (viewModel, stateMachine) = genAllScheduleMockingViewModelComponent()
        val action = AllScheduleAction.Fetch
        viewModel.onAction(action)
        verify(stateMachine, times(1)).send(action)
    }

    @Test
    fun `invoke fetch when subscribing home state`() = runTest {
        val (viewModel, stateMachine) = genAllScheduleMockingViewModelComponent()
        CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect() }
        verify(stateMachine, times(1)).send(AllScheduleAction.Fetch)
    }

    @Test
    fun `Notify state when state subscribed`() = runTest {
        val actualStates = mutableListOf<AllScheduleState>()
        val expectedReport: AllScheduleReport = genAllScheduleReport()

        val viewModel: AllScheduleViewModel = genAllScheduleViewModel(
            getAllScheduleReport = mock {
                whenever(mock(any())) doReturn flowOf(expectedReport)
            }
        )
        CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect(actualStates::add) }
        assertThat(
            actualStates,
            equalTo(buildList {
                add(AllScheduleState.Init)
                add(AllScheduleState.Loading)
                add(AllScheduleState.Loaded(expectedReport))
            })
        )
    }
}