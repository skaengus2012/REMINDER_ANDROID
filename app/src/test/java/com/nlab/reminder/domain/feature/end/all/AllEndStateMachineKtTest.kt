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
import androidx.paging.PagingData
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.genSchedules
import com.nlab.reminder.test.genBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllEndStateMachineKtTest {
    private val dummyActions: Set<AllEndAction> = setOf(
        AllEndAction.Fetch,
        AllEndAction.DoingScheduleLoaded(doingSchedules = genSchedules(isComplete = false)),
        AllEndAction.DoneScheduleLoaded(doneSchedule = PagingData.from(genSchedules(isComplete = true))),
        AllEndAction.DoneScheduleShownChanged(isShow = genBoolean())
    )

    private val dummyStates: Set<AllEndState> = setOf(
        AllEndState.Init,
        AllEndState.Loading,
        AllEndStateGenerator.genLoaded()
    )

    private fun createStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        initState: AllEndState = AllEndState.Init
    ): AllEndStateMachine = AllEndStateMachineFactory(initState).create(scope)

    @Test
    fun `holds injected state when machine created`() = runTest {
        val initState: AllEndState = AllEndStateGenerator.genLoaded()
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
    fun `Notify doing schedules Loaded when doing schedule loaded action received after loading`() = runTest {
        val target: List<Schedule> = genSchedules(isComplete = false)
        dummyStates
            .asSequence()
            .filter { it !is AllEndState.Init }
            .map { state -> state to createStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine
                    .send(AllEndAction.DoingScheduleLoaded(target))
                    .join()
                assertThat(
                    stateMachine.state.value,
                    equalTo(
                        if (initState is AllEndState.Loaded) initState.copy(doingSchedules = target)
                        else AllEndState.Loaded(
                            doingSchedules = target,
                            doneSchedules = PagingData.empty(),
                            isDoneScheduleShown = false
                        )
                    )
                )
            }
    }

    @Test
    fun `Notify done schedules Loaded when done schedule loaded action received after loading`() = runTest {
        val target: PagingData<Schedule> = PagingData.from(genSchedules(isComplete = true))
        dummyStates
            .asSequence()
            .filter { it !is AllEndState.Init }
            .map { state -> state to createStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine
                    .send(AllEndAction.DoneScheduleLoaded(target))
                    .join()
                assertThat(
                    stateMachine.state.value,
                    equalTo(
                        if (initState is AllEndState.Loaded) initState.copy(doneSchedules = target)
                        else AllEndState.Loaded(
                            doingSchedules = emptyList(),
                            doneSchedules = target,
                            isDoneScheduleShown = false
                        )
                    )
                )
            }
    }

    @Test
    fun `Notify done schedules shown changed when done schedule change action received after loading`() = runTest {
        val isDoneScheduleShown: Boolean = genBoolean()
        dummyStates
            .asSequence()
            .filter { it !is AllEndState.Init }
            .map { state -> state to createStateMachine(initState = state) }
            .forEach { (initState, stateMachine) ->
                stateMachine
                    .send(AllEndAction.DoneScheduleShownChanged(isDoneScheduleShown))
                    .join()
                assertThat(
                    stateMachine.state.value,
                    equalTo(
                        if (initState is AllEndState.Loaded) initState.copy(isDoneScheduleShown = isDoneScheduleShown)
                        else AllEndState.Loaded(
                            doingSchedules = emptyList(),
                            doneSchedules = PagingData.empty(),
                            isDoneScheduleShown = isDoneScheduleShown
                        )
                    )
                )
            }
    }
}