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

package com.nlab.reminder.internal.common.schedule.impl

import com.nlab.reminder.core.kotlin.util.isFailure
import com.nlab.reminder.core.kotlin.util.isSuccess
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.tag.genTags
import com.nlab.reminder.internal.common.android.database.*
import com.nlab.reminder.internal.common.database.toScheduleEntityWithTagEntities
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
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
    fun `observe 2 times schedules when ScheduleDao sent 2 times notifications`() = runTest {
        val isComplete: Boolean = genBoolean()

        observeSchedulesWhenScheduleDaoNotified2Times(
            ScheduleItemRequest.Find,
            setupMock = { scheduleDao, mockFlow -> whenever(scheduleDao.find()) doReturn mockFlow }
        )
        observeSchedulesWhenScheduleDaoNotified2Times(
            ScheduleItemRequest.FindByComplete(isComplete),
            setupMock = { scheduleDao, mockFlow -> whenever(scheduleDao.findByComplete(isComplete)) doReturn mockFlow }
        )
    }

    private fun observeSchedulesWhenScheduleDaoNotified2Times(
        scheduleItemRequest: ScheduleItemRequest,
        setupMock: (ScheduleDao, Flow<List<ScheduleEntityWithTagEntities>>) -> Unit
    ) = runTest {
        val executeDispatcher = genFlowExecutionDispatcher(testScheduler)
        val firstSchedule = genSchedule(tags = emptyList())
        val secondSchedule = genSchedule(tags = genTags().sortedBy { it.name })
        val scheduleDao: ScheduleDao = mock {
            setupMock(mock, flow {
                firstSchedule.toScheduleEntityWithTagEntities()
                    .let(::listOf)
                    .also { emit(it) }

                delay(1_000)
                secondSchedule.toScheduleEntityWithTagEntities()
                    .let { entity -> entity.copy(tagEntities = entity.tagEntities.sortedBy { it.name }.reversed()) }
                    .let(::listOf)
                    .also { emit(it) }
            }.flowOn(executeDispatcher))
        }
        val actualSchedules = mutableListOf<Schedule>()

        LocalScheduleRepository(scheduleDao)
            .get(scheduleItemRequest)
            .onEach { actualSchedules += it.first() }
            .launchIn(genFlowObserveDispatcher())

        advanceTimeBy(2_000)
        assertThat(actualSchedules, equalTo(listOf(firstSchedule, secondSchedule)))
    }

    @Test
    fun `update result for updateComplete was success`() = runTest {
        val schedules: List<Schedule> = genSchedules()
        val completes: List<Boolean> = List(schedules.size) { genBoolean() }
        val repositoryParam: Set<ScheduleCompleteRequest> =
            schedules
                .mapIndexed { index, schedule -> ScheduleCompleteRequest(schedule.id(), completes[index]) }
                .toSet()
        val daoParam = buildMap {
            schedules.forEachIndexed { index, schedule -> this[schedule.id().value] = completes[index] }
        }

        val scheduleDao: ScheduleDao = mock()
        val updateResult = LocalScheduleRepository(scheduleDao).updateComplete(repositoryParam)

        verify(scheduleDao, once()).updateComplete(daoParam)
        assertThat(updateResult.isSuccess, equalTo(true))
    }

    @Test
    fun `update result for updateComplete was failed`() = runTest {
        val scheduleDao: ScheduleDao = mock {
            whenever(mock.updateComplete(any())) doThrow RuntimeException()
        }
        val updateResult = LocalScheduleRepository(scheduleDao).updateComplete(emptySet())
        assertThat(updateResult.isFailure, equalTo(true))
    }
}