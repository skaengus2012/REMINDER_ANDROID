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

import com.nlab.reminder.core.state.StateController
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.once
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
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
    private val sampleEvent: AllScheduleEvent =
        AllScheduleEvent.OnScheduleCompleteUpdateClicked(genSchedule().id(), isComplete = genBoolean())
    private val sampleState: AllScheduleState = AllScheduleState.Loaded(genAllScheduleReport())

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stateController send event when viewModel sent`() {
        val stateController: StateController<AllScheduleEvent, AllScheduleState> = mock()
        val viewModel = AllScheduleViewModel(
            stateControllerFactory = mock {
                whenever(mock.create(any())) doReturn stateController
            }
        )

        viewModel.invoke(sampleEvent)
        verify(stateController, once()).send(sampleEvent)
    }

    @Test
    fun `notify state when stateController flow published`() {
        val stateController: StateController<AllScheduleEvent, AllScheduleState> = mock {
            whenever(mock.state) doReturn MutableStateFlow(sampleState)
        }
        val viewModel = AllScheduleViewModel(
            stateControllerFactory = mock { whenever(mock.create(any())) doReturn stateController }
        )

        assertThat(viewModel.state.value, equalTo(sampleState))
    }
}