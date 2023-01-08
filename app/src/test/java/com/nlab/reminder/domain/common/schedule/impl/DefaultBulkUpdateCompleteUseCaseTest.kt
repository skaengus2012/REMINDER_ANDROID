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

import com.nlab.reminder.domain.common.schedule.*
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
class DefaultBulkUpdateCompleteUseCaseTest {
    @Test
    fun `update schedules with distinct`() = runTest {
        val schedule: Schedule = genSchedule()
        testInvokeTemplate(
            inputSchedules = List(10) { schedule }.toSet(),
            expectedScheduleIds = listOf(schedule.id)
        )
    }

    @Test
    fun `update schedules with sorted visiblePriority`() = runTest {
        val size = 10L
        val schedules: List<Schedule> = List(size.toInt()) { index ->
            genSchedule(scheduleId = index.toLong(), visiblePriority = size - index)
        }
        testInvokeTemplate(
            inputSchedules =
            List(size.toInt()) { index -> genSchedule(scheduleId = index.toLong(), visiblePriority = size - index) }
                .toSet(),
            expectedScheduleIds = schedules.map { it.id }.reversed(),
        )
    }

    private suspend fun testInvokeTemplate(
        inputSchedules: Set<Schedule>,
        expectedScheduleIds: List<ScheduleId>
    ) {
        val scheduleRepository: ScheduleRepository = mock()
        val bulkCompleteUseCase = DefaultBulkUpdateCompleteUseCase(scheduleRepository)
        val isComplete: Boolean = genBoolean()

        bulkCompleteUseCase(inputSchedules, isComplete)
        verify(scheduleRepository, once()).update(UpdateRequest.BulkCompletes(expectedScheduleIds, isComplete))
    }
}