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

package com.nlab.reminder.domain.feature.end.all

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllEndStateMachineKtTest {
    private val dummyActions: Set<AllEndAction> = setOf(
        AllEndAction.Fetch,
        AllEndAction.AllScheduleReportLoaded(genAllScheduleReport())
    )

    private val dummyStates: Set<AllEndState> = setOf(
        AllEndState.Init,
        AllEndState.Loading,
        AllEndState.Loaded(genAllScheduleReport())
    )

    private fun createStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        initState: AllEndState = AllEndState.Init,
        getAllScheduleReport: GetAllScheduleReportUseCase = mock()
    ): AllEndStateMachine =
        AllEndStateMachineFactory(getAllScheduleReport, initState).create(scope)

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState: AllEndState = AllEndState.Loaded(genAllScheduleReport())
        val stateMachine: AllEndStateMachine = createStateMachine(initState = initState)
        assertThat(stateMachine.state.value, sameInstance(initState))
    }

    @Test
    fun `keep state init even when action occurs until fetched`() = runTest {
        val initState: AllEndState = AllEndState.Init
        val stateMachine: AllEndStateMachine = createStateMachine(initState = initState)
        dummyActions
            .asSequence()
            .filter { it !is AllEndAction.Fetch }
            .forEach { action ->
                stateMachine.send(action).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }

        stateMachine.send(AllEndAction.Fetch).join()
        assertThat(stateMachine.state.value, not(sameInstance(initState)))
    }

    @Test
    fun `fetch is executed when state is init`() = runTest {
        dummyStates
            .asSequence()
            .filter { it !is AllEndState.Init }
            .map { state -> state to createStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine.send(AllEndAction.Fetch).join()
                assertThat(stateMachine.state.value, sameInstance(initState))
            }
        val stateMachine: AllEndStateMachine = createStateMachine(initState = AllEndState.Init)
        stateMachine.send(AllEndAction.Fetch).join()
        assertThat(stateMachine.state.value, equalTo(AllEndState.Loading))
    }

    @Test
    fun `Notify AllScheduleReport when AllScheduleReportLoaded action received after loading`() = runTest {
        val report: AllScheduleReport = genAllScheduleReport()
        val action: AllEndAction = AllEndAction.AllScheduleReportLoaded(report)
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
                        if (initState is AllEndState.Init) initState
                        else AllEndState.Loaded(report)
                    )
                )
            }
    }
}