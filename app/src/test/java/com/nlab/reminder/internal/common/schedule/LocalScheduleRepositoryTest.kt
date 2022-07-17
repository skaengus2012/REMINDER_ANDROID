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

package com.nlab.reminder.internal.common.schedule

import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.ScheduleItemRequestConfig
import com.nlab.reminder.domain.common.schedule.ScheduleRepository
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.internal.common.android.database.*
import com.nlab.reminder.internal.common.database.from
import com.nlab.reminder.test.genBothify
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.*


/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalScheduleRepositoryTest {
    @Test
    fun `scheduleDao found when get`() {
        val expectedIsComplete = true
        val scheduleDao: ScheduleDao = mock()
        val scheduleRepository: ScheduleRepository = LocalScheduleRepository(scheduleDao, Dispatchers.Unconfined)
        val requestConfig = ScheduleItemRequestConfig(
            isComplete = expectedIsComplete
        )
        scheduleRepository.get(requestConfig)
        verify(scheduleDao, times(1)).find(isComplete = expectedIsComplete)
    }

    @Test
    fun `combined 2 time item when event notified as loading first schedule, loading tags update schedule`() = runTest {
        val firstSchedule = genSchedule(note = "", url = "", tags = emptyList())
        val secondSchedule = genSchedule(
            tags = listOf(
                Tag(tagId = 1, name = genBothify()),
                Tag(tagId = 2, name = genBothify()),
                Tag(tagId = 3, name = genBothify())
            ).sortedBy { it.name }
        )
        val firstState = ScheduleEntityWithTagEntities(
            scheduleEntity = from(firstSchedule).copy(description = null, url = null),
            tagEntities = emptyList()
        )
        val secondState = ScheduleEntityWithTagEntities(
            scheduleEntity = from(secondSchedule),
            tagEntities = secondSchedule.tags.map { from(it) }.reversed()
        )

        val scheduleDao: ScheduleDao = mock {
            whenever(mock.find(isComplete = true)) doReturn flow {
                emit(listOf(firstState))
                delay(1_000)
                emit(listOf(secondState))
            }
        }

        val actualSchedules = mutableListOf<Schedule>()
        val scheduleRepository: ScheduleRepository =
            LocalScheduleRepository(scheduleDao, StandardTestDispatcher(testScheduler))
        CoroutineScope(Dispatchers.Unconfined).launch {
            scheduleRepository.get(ScheduleItemRequestConfig()).collect { actualSchedules += it.first() }
        }

        advanceTimeBy(2_000)
        assertThat(actualSchedules, equalTo(listOf(firstSchedule, secondSchedule)))
    }
}