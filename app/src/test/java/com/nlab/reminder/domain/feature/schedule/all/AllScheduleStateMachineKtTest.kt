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

import com.nlab.reminder.core.state.asContainer
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.UpdateCompleteUseCase
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genFlowObserveDispatcher
import com.nlab.reminder.test.once
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllScheduleStateMachineKtTest {
    @Test
    fun `update to loading when state was init and fetch sent`() = runTest {
        val stateContainer =
            genAllScheduleStateMachine()
                .asContainer(CoroutineScope(Dispatchers.Default), AllScheduleState.Init)
        stateContainer
            .send(AllScheduleEvent.Fetch)
            .join()
        assertThat(stateContainer.stateFlow.value, equalTo(AllScheduleState.Loading))
    }

    @Test
    fun `update to Loaded when state was not init and AllScheduleReportLoaded sent`() = runTest {
        val allScheduleReport = genAllScheduleReport()
        val stateContainers =
            genAllScheduleStates()
                .filter { it != AllScheduleState.Init }
                .map { genAllScheduleStateMachine().asContainer(CoroutineScope(Dispatchers.Default), it) }
        stateContainers
            .map { it.send(AllScheduleEvent.AllScheduleReportLoaded(allScheduleReport)) }
            .joinAll()
        assertThat(
            stateContainers.map { it.stateFlow.value }.all { it == AllScheduleState.Loaded(allScheduleReport) },
            equalTo(true)
        )
    }

    @Test
    fun `subscribe allScheduleReport snapshot when state was init and fetch sent`() = runTest {
        val expected = genAllScheduleReport()
        val getAllScheduleReport: GetAllScheduleReportUseCase = mock {
            whenever(mock()) doReturn flow { emit(expected) }
        }
        val stateContainer =
            genAllScheduleStateMachine(getAllScheduleReport = getAllScheduleReport)
                .asContainer(CoroutineScope(Dispatchers.Unconfined), AllScheduleState.Init)
        stateContainer
            .send(AllScheduleEvent.Fetch)
            .join()

        val deferred = CompletableDeferred<AllScheduleReport>()
        stateContainer
            .stateFlow
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
        val stateContainer =
            genAllScheduleStateMachine(updateScheduleComplete = updateCompleteUseCase)
                .asContainer(CoroutineScope(Dispatchers.Unconfined), AllScheduleState.Loaded(genAllScheduleReport()))
        stateContainer
            .send(AllScheduleEvent.OnScheduleCompleteUpdateClicked(schedule.id(), isComplete))
            .join()
        verify(updateCompleteUseCase, once())(schedule.id(), isComplete)
    }
}