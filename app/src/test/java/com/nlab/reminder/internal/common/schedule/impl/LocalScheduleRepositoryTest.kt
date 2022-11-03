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
    fun `notify 2 times schedules when ScheduleDao sent 2 times data`() = runTest {
        val isComplete: Boolean = genBoolean()

        notifySchedulesWhenScheduleDaoSent2TimesData(
            ScheduleItemRequest.Find,
            setupMock = { scheduleDao, mockFlow -> whenever(scheduleDao.findAsStream()) doReturn mockFlow }
        )
        notifySchedulesWhenScheduleDaoSent2TimesData(
            ScheduleItemRequest.FindWithComplete(isComplete),
            setupMock = { scheduleDao, mockFlow -> whenever(scheduleDao.findWithCompleteAsStream(isComplete)) doReturn mockFlow }
        )
    }

    private fun notifySchedulesWhenScheduleDaoSent2TimesData(
        scheduleItemRequest: ScheduleItemRequest,
        setupMock: (ScheduleDao, Flow<List<ScheduleEntityWithTagEntities>>) -> Unit
    ) = runTest {
        val executeDispatcher = genFlowExecutionDispatcher(testScheduler)
        val expectedSchedules: List<Schedule> = listOf(
            genSchedule(tags = emptyList()),
            genSchedule(tags = genTags().sortedBy { it.name })
        )
        val delayBetweenElements = 500L
        val scheduleDao: ScheduleDao = mock {
            setupMock(mock, flow {
                expectedSchedules[0].toScheduleEntityWithTagEntities()
                    .let(::listOf)
                    .also { emit(it) }

                delay(delayBetweenElements)
                expectedSchedules[1].toScheduleEntityWithTagEntities()
                    .let { entity -> entity.copy(tagEntities = entity.tagEntities.sortedBy { it.name }.reversed()) }
                    .let(::listOf)
                    .also { emit(it) }
            }.flowOn(executeDispatcher))
        }
        val actualSchedules = mutableListOf<Schedule>()
        LocalScheduleRepository(scheduleDao)
            .get(scheduleItemRequest)
            .onEach { actualSchedules += it.first() }
            .launchIn(genFlowObserveCoroutineScope())

        advanceTimeBy(delayTimeMillis = delayBetweenElements * 2)
        assertThat(actualSchedules, equalTo(expectedSchedules))
    }

    @Test
    fun `update result for updateComplete was success`() = runTest {
        val (repositoryParam, daoParam) = genRepositoryParamAndDaoParams(
            genRandomUpdateValue = { genBoolean() },
            genRequest = { scheduleId, isComplete -> ModifyCompleteRequest(scheduleId, isComplete) }
        )
        val scheduleDao: ScheduleDao = mock()
        val updateResult = LocalScheduleRepository(scheduleDao).updateCompletes(repositoryParam)

        verify(scheduleDao, once()).updateCompletes(daoParam)
        assertThat(updateResult.isSuccess, equalTo(true))
    }

    @Test
    fun `update result for updateVisiblePriority was success`() = runTest {
        val (repositoryParam, daoParam) = genRepositoryParamAndDaoParams(
            genRandomUpdateValue = { genLong() },
            genRequest = { scheduleId, visiblePriority -> ModifyVisiblePriorityRequest(scheduleId, visiblePriority) }
        )

        val scheduleDao: ScheduleDao = mock()
        val result = LocalScheduleRepository(scheduleDao).updateVisiblePriorities(repositoryParam)

        verify(scheduleDao, once()).updateVisiblePriorities(daoParam)
        assertThat(result.isSuccess, equalTo(true))
    }

    private fun <T, U> genRepositoryParamAndDaoParams(
        genRandomUpdateValue: () -> T,
        genRequest: (ScheduleId, T) -> U
    ): Pair<List<U>, List<Pair<Long, T>>> {
        val schedules: List<Schedule> = genSchedules()
        val updateValues: List<T> = List(schedules.size) { genRandomUpdateValue() }
        val repositoryParam: List<U> =
            schedules.mapIndexed { index, schedule -> genRequest(schedule.id(), updateValues[index]) }
        val daoParam: List<Pair<Long, T>> =
            schedules.mapIndexed { index, schedule -> schedule.id().value to updateValues[index] }

        return repositoryParam to daoParam
    }
}