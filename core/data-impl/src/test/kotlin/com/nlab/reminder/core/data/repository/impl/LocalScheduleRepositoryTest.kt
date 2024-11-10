/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genScheduleAndEntities
import com.nlab.reminder.core.data.model.genScheduleAndEntity
import com.nlab.reminder.core.data.model.toLocalDTO
import com.nlab.reminder.core.data.repository.DeleteScheduleQuery
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.UpdateSchedulesQuery
import com.nlab.reminder.core.kotlin.genNonNegativeLong
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class LocalScheduleRepositoryTest {
    @Test
    fun `Given scheduleContent, when add, then dao called insertAndGet`() = runTest {
        val (expectedSchedule, entity) = genScheduleAndEntity()
        val input = expectedSchedule.content
        val scheduleDAO = mock<ScheduleDAO> {
            whenever(mock.insertAndGet(input.toLocalDTO())) doReturn entity
        }
        val actualSchedule = genScheduleRepository(scheduleDAO)
            .save(SaveScheduleQuery.Add(input))
            .getOrThrow()
        assertThat(actualSchedule, equalTo(expectedSchedule))
    }

    @Test
    fun `Given id and scheduleContent, when modify, then dao called updateAndGet`() = runTest {
        val (expectedSchedule, entity) = genScheduleAndEntity()
        val inputId = expectedSchedule.id
        val inputContent = expectedSchedule.content
        val scheduleDAO = mock<ScheduleDAO> {
            whenever(mock.updateAndGet(inputId.rawId, inputContent.toLocalDTO())) doReturn entity
        }
        val actualSchedule = genScheduleRepository(scheduleDAO)
            .save(SaveScheduleQuery.Modify(inputId, inputContent))
            .getOrThrow()
        assertThat(actualSchedule, equalTo(expectedSchedule))
    }

    @Test
    fun `When repository called updateBulk, Then dao also call update`() = runTest {
        suspend fun <T : UpdateSchedulesQuery> testUpdate(
            request: T,
            doVerify: suspend (ScheduleDAO).(T) -> Unit
        ) {
            val scheduleDAO = mock<ScheduleDAO>()
            val scheduleRepository = genScheduleRepository(scheduleDAO = scheduleDAO)
            scheduleRepository.updateBulk(request)
            verify(scheduleDAO, once()).doVerify(request)
        }

        val scheduleIdToComplete = List(genInt(min = 1, max = 10)) { ScheduleId(it.toLong()) }
            .associateWith { genBoolean() }
        testUpdate(
            request = UpdateSchedulesQuery.Completes(scheduleIdToComplete),
            doVerify = { updateByCompletes(scheduleIdToComplete.mapKeys { (id) -> id.rawId }) }
        )

        val scheduleIdToVisiblePriority = List(genInt(min = 1, max = 10)) { ScheduleId(it.toLong()) }
            .associateWith { genNonNegativeLong() }
        val isCompletedRange = genBoolean()
        testUpdate(
            request = UpdateSchedulesQuery.VisiblePriorities(scheduleIdToVisiblePriority, isCompletedRange),
            doVerify = {
                val idToVisiblePriorities = buildMap {
                    scheduleIdToVisiblePriority.forEach { (id, visiblePriority) ->
                        this[id.rawId] = visiblePriority.value
                    }
                }
                updateByVisiblePriorities(idToVisiblePriorities, isCompletedRange)
            }
        )
    }

    @Test
    fun `When repository called delete actions, Then dao also called delete`() = runTest {
        suspend fun <T : DeleteScheduleQuery> testDelete(
            request: T,
            doVerify: suspend (ScheduleDAO).(T) -> Unit
        ) {
            val scheduleDAO = mock<ScheduleDAO>()
            val scheduleRepository = genScheduleRepository(scheduleDAO = scheduleDAO)
            scheduleRepository.delete(request)
            verify(scheduleDAO, once()).doVerify(request)
        }

        val isComplete = genBoolean()
        val scheduleIds = List(genInt(min = 5, max = 10)) { ScheduleId(it.toLong()) }

        testDelete(
            request = DeleteScheduleQuery.ByComplete(isComplete),
            doVerify = { deleteByComplete(isComplete = isComplete) }
        )
        testDelete(
            request = DeleteScheduleQuery.ByIds(scheduleIds.toSet()),
            doVerify = { deleteByScheduleIds(scheduleIds.map { it.rawId }.toSet()) }
        )
    }

    @Test
    fun `Given schedules and entities, When repository called getSchedulesAsStream, Then return schedules`() = runTest {
        suspend fun testGetScheduleAsStream(
            scheduleDAO: ScheduleDAO,
            request: GetScheduleQuery,
            expectedResult: List<Schedule>
        ) {
            val scheduleRepository = genScheduleRepository(scheduleDAO)
            val actualSchedules = scheduleRepository.getSchedulesAsStream(request)
                .take(1)
                .first()

            assertThat(actualSchedules, equalTo(expectedResult))
        }

        val (schedules, entities) = genScheduleAndEntities()
        val isComplete = genBoolean()

        testGetScheduleAsStream(
            scheduleDAO = mock {
                whenever(mock.getAsStream()) doReturn flowOf(entities.toTypedArray())
            },
            request = GetScheduleQuery.All,
            expectedResult = schedules
        )
        testGetScheduleAsStream(
            scheduleDAO = mock {
                whenever(mock.findByCompleteAsStream(isComplete)) doReturn flowOf(entities.toTypedArray())
            },
            request = GetScheduleQuery.ByComplete(isComplete),
            expectedResult = schedules
        )
    }
}

private fun genScheduleRepository(
    scheduleDAO: ScheduleDAO = mock()
): ScheduleRepository = LocalScheduleRepository(scheduleDAO)