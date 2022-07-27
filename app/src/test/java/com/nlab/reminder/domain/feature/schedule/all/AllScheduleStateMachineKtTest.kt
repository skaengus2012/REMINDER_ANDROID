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
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
    private val dummyActions: Set<AllScheduleAction> = setOf(
        AllScheduleAction.Fetch,
        AllScheduleAction.AllScheduleReportLoaded(genAllScheduleReport())
    )

    private val dummyStates: Set<AllScheduleState> = setOf(
        AllScheduleState.Init,
        AllScheduleState.Loading,
        AllScheduleState.Loaded(genAllScheduleReport())
    )

    private fun createStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        initState: AllScheduleState = AllScheduleState.Init,
        getAllScheduleReport: GetAllScheduleReportUseCase = mock(),
        updateScheduleComplete: UpdateScheduleCompleteUseCase = mock()
    ): AllScheduleStateMachine =
        AllScheduleStateMachineFactory(getAllScheduleReport, updateScheduleComplete, initState)
            .create(scope)

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState: AllScheduleState = AllScheduleState.Loaded(genAllScheduleReport())
        val stateMachine: AllScheduleStateMachine = createStateMachine(initState = initState)
        assertThat(stateMachine.state.value, sameInstance(initState))
    }

    @Test
    fun `keep state init even when action occurs until fetched`() = runTest {
        val initState: AllScheduleState = AllScheduleState.Init
        val stateMachine: AllScheduleStateMachine = createStateMachine(initState = initState)
        dummyActions
            .asSequence()
            .filter { it !is AllScheduleAction.Fetch }
            .forEach { action ->
                stateMachine.send(action).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }

        stateMachine.send(AllScheduleAction.Fetch).join()
        assertThat(stateMachine.state.value, not(sameInstance(initState)))
    }

    @Test
    fun `fetch is executed when state is init`() = runTest {
        dummyStates
            .asSequence()
            .filter { it !is AllScheduleState.Init }
            .map { state -> state to createStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine.send(AllScheduleAction.Fetch).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }
        val stateMachine: AllScheduleStateMachine = createStateMachine(initState = AllScheduleState.Init)
        stateMachine.send(AllScheduleAction.Fetch).join()
        assertThat(stateMachine.state.value, equalTo(AllScheduleState.Loading))
    }

    @Test
    fun `Notify AllScheduleReport when AllScheduleReportLoaded action received after loading`() = runTest {
        val report: AllScheduleReport = genAllScheduleReport()
        val action: AllScheduleAction = AllScheduleAction.AllScheduleReportLoaded(report)
        dummyStates
            .asSequence()
            .map { state -> state to createStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine
                    .send(action)
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
            whenever(mock(any())) doReturn flow { emit(testReport) }
        }
        val stateMachine: AllScheduleStateMachine = createStateMachine(
            scope = CoroutineScope(Dispatchers.Unconfined),
            getAllScheduleReport = getAllScheduleReportUseCase
        )
        stateMachine
            .send(AllScheduleAction.Fetch)
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
        val updateScheduleCompleteUseCase: UpdateScheduleCompleteUseCase = mock()
        val stateMachine: AllScheduleStateMachine = createStateMachine(
            initState = AllScheduleState.Loaded(genAllScheduleReport()),
            updateScheduleComplete = updateScheduleCompleteUseCase
        )
        stateMachine
            .send(AllScheduleAction.OnScheduleCompleteUpdateClicked(schedule, isComplete))
            .join()
        verify(updateScheduleCompleteUseCase, times(1))(schedule, isComplete)
    }
}