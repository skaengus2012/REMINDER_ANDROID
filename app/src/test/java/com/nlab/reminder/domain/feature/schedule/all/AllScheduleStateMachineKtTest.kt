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

import com.nlab.reminder.core.effect.SideEffectHandle
import com.nlab.reminder.core.state.asContainer
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    @Test
    fun `update to loading when state was init and fetch sent`() = runTest {
        val stateContainer =
            genAllScheduleStateMachine()
                .asContainer(genStateContainerScope(), AllScheduleState.Init)
        stateContainer.send(AllScheduleEvent.Fetch)
        assertThat(stateContainer.stateFlow.value, equalTo(AllScheduleState.Loading))
    }

    @Test
    fun `update to Loaded when state was not init and AllScheduleReportLoaded sent`() = runTest {
        val allScheduleReport = genAllScheduleSnapshot()
        val stateContainers =
            genAllScheduleStates()
                .filter { it != AllScheduleState.Init }
                .map { genAllScheduleStateMachine().asContainer(genStateContainerScope(), it) }
        stateContainers
            .map { it.send(AllScheduleEvent.OnAllScheduleSnapshotLoaded(allScheduleReport)) }
            .joinAll()
        assertThat(
            stateContainers.map { it.stateFlow.value }.all { it == AllScheduleState.Loaded(allScheduleReport) },
            equalTo(true)
        )
    }

    @Test
    fun `subscribe allScheduleReport snapshot when state was init and fetch sent`() = runTest {
        val expected = genAllScheduleSnapshot()
        val getAllScheduleReport: GetAllScheduleSnapshotUseCase = mock {
            whenever(mock()) doReturn flow { emit(expected) }
        }
        val stateContainer =
            genAllScheduleStateMachine(getAllScheduleSnapshot = getAllScheduleReport)
                .asContainer(genStateContainerScope(), AllScheduleState.Init)
        stateContainer.send(AllScheduleEvent.Fetch)

        val deferred = CompletableDeferred<AllScheduleSnapshot>()
        stateContainer
            .stateFlow
            .filterIsInstance<AllScheduleState.Loaded>()
            .onEach { deferred.complete(it.snapshot) }
            .launchIn(genFlowObserveCoroutineScope())

        assertThat(deferred.await(), equalTo(expected))
    }

    @Test
    fun `modify schedule complete when stateMachine received update schedule complete`() = runTest {
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val modifyScheduleCompleteUseCase: ModifyScheduleCompleteUseCase = mock()
        val stateContainer =
            genAllScheduleStateMachine(modifyScheduleComplete = modifyScheduleCompleteUseCase)
                .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
        stateContainer
            .send(AllScheduleEvent.OnModifyScheduleCompleteClicked(schedule.id, isComplete))
            .join()
        verify(modifyScheduleCompleteUseCase, once())(schedule.id, isComplete)
    }

    @Test
    fun `hide completed when stateMachine received OnToggleCompletedScheduleShownClicked and snapshot was shown`() =
        runTest { testOnToggleCompletedScheduleShownClicked(isCompletedScheduleShown = true) }

    @Test
    fun `show completed when stateMachine received OnToggleCompletedScheduleShownClicked and snapshot was hidden`() =
        runTest { testOnToggleCompletedScheduleShownClicked(isCompletedScheduleShown = false) }

    private suspend fun testOnToggleCompletedScheduleShownClicked(isCompletedScheduleShown: Boolean) {
        val completedScheduleShownRepository: CompletedScheduleShownRepository = mock()
        val stateContainer =
            genAllScheduleStateMachine(completedScheduleShownRepository = completedScheduleShownRepository)
                .asContainer(
                    genStateContainerScope(),
                    AllScheduleState.Loaded(genAllScheduleSnapshot(isCompletedScheduleShown = isCompletedScheduleShown))
                )
        stateContainer
            .send(AllScheduleEvent.OnToggleCompletedScheduleShownClicked)
            .join()
        verify(completedScheduleShownRepository, once()).setShown(isCompletedScheduleShown.not())
    }

    @Test
    fun `show error popup when OnToggleCompletedScheduleShownClicked execution failed`() = runTest {
        val sideEffectHandle: SideEffectHandle<AllScheduleSideEffect> = mock()
        val completedScheduleShownRepository: CompletedScheduleShownRepository = mock {
            whenever(mock.setShown(any())) doReturn Result.Failure(Throwable())
        }

        genAllScheduleStateMachine(
            sideEffectHandle = sideEffectHandle,
            completedScheduleShownRepository = completedScheduleShownRepository
        )
            .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
            .send(AllScheduleEvent.OnToggleCompletedScheduleShownClicked)
            .join()

        verify(sideEffectHandle, once()).post(AllScheduleSideEffect.ShowErrorPopup)
    }

    @Test
    fun `never update when received dragEnd event was emptyList`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()

        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
            .send(AllScheduleEvent.OnDragScheduleEnded(emptyList()))
            .join()
        verify(scheduleRepository, never()).updateVisiblePriorities(any())
    }

    @Test
    fun `never update when received dragEnd event was same by visible priority`() = runTest {
        val startPriority = genLong()
        val firstSchedule: Schedule = genSchedule(visiblePriority = startPriority)
        val secondSchedule: Schedule = genSchedule(visiblePriority = startPriority + 1)
        val scheduleRepository: ScheduleRepository = mock()

        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
            .send(AllScheduleEvent.OnDragScheduleEnded(genScheduleUiStates(listOf(firstSchedule, secondSchedule))))
            .join()
        verify(scheduleRepository, once()).updateVisiblePriorities(emptyList())
    }

    @Test
    fun `update exclude same visible priority when stateMachine sent dragEnd event`() = runTest {
        val startPriority = genLong()
        val schedules: List<Schedule> =
            List(10) { index -> genSchedule(visiblePriority = startPriority + index.toLong()) }
                .toMutableList()
                .apply { add(index = 3, removeAt(5)) }
        val scheduleRepository: ScheduleRepository = mock()
        println(schedules.map { it.visiblePriority })

        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
            .send(AllScheduleEvent.OnDragScheduleEnded(genScheduleUiStates(schedules)))
            .join()
        verify(scheduleRepository, once()).updateVisiblePriorities(
            listOf(
                ModifyVisiblePriorityRequest(scheduleId = schedules[3].id, startPriority + 3),
                ModifyVisiblePriorityRequest(scheduleId = schedules[4].id, startPriority + 4),
                ModifyVisiblePriorityRequest(scheduleId = schedules[5].id, startPriority + 5)
            )
        )
    }

    @Test
    fun `delete specific schedule when stateMachine sent deleteClicked event`() = runTest {
        val schedule: Schedule = genSchedule()
        val scheduleRepository: ScheduleRepository = mock()

        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
            .send(AllScheduleEvent.OnDeleteScheduleClicked(schedule.id))
            .join()
        verify(scheduleRepository, once()).delete(DeleteScheduleRequest.ByScheduleId(schedule.id))
    }

    @Test
    fun `navigate schedule link when stateMachine sent scheduleLinkClicked event`() = runTest {
        val link: String = genBothify()
        val schedule: Schedule = genSchedule(link = link)
        val sideEffectHandle: SideEffectHandle<AllScheduleSideEffect> = mock()
        genAllScheduleStateMachine(sideEffectHandle = sideEffectHandle)
            .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
            .send(AllScheduleEvent.OnScheduleLinkClicked(genScheduleUiState(schedule)))
            .join()
        verify(sideEffectHandle, once()).post(AllScheduleSideEffect.NavigateScheduleLink(link))
    }

    @Test
    fun `nothing work when stateMachine sent scheduleLinkClicked event and schedule link was empty`() = runTest {
        val schedule: Schedule = genSchedule(link = "")
        val sideEffectHandle: SideEffectHandle<AllScheduleSideEffect> = mock()
        genAllScheduleStateMachine(sideEffectHandle = sideEffectHandle)
            .asContainer(genStateContainerScope(), AllScheduleState.Loaded(genAllScheduleSnapshot()))
            .send(AllScheduleEvent.OnScheduleLinkClicked(genScheduleUiState(schedule)))
            .join()
        verify(sideEffectHandle, never()).post(any())
    }
}