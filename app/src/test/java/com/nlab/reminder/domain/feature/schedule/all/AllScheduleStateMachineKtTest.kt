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
import com.nlab.reminder.domain.common.schedule.SelectionModeRepository
import com.nlab.reminder.domain.common.schedule.visibleconfig.*
import com.nlab.reminder.test.*
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
    @Test
    fun `update to loading when state was init and fetch sent`() = runTest {
        val stateContainer =
            genAllScheduleStateMachine()
                .asContainer(genStateContainerScope(), AllScheduleState.Init)
        stateContainer.send(AllScheduleEvent.Fetch)
        assertThat(stateContainer.stateFlow.value, equalTo(AllScheduleState.Loading))
    }

    @Test
    fun `update to Loaded when state was not init and StateLoaded sent`() = runTest {
        val isSelectionEnabled: Boolean = genBoolean()
        val scheduleSnapshot: AllScheduleSnapshot =
            genAllScheduleSnapshot(isCompletedScheduleShown = genBoolean(), uiStates = genScheduleUiStates())
        val stateContainers =
            genAllScheduleStates()
                .filter { it != AllScheduleState.Init }
                .map { genAllScheduleStateMachine().asContainer(genStateContainerScope(), it) }
        stateContainers
            .map { it.send(AllScheduleEvent.StateLoaded(scheduleSnapshot, isSelectionEnabled)) }
            .joinAll()
        assertThat(
            stateContainers.map { it.stateFlow.value }.all { state ->
                state == AllScheduleState.Loaded(
                    scheduleUiStates = scheduleSnapshot.scheduleUiStates,
                    isCompletedScheduleShown = scheduleSnapshot.isCompletedScheduleShown,
                    isSelectionMode = isSelectionEnabled
                )
            },
            equalTo(true)
        )
    }

    @Test
    fun `subscribe combined loaded values when state was init and fetch sent`() = runTest {
        val expectedSnapshot: AllScheduleSnapshot = genAllScheduleSnapshot()
        val expectedSelectionMode: Boolean = genBoolean()
        val stateContainer =
            genAllScheduleStateMachine(
                getAllScheduleSnapshot = mock {
                    whenever(mock()) doReturn flow { emit(expectedSnapshot) }
                },
                selectionModeRepository = mock {
                    whenever(mock.enabledStream()) doReturn MutableStateFlow(expectedSelectionMode)
                })
                .asContainer(genStateContainerScope(), AllScheduleState.Init)
        val actualDeferred = async {
            stateContainer
                .stateFlow
                .filterIsInstance<AllScheduleState.Loaded>()
                .take(count = 1)
                .first()
        }

        stateContainer.send(AllScheduleEvent.Fetch)
        assertThat(
            actualDeferred.await(),
            equalTo(
                AllScheduleState.Loaded(
                    expectedSnapshot.scheduleUiStates,
                    isCompletedScheduleShown = expectedSnapshot.isCompletedScheduleShown,
                    isSelectionMode = expectedSelectionMode
                )
            )
        )
    }

    @Test
    fun `modify schedule complete when stateMachine received update schedule complete`() = runTest {
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val modifyScheduleCompleteUseCase: ModifyScheduleCompleteUseCase = mock()
        val stateContainer =
            genAllScheduleStateMachine(modifyScheduleComplete = modifyScheduleCompleteUseCase)
                .asContainer(genStateContainerScope(), genAllScheduleLoadedState())
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
                    genAllScheduleLoadedState(
                        snapshot = genAllScheduleSnapshot(isCompletedScheduleShown = isCompletedScheduleShown)
                    )
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
            .asContainer(genStateContainerScope(), genAllScheduleLoadedState())
            .send(AllScheduleEvent.OnToggleCompletedScheduleShownClicked)
            .join()

        verify(sideEffectHandle, once()).post(AllScheduleSideEffect.ShowErrorPopup)
    }

    @Test
    fun `selectionModeRepository setEnabled when selectionMode was false and received OnToggleSelectionModeEnableClicked`() = runTest {
        testToggleSelectionModeEnableClicked(expectedSelectionModeEnabled = false)
    }

    @Test
    fun `selectionModeRepository setEnabled when selectionMode was true and received OnToggleSelectionModeEnableClicked`() = runTest {
        testToggleSelectionModeEnableClicked(expectedSelectionModeEnabled = true)
    }

    private suspend fun testToggleSelectionModeEnableClicked(expectedSelectionModeEnabled: Boolean) {
        val selectionModeRepository: SelectionModeRepository = mock()

        genAllScheduleStateMachine(selectionModeRepository = selectionModeRepository)
            .asContainer(
                genStateContainerScope(),
                genAllScheduleLoadedState(isSelectionMode = expectedSelectionModeEnabled.not())
            )
            .send(AllScheduleEvent.OnToggleSelectionModeEnableClicked)
            .join()
        verify(selectionModeRepository, once()).setEnabled(expectedSelectionModeEnabled)
    }

    @Test
    fun `never update when received dragEnd event was emptyList`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()

        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), genAllScheduleLoadedState())
            .send(AllScheduleEvent.OnDragScheduleEnded(emptyList()))
            .join()
        verify(scheduleRepository, never()).update(any())
    }

    @Test
    fun `never update when received dragEnd event was same by visible priority`() = runTest {
        val startPriority = genLong()
        val firstSchedule: Schedule = genSchedule(visiblePriority = startPriority)
        val secondSchedule: Schedule = genSchedule(visiblePriority = startPriority + 1)
        val scheduleRepository: ScheduleRepository = mock()

        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), genAllScheduleLoadedState())
            .send(AllScheduleEvent.OnDragScheduleEnded(genScheduleUiStates(listOf(firstSchedule, secondSchedule))))
            .join()
        verify(scheduleRepository, once()).update(UpdateRequest.VisiblePriorities(emptyList()))
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
            .asContainer(genStateContainerScope(), genAllScheduleLoadedState())
            .send(AllScheduleEvent.OnDragScheduleEnded(genScheduleUiStates(schedules)))
            .join()
        verify(scheduleRepository, once()).update(
            UpdateRequest.VisiblePriorities(
                listOf(
                    ModifyVisiblePriorityRequest(scheduleId = schedules[3].id, startPriority + 3),
                    ModifyVisiblePriorityRequest(scheduleId = schedules[4].id, startPriority + 4),
                    ModifyVisiblePriorityRequest(scheduleId = schedules[5].id, startPriority + 5)
                )
            )
        )
    }

    @Test
    fun `delete specific schedule when stateMachine sent deleteClicked event`() = runTest {
        val schedule: Schedule = genSchedule()
        val scheduleRepository: ScheduleRepository = mock()

        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), genAllScheduleLoadedState())
            .send(AllScheduleEvent.OnDeleteScheduleClicked(schedule.id))
            .join()
        verify(scheduleRepository, once()).delete(DeleteRequest.ById(schedule.id))
    }

    @Test
    fun `delete completed schedule when stateMachine sent deletedCompleteScheduleClicked`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()
        genAllScheduleStateMachine(scheduleRepository = scheduleRepository)
            .asContainer(genStateContainerScope(), genAllScheduleLoadedState())
            .send(AllScheduleEvent.OnDeleteCompletedScheduleClicked)
            .join()
        verify(scheduleRepository, once()).delete(DeleteRequest.ByComplete(true))
    }

    @Test
    fun `navigate schedule link when stateMachine sent scheduleLinkClicked event and has uiState`() = runTest {
        val link: String = genBothify()
        val schedule: Schedule = genSchedule(link = link)
        testOnScheduleLinkClickedVerifyTemplate(
            schedule,
            AllScheduleEvent.OnScheduleLinkClicked(schedule.id),
            verify = { sideEffectHandle ->
                verify(sideEffectHandle, once()).post(AllScheduleSideEffect.NavigateScheduleLink(link))
            }
        )
    }

    @Test
    fun `nothing work when stateMachine sent scheduleLinkClicked event and schedule hasn't uiState`() = runTest {
        val link: String = genBothify()
        val schedule: Schedule = genSchedule(scheduleId = 0, link = link)
        testOnScheduleLinkClickedVerifyTemplate(
            genSchedule(scheduleId = 1),
            AllScheduleEvent.OnScheduleLinkClicked(schedule.id),
            verify = { sideEffectHandle ->
                verify(sideEffectHandle, never()).post(AllScheduleSideEffect.NavigateScheduleLink(link))
            }
        )
    }

    @Test
    fun `nothing work when stateMachine sent scheduleLinkClicked event and schedule has blank link`() = runTest {
        val link = " "
        val schedule: Schedule = genSchedule(link = link)
        testOnScheduleLinkClickedVerifyTemplate(
            schedule,
            AllScheduleEvent.OnScheduleLinkClicked(schedule.id),
            verify = { sideEffectHandle ->
                verify(sideEffectHandle, never()).post(AllScheduleSideEffect.NavigateScheduleLink(link))
            }
        )
    }

    private suspend inline fun testOnScheduleLinkClickedVerifyTemplate(
        initState: Schedule,
        event: AllScheduleEvent.OnScheduleLinkClicked,
        verify: (SideEffectHandle<AllScheduleSideEffect>) -> Unit
    ) {
        val sideEffectHandle: SideEffectHandle<AllScheduleSideEffect> = mock()
        genAllScheduleStateMachine(sideEffectHandle = sideEffectHandle)
            .asContainer(
                genStateContainerScope(),
                genAllScheduleLoadedState(
                    snapshot = genAllScheduleSnapshot(uiStates = genScheduleUiStates(listOf(initState)))
                )
            )
            .send(event)
            .join()
        verify(sideEffectHandle)
    }

    @Test
    fun `nothing work when StateMachine hasn't uiState and OnScheduleSelectionClicked event sent`() = runTest {
        val testSchedule: Schedule = genSchedule(scheduleId = 1)
        testOnScheduleSelectionClickedVerifyTemplate(
            initState = genScheduleUiState(genSchedule(scheduleId = 0)),
            event = AllScheduleEvent.OnScheduleSelectionClicked(testSchedule.id),
            verify = { selectionRepository ->
                verify(selectionRepository, never()).setSelected(testSchedule.id, genBoolean())
            }
        )
    }

    @Test
    fun `selectionRepository select true when select was false OnScheduleSelectionClicked event sent`() = runTest {
        val expectedSelect = true
        val uiState: ScheduleUiState = genScheduleUiState(isSelected = expectedSelect.not())
        testOnScheduleSelectionClickedVerifyTemplate(
            initState = uiState,
            event = AllScheduleEvent.OnScheduleSelectionClicked(uiState.id),
            verify = { selectionRepository ->
                verify(selectionRepository, once()).setSelected(uiState.id, expectedSelect)
            }
        )
    }

    @Test
    fun `selectionRepository select false when select was true OnScheduleSelectionClicked event sent`() = runTest {
        val expectedSelect = false
        val uiState: ScheduleUiState = genScheduleUiState(isSelected = expectedSelect.not())
        testOnScheduleSelectionClickedVerifyTemplate(
            initState = uiState,
            event = AllScheduleEvent.OnScheduleSelectionClicked(uiState.id),
            verify = { selectionRepository ->
                verify(selectionRepository, once()).setSelected(uiState.id, expectedSelect)
            }
        )
    }

    private suspend inline fun testOnScheduleSelectionClickedVerifyTemplate(
        initState: ScheduleUiState,
        event: AllScheduleEvent.OnScheduleSelectionClicked,
        verify: (SelectionRepository) -> Unit
    ) {
        val selectionRepository: SelectionRepository = mock()
        genAllScheduleStateMachine(selectionRepository = selectionRepository)
            .asContainer(
                genStateContainerScope(),
                genAllScheduleLoadedState(
                    snapshot = genAllScheduleSnapshot(uiStates = listOf(initState))
                )
            )
            .send(event)
            .join()
        verify(selectionRepository)
    }
}