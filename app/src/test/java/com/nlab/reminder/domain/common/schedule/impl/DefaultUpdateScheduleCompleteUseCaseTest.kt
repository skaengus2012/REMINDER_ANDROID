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

package com.nlab.reminder.domain.common.schedule.impl

import com.nlab.reminder.core.kotlin.coroutine.Delay
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.ScheduleItemRequest
import com.nlab.reminder.domain.common.schedule.ScheduleRepository
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultUpdateScheduleCompleteUseCaseTest {
    @Test
    fun `complete will update after pending complete update when useCase invoked`() = runTest {
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.get(ScheduleItemRequest.FindByScheduleId(schedule.id()))) doReturn flow {
                emit(listOf(schedule))
            }
        }
        val delay: Delay = mock()
        val updateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(scheduleRepository, delay)
        val prevDelayedOrder = inOrder(scheduleRepository, delay)
        val afterDelayedOrder = inOrder(delay, scheduleRepository)
        updateScheduleCompleteUseCase(schedule.id(), isComplete)

        prevDelayedOrder.verify(scheduleRepository, once()).updatePendingComplete(schedule.id(), isComplete)
        prevDelayedOrder.verify(delay, once())()
        afterDelayedOrder.verify(delay, once())()
        afterDelayedOrder.verify(scheduleRepository, once()).updateComplete(schedule.id(), schedule.isComplete)
    }

    @Test
    fun `complete will be skipped when schedule was empty`() = runTest {
        val schedule: Schedule = genSchedule()
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.get(ScheduleItemRequest.FindByScheduleId(schedule.id()))) doReturn flow { emit(emptyList()) }
        }
        val updateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(scheduleRepository, mock())
        updateScheduleCompleteUseCase(schedule.id(), genBoolean())
        verify(scheduleRepository, never()).updateComplete(schedule.id(), isComplete = true)
        verify(scheduleRepository, never()).updateComplete(schedule.id(), isComplete = false)
    }
}