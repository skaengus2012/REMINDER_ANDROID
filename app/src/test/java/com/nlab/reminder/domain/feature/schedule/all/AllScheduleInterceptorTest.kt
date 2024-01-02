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

import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.ScheduleDeleteRequest
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.domain.CompleteScheduleWithMarkUseCase
import com.nlab.statekit.middleware.interceptor.scenario
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import com.nlab.testkit.once
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class AllScheduleInterceptorTest {
    @Test
    fun `Given State Loaded, When OnCompletedScheduleVisibilityUpdateClicked, Then Repository called setShown`() = runTest {
        val isVisible = genBoolean()
        val completedScheduleShownRepository: CompletedScheduleShownRepository = mock {
            whenever(mock.setShown(isVisible)) doReturn Result.Success(Unit)
        }

        genInterceptor(completedScheduleShownRepository = completedScheduleShownRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded())
            .action(AllScheduleAction.OnCompletedScheduleVisibilityUpdateClicked(isVisible))
            .dispatchIn(testScope = this)
        verify(completedScheduleShownRepository, once()).setShown(isShown = isVisible)
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
        val scheduleId = genScheduleId()
        val repositoryDeleteRequest = ScheduleDeleteRequest.ById(scheduleId)
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.delete(repositoryDeleteRequest)) doReturn Result.Success(Unit)
        }

        genInterceptor(scheduleRepository = scheduleRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded())
            .action(AllScheduleAction.OnScheduleDeleteClicked(scheduleId))
            .dispatchIn(testScope = this)
        verify(scheduleRepository, once()).delete(repositoryDeleteRequest)
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
    fun `Given State Loaded, When OnScheduleCompleteClicked, Then CompleteScheduleWithMarkUseCase occurred`() = runTest {
        val schedule = genSchedule()
        val isComplete = genBoolean()
        val useCase: CompleteScheduleWithMarkUseCase = mock {
            whenever(mock(schedule.scheduleId, isComplete)) doReturn Result.Success(Unit)
        }
        genInterceptor(completeScheduleWithMark = useCase)
            .scenario()
            .initState(genAllScheduleUiStateLoaded(schedules = listOf(schedule)))
            .action(AllScheduleAction.OnScheduleCompleteClicked(schedule.scheduleId, isComplete))
            .dispatchIn(testScope = this)
        verify(useCase, once()).invoke(schedule.scheduleId, isComplete)
    }
}

private fun genInterceptor(
    scheduleRepository: ScheduleRepository = mock(),
    completedScheduleShownRepository: CompletedScheduleShownRepository = mock(),
    completeScheduleWithMark: CompleteScheduleWithMarkUseCase = mock()
): AllScheduleInterceptor = AllScheduleInterceptor(
    scheduleRepository = scheduleRepository,
    completedScheduleShownRepository = completedScheduleShownRepository,
    completeScheduleWithMark = completeScheduleWithMark
)