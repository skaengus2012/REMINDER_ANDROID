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

import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.UpdateCompleteUseCase
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllScheduleStateMachineKtTest {
    private val dummyEvents: Set<AllScheduleEvent> = setOf(
        AllScheduleEvent.Fetch,
        AllScheduleEvent.AllScheduleReportLoaded(genAllScheduleReport()),
        AllScheduleEvent.OnScheduleCompleteUpdateClicked(genSchedule().id(), genBoolean())
    )

    private val dummyStates: Set<AllScheduleState> = setOf(
        AllScheduleState.Init,
        AllScheduleState.Loading,
        AllScheduleState.Loaded(genAllScheduleReport())
    )

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState: AllScheduleState = AllScheduleState.Loaded(genAllScheduleReport())
        val stateMachine: AllScheduleStateMachine = genStateMachine(initState = initState)
        assertThat(stateMachine.state.value, sameInstance(initState))
    }

    @Test
    fun `keep state init even when event occurs until fetched`() = runTest {
        val initState: AllScheduleState = AllScheduleState.Init
        val stateMachine: AllScheduleStateMachine = genStateMachine(initState = initState)
        dummyEvents
            .asSequence()
            .filter { it !is AllScheduleEvent.Fetch }
            .forEach { event ->
                stateMachine.send(event).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }

        stateMachine.send(AllScheduleEvent.Fetch).join()
        assertThat(stateMachine.state.value, not(sameInstance(initState)))
    }

    @Test
    fun `fetch is executed when state is init`() = runTest {
        dummyStates
            .asSequence()
            .filter { it !is AllScheduleState.Init }
            .map { state -> state to genStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine.send(AllScheduleEvent.Fetch).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }
        val stateMachine: AllScheduleStateMachine = genStateMachine(initState = AllScheduleState.Init)
        stateMachine.send(AllScheduleEvent.Fetch).join()
        assertThat(stateMachine.state.value, equalTo(AllScheduleState.Loading))
    }

    @Test
    fun `Notify AllScheduleReport when AllScheduleReportLoaded event received after loading`() = runTest {
        val report: AllScheduleReport = genAllScheduleReport()
        val event: AllScheduleEvent = AllScheduleEvent.AllScheduleReportLoaded(report)
        dummyStates
            .asSequence()
            .map { state -> state to genStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine
                    .send(event)
                    .join()
                assertThat(
                    stateMachine.state.value,
                    equalTo(
                        if (initState is AllScheduleState.Init) initState
                        else AllScheduleState.Loaded(report)
                    )
                )
            }
    }

    @Test
    fun `Notify loaded when fetch is called`() = runTest {
        val testReport: AllScheduleReport = genAllScheduleReport()
        val getAllScheduleReportUseCase: GetAllScheduleReportUseCase = mock {
            whenever(mock()) doReturn flow { emit(testReport) }
        }
        val stateMachine: AllScheduleStateMachine = genStateMachine(
            getAllScheduleReport = getAllScheduleReportUseCase
        )
        stateMachine
            .send(AllScheduleEvent.Fetch)
            .join()
        assertThat(
            stateMachine.state.value,
            equalTo(AllScheduleState.Loaded(testReport))
        )
    }

    @Test
    fun `update schedule complete when stateMachine received update schedule complete`() = runTest {
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val updateCompleteUseCase: UpdateCompleteUseCase = mock()
        val stateMachine: AllScheduleStateMachine = genStateMachine(
            initState = AllScheduleState.Loaded(genAllScheduleReport()),
            updateScheduleComplete = updateCompleteUseCase
        )
        stateMachine
            .send(AllScheduleEvent.OnScheduleCompleteUpdateClicked(schedule.id(), isComplete))
            .join()
        verify(updateCompleteUseCase, once())(schedule.id(), isComplete)
    }
}