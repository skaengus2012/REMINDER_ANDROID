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

import com.nlab.reminder.core.state.util.controlIn
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.UpdateCompleteUseCase
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genFlowObserveDispatcher
import com.nlab.reminder.test.once
import kotlinx.coroutines.*
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
    private fun genEvents(): Set<AllScheduleEvent> = setOf(
        AllScheduleEvent.Fetch,
        AllScheduleEvent.AllScheduleReportLoaded(genAllScheduleReport()),
        AllScheduleEvent.OnScheduleCompleteUpdateClicked(genSchedule().id(), genBoolean())
    )

    private fun genStates(): Set<AllScheduleState> = setOf(
        AllScheduleState.Init,
        AllScheduleState.Loading,
        AllScheduleState.Loaded(genAllScheduleReport())
    )

    private fun genStateMachine(
        getAllScheduleReport: GetAllScheduleReportUseCase = mock { onBlocking { mock() } doReturn emptyFlow() },
        updateScheduleComplete: UpdateCompleteUseCase = mock()
    ): AllScheduleStateMachine = AllScheduleStateMachine(
        getAllScheduleReport,
        updateScheduleComplete
    )

    @Test
    fun `update to loading when state was init and fetch sent`() = runTest {
        val stateController = genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), AllScheduleState.Init)
        stateController
            .send(AllScheduleEvent.Fetch)
            .join()
        assertThat(stateController.state.value, equalTo(AllScheduleState.Loading))
    }

    @Test
    fun `never updated when state was not init and fetch sent`() = runTest {
        val initAndStateControllers =
            genStates()
                .filter { it != AllScheduleState.Init }
                .map { state -> state to genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), state) }
        initAndStateControllers
            .map { it.second }
            .map { it.send(AllScheduleEvent.Fetch) }
            .joinAll()

        assertThat(
            initAndStateControllers.all { (initState, controller) -> initState == controller.state.value },
            equalTo(true)
        )
    }

    @Test
    fun `update to Loaded when state was not init and AllScheduleReportLoaded sent`() = runTest {
        val allScheduleReport = genAllScheduleReport()
        val stateControllers =
            genStates()
                .filter { it != AllScheduleState.Init }
                .map { genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), it) }
        stateControllers
            .map { it.send(AllScheduleEvent.AllScheduleReportLoaded(allScheduleReport)) }
            .joinAll()
        assertThat(
            stateControllers.map { it.state.value }.all { it == AllScheduleState.Loaded(allScheduleReport) },
            equalTo(true)
        )
    }

    @Test
    fun `never updated when state was init and AllScheduleReportLoaded sent`() = runTest {
        val stateController = genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), AllScheduleState.Init)
        stateController
            .send(AllScheduleEvent.AllScheduleReportLoaded(genAllScheduleReport()))
            .join()
        assertThat(stateController.state.value, equalTo(AllScheduleState.Init))
    }

    @Test
    fun `never updated when any event excluded fetch and AllScheduleReportLoaded sent`() = runTest {
        val initAndStateControllers = genStates().map { state ->
            state to genStateMachine().controlIn(CoroutineScope(Dispatchers.Default), state)
        }
        assertThat(
            genEvents()
                .asSequence()
                .filterNot { it is AllScheduleEvent.Fetch }
                .filterNot { it is AllScheduleEvent.AllScheduleReportLoaded }
                .map { event ->
                    initAndStateControllers.map { (initState, controller) ->
                        async {
                            controller
                                .send(event)
                                .join()
                            controller.state.value == initState
                        }
                    }
                }
                .flatten()
                .toList()
                .all { it.await() },
            equalTo(true)
        )
    }

    @Test
    fun `subscribe allScheduleReport snapshot when state was init and fetch sent`() = runTest {
        val expected = genAllScheduleReport()
        val getAllScheduleReport: GetAllScheduleReportUseCase =  mock {
            onBlocking { mock() } doReturn flow { emit(expected) }
        }
        val stateController =
            genStateMachine(getAllScheduleReport = getAllScheduleReport)
                .controlIn(CoroutineScope(Dispatchers.Unconfined), AllScheduleState.Init)
        stateController
            .send(AllScheduleEvent.Fetch)
            .join()

        val deferred = CompletableDeferred<AllScheduleReport>()
        stateController
            .state
            .filterIsInstance<AllScheduleState.Loaded>()
            .onEach { deferred.complete(it.allSchedulesReport) }
            .launchIn(genFlowObserveDispatcher())

        assertThat(deferred.await(), equalTo(expected))
    }

    @Test
    fun `update schedule complete when stateMachine received update schedule complete`() = runTest {
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val updateCompleteUseCase: UpdateCompleteUseCase = mock()
        val stateController =
            genStateMachine(updateScheduleComplete = updateCompleteUseCase)
                .controlIn(CoroutineScope(Dispatchers.Unconfined), AllScheduleState.Loaded(genAllScheduleReport()))
        stateController
            .send(AllScheduleEvent.OnScheduleCompleteUpdateClicked(schedule.id(), isComplete))
            .join()
        verify(updateCompleteUseCase, once())(schedule.id(), isComplete)
    }
}