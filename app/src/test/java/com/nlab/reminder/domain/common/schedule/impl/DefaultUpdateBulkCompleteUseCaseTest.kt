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

package com.nlab.reminder.domain.common.schedule.impl

import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.ScheduleRepository
import com.nlab.reminder.domain.common.schedule.UpdateRequest
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class DefaultUpdateBulkCompleteUseCaseTest {
    @Test
    fun `update schedules with distinct`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()
        val bulkCompleteUseCase = DefaultUpdateBulkCompleteUseCase(scheduleRepository)
        val schedule: Schedule = genSchedule()
        val schedules: List<Schedule> = List(10) { schedule }
        val isComplete: Boolean = genBoolean()

        bulkCompleteUseCase(schedules, isComplete)
        verify(scheduleRepository, once()).update(
            UpdateRequest.BulkCompletes(
                listOf(schedule.id),
                isComplete
            )
        )
    }

    @Test
    fun `update schedules with sorted visiblePriority`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()
        val bulkCompleteUseCase = DefaultUpdateBulkCompleteUseCase(scheduleRepository)
        val size = 10L
        val schedules: List<Schedule> = List(size.toInt()) { index ->
            genSchedule(scheduleId = index.toLong(), visiblePriority = size - index)
        }
        val isComplete: Boolean = genBoolean()

        bulkCompleteUseCase(schedules, isComplete)
        verify(scheduleRepository, once()).update(
            UpdateRequest.BulkCompletes(
                schedules.map { it.id }.reversed(),
                isComplete
            )
        )
    }
}