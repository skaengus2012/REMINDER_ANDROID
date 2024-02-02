/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

import com.nlab.reminder.core.data.model.anyScheduleId
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.ScheduleDeleteRequest
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.ScheduleUpdateRequest
import com.nlab.reminder.core.domain.CalculateItemSwapResultUseCase
import com.nlab.reminder.core.domain.CompleteScheduleWithIdsUseCase
import com.nlab.reminder.core.domain.CompleteScheduleWithMarkUseCase
import com.nlab.reminder.core.domain.FetchLinkMetadataUseCase
import com.nlab.reminder.core.schedule.model.genScheduleElements
import com.nlab.reminder.core.schedule.model.mapToScheduleElementsAsImmutableList
import com.nlab.statekit.middleware.interceptor.scenario
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import com.nlab.testkit.once
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class AllScheduleInterceptorTest {
    @Test
    fun `Given State Loaded, When OnCompletedScheduleVisibilityToggleClicked, Then Repository called setShown with negative isCompletedScheduleShown`() = runTest {
        testOnCompletedScheduleVisibilityToggleClicked(currentCompletedScheduleShown = true)
        testOnCompletedScheduleVisibilityToggleClicked(currentCompletedScheduleShown = false)
    }

    private suspend fun TestScope.testOnCompletedScheduleVisibilityToggleClicked(
        currentCompletedScheduleShown: Boolean
    ) {
        val completedScheduleShownRepository: CompletedScheduleShownRepository = mock {
            whenever(mock.setShown(currentCompletedScheduleShown.not())) doReturn Result.Success(Unit)
        }

        genInterceptor(completedScheduleShownRepository = completedScheduleShownRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(isCompletedScheduleShown = currentCompletedScheduleShown))
            .action(AllScheduleAction.OnCompletedScheduleVisibilityToggleClicked)
            .dispatchIn(testScope = this)
        verify(completedScheduleShownRepository, once()).setShown(isShown = currentCompletedScheduleShown.not())
    }

    @Test
    fun `Given State Loaded, When OnCompletedScheduleDeleteClicked, Then Repository called delete`() = runTest {
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.delete(ScheduleDeleteRequest.ByComplete(true))) doReturn Result.Success(Unit)
        }

        genInterceptor(scheduleRepository = scheduleRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded())
            .action(AllScheduleAction.OnCompletedScheduleDeleteClicked)
            .dispatchIn(testScope = this)
        verify(scheduleRepository, once()).delete(ScheduleDeleteRequest.ByComplete(true))
    }

    @Test
    fun `Given State Loaded, When OnScheduleDeleteClicked, Then Repository called delete`() = runTest {
        val schedule = genSchedule()
        val repositoryDeleteRequest = ScheduleDeleteRequest.ById(schedule.id)
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.delete(repositoryDeleteRequest)) doReturn Result.Success(Unit)
        }
        genInterceptor(scheduleRepository = scheduleRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = schedule.mapToScheduleElementsAsImmutableList()))
            .action(AllScheduleAction.OnScheduleDeleteClicked(position = 0))
            .dispatchIn(testScope = this)
        verify(scheduleRepository, once()).delete(repositoryDeleteRequest)
    }

    @Test
    fun `Given State Loaded with empty schedules, When OnScheduleDeleteClicked, Then Repository never called`() = runTest {
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.delete(any())) doReturn Result.Success(Unit)
        }
        genInterceptor(scheduleRepository = scheduleRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = persistentListOf()))
            .action(AllScheduleAction.OnScheduleDeleteClicked(position = 0))
            .dispatchIn(testScope = this)
        verify(scheduleRepository, never()).delete(any())
    }

    @Test
    fun `Given State Loaded, When OnSelectedSchedulesDeleteClicked, Then Repository called delete`() = runTest {
        val scheduleIds = List(genInt(min = 1, max = 5)) { genScheduleId() }
        val repositoryDeleteRequest = ScheduleDeleteRequest.ByIds(scheduleIds)
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.delete(repositoryDeleteRequest)) doReturn Result.Success(Unit)
        }

        genInterceptor(scheduleRepository = scheduleRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded())
            .action(AllScheduleAction.OnSelectedSchedulesDeleteClicked(scheduleIds))
            .dispatchIn(testScope = this)
        verify(scheduleRepository, once()).delete(repositoryDeleteRequest)
    }

    @Test
    fun `Given State Loaded, When OnScheduleCompleteClicked, Then CompleteScheduleWithMarkUseCase invoked`() = runTest {
        val isComplete = genBoolean()
        val schedule = genSchedule(isComplete = isComplete.not())
        val useCase: CompleteScheduleWithMarkUseCase = mock {
            whenever(mock(schedule.id, isComplete)) doReturn Result.Success(Unit)
        }
        genInterceptor(completeScheduleWithMark = useCase)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = schedule.mapToScheduleElementsAsImmutableList()))
            .action(AllScheduleAction.OnScheduleCompleteClicked(position = 0, isComplete))
            .dispatchIn(testScope = this)
        verify(useCase, once()).invoke(schedule.id, isComplete)
    }

    @Test
    fun `Given State Loaded, When OnScheduleCompleteClicked with wrong position, Then CompleteScheduleWithMarkUseCase never invoked`() = runTest {
        val isComplete = genBoolean()
        val useCase: CompleteScheduleWithMarkUseCase = mock()
        genInterceptor(completeScheduleWithMark = useCase)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = persistentListOf()))
            .action(AllScheduleAction.OnScheduleCompleteClicked(position = 0, isComplete))
            .dispatchIn(testScope = this)
        verify(useCase, never()).invoke(anyScheduleId(), isComplete)
    }

    @Test
    fun `Given State Loaded, When OnSelectedSchedulesCompleteClicked, Then CompleteScheduleWithIdsUseCase invoked`() = runTest {
        val schedules = genSchedules()
        val scheduleIds = schedules.map { it.id }
        val isComplete = genBoolean()
        val useCase: CompleteScheduleWithIdsUseCase = mock {
            whenever(mock(scheduleIds, isComplete)) doReturn Result.Success(Unit)
        }
        genInterceptor(completeScheduleWithIds = useCase)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = schedules.mapToScheduleElementsAsImmutableList()))
            .action(AllScheduleAction.OnSelectedSchedulesCompleteClicked(scheduleIds, isComplete))
            .dispatchIn(testScope = this)
        verify(useCase, once()).invoke(scheduleIds, isComplete)
    }

    @Test
    fun `Given state Loaded, When OnScheduleItemMoved and swap result was empty, scheduleRepository never called`() = runTest {
        val schedules = genSchedules()
        val items = schedules.mapToScheduleElementsAsImmutableList()
        val from = genInt(min = 0, max = items.size - 1)
        val to = genInt(min = 0, max = items.size - 1)
        val useCase: CalculateItemSwapResultUseCase = mock {
            whenever(mock(items, from, to)) doReturn emptyList()
        }
        val scheduleRepository: ScheduleRepository = mock()
        genInterceptor(scheduleRepository = scheduleRepository, calculateItemSwapResult = useCase)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = items))
            .action(AllScheduleAction.OnScheduleItemMoved(from, to))
            .dispatchIn(testScope = this)
        verify(scheduleRepository, never()).update(any())
    }

    @Test
    fun `Given state Loaded, When OnScheduleItemMoved, scheduleRepository call updated`() = runTest {
        val schedules = List(10) { index ->
            val indexToLong = index.toLong()
            genSchedule(scheduleId = genScheduleId(indexToLong), visiblePriority = indexToLong)
        }
        val from = 2
        val to = 4
        val items = schedules.mapToScheduleElementsAsImmutableList()
        val fakeSwapResult = items.subList(from, to)
        val useCase: CalculateItemSwapResultUseCase = mock {
            whenever(mock(items, 2, 4)) doReturn fakeSwapResult
        }
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.update(any())) doReturn Result.Success(Unit)
        }
        genInterceptor(scheduleRepository = scheduleRepository, calculateItemSwapResult = useCase)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = items))
            .action(AllScheduleAction.OnScheduleItemMoved(from, to))
            .dispatchIn(testScope = this)
        verify(scheduleRepository, once()).update(
            request = fakeSwapResult
                .associateBy({ it.id }, { it.visiblePriority })
                .let(ScheduleUpdateRequest::VisiblePriority)
        )
    }

    @Test
    fun `Given any state, When ScheduleLoaded, Then fetchLinkMetadata invoked`() = runTest {
        val useCase: FetchLinkMetadataUseCase = mock()
        genInterceptor(fetchLinkMetadata = useCase)
            .scenario()
            .initState(genAllScheduleUiState())
            .action(AllScheduleAction.ScheduleElementsLoaded(genScheduleElements(), genBoolean()))
            .dispatchIn(testScope = this)
        verify(useCase, once()).invoke(any())
    }
}

private fun genInterceptor(
    scheduleRepository: ScheduleRepository = mock(),
    completedScheduleShownRepository: CompletedScheduleShownRepository = mock(),
    completeScheduleWithMark: CompleteScheduleWithMarkUseCase = mock(),
    completeScheduleWithIds: CompleteScheduleWithIdsUseCase = mock(),
    fetchLinkMetadata: FetchLinkMetadataUseCase = mock(),
    calculateItemSwapResult: CalculateItemSwapResultUseCase = mock()
): AllScheduleInterceptor = AllScheduleInterceptor(
    scheduleRepository = scheduleRepository,
    completedScheduleShownRepository = completedScheduleShownRepository,
    completeScheduleWithMark = completeScheduleWithMark,
    completeScheduleWithIds = completeScheduleWithIds,
    fetchLinkMetadata = fetchLinkMetadata,
    calculateItemSwapResult = calculateItemSwapResult
)