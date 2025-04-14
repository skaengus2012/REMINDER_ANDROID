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

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleAndEntities
import com.nlab.reminder.core.data.model.genScheduleAndEntity
import com.nlab.reminder.core.data.model.genScheduleContent
import com.nlab.reminder.core.data.model.toDTO
import com.nlab.reminder.core.data.repository.DeleteScheduleQuery
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.UpdateAllScheduleQuery
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.kotlin.isSuccess
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleRepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.transaction.InsertAndGetScheduleWithExtraTransaction
import com.nlab.reminder.core.local.database.transaction.InsertScheduleWithExtraResult
import com.nlab.reminder.core.local.database.transaction.UpdateAndGetScheduleWithExtraTransaction
import com.nlab.reminder.core.local.database.transaction.UpdateScheduleWithExtraResult
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class LocalScheduleRepositoryTest {
    @Test
    fun `Given schedule and tagIds, When save is called, Then it should insert and return the schedule`() = runTest {
        // given
        val (schedule, entity) = genScheduleAndEntities(count = 1).first()
        val tagIds = List(size = genInt(min = 2, max = 5)) { TagId(it.toLong()) }
        val contentDTO = schedule.content.toDTO()
        val insertAndGetScheduleWithExtra: InsertAndGetScheduleWithExtraTransaction = mockk {
            coEvery { invoke(contentDTO = contentDTO) } returns InsertScheduleWithExtraResult(
                scheduleEntity = entity.scheduleEntity,
                scheduleTagListEntities = entity.scheduleTagListEntities,
                repeatDetailEntities = entity.repeatDetailEntities
            )
        }
        val repository = genLocalScheduleRepository(
            insertAndGetScheduleWithExtra = insertAndGetScheduleWithExtra
        )

        // when
        val actual = repository.save(query = SaveScheduleQuery.Add(schedule.content, tagIds.toSet()))

        // then
        coVerify(exactly = 1) {
            insertAndGetScheduleWithExtra(contentDTO)
        }
        assertThat(actual.getOrThrow(), equalTo(schedule))
    }

    @Test
    fun `Given schedule and tagIds, When modify is called, Then it should update and return the schedule`() = runTest {
        // given
        val (schedule, entity) = genScheduleAndEntities(count = 1).first()
        val tagIds = List(size = genInt(min = 2, max = 5)) { TagId(it.toLong()) }
        val rawScheduleId = schedule.id.rawId
        val contentDTO = schedule.content.toDTO()
        val updateAndGetScheduleWithExtra: UpdateAndGetScheduleWithExtraTransaction = mockk {
            coEvery {
                invoke(scheduleId = rawScheduleId, contentDTO = contentDTO)
            } returns UpdateScheduleWithExtraResult(
                scheduleEntity = entity.scheduleEntity,
                scheduleTagListEntities = entity.scheduleTagListEntities,
                repeatDetailEntities = entity.repeatDetailEntities
            )
        }
        val repository = genLocalScheduleRepository(
            updateAndGetScheduleWithExtra = updateAndGetScheduleWithExtra
        )

        // when
        val actual = repository.save(query = SaveScheduleQuery.Modify(schedule.id, schedule.content, tagIds.toSet()))

        // then
        coVerify(exactly = 1) {
            updateAndGetScheduleWithExtra(rawScheduleId, contentDTO)
        }
        assertThat(actual.getOrThrow(), equalTo(schedule))
    }

    @Test
    fun `Given id-complete, When update completes, Then dao called update and return success`() = runTest {
        // given
        val sampleSize = genInt(min = 5, max = 10)
        val ids = List(sampleSize) { ScheduleId(it.toLong()) }
        val completes = List(sampleSize) { genBoolean() }
        val idToComplete = ids.zip(completes).toMap()
        val scheduleDAO: ScheduleDAO = mockk(relaxed = true)
        val repository = genLocalScheduleRepository(scheduleDAO = scheduleDAO)

        // when
        val result = repository.updateAll(UpdateAllScheduleQuery.Completes(idToComplete))

        // then
        coVerify(exactly = 1) {
            scheduleDAO.updateByCompletes(
                idToCompleteTable = ids.map { it.rawId }.zip(completes).toMap()
            )
        }
        assertThat(result.isSuccess, equalTo(true))
    }

    @Test
    fun `Given id-visiblePriority, When update priorities, Then dao called update and return success`() = runTest {
        // given
        val sampleSize = genInt(min = 5, max = 10)
        val ids = List(sampleSize) { ScheduleId(it.toLong()) }
        val visiblePriorities = List(sampleSize) { (it + 1).toNonNegativeLong() }
        val idToPriority = ids.zip(visiblePriorities).toMap()
        val scheduleDAO: ScheduleDAO = mockk(relaxed = true)
        val repository = genLocalScheduleRepository(scheduleDAO = scheduleDAO)

        // when
        val result = repository.updateAll(UpdateAllScheduleQuery.VisiblePriorities(idToPriority))

        // then
        coVerify(exactly = 1) {
            scheduleDAO.updateByVisiblePriorities(
                idToVisiblePriorityTable = ids.map { it.rawId }.zip(visiblePriorities.map { it }).toMap()
            )
        }
        assertThat(result.isSuccess, equalTo(true))
    }

    @Test
    fun `Given complete flag, When delete by complete, Then dao deletes by completion and return success`() = runTest {
        // given
        val isComplete = genBoolean()
        val scheduleDAO: ScheduleDAO = mockk(relaxed = true)
        val repository = genLocalScheduleRepository(scheduleDAO = scheduleDAO)

        // when
        val result = repository.delete(DeleteScheduleQuery.ByComplete(isComplete))

        // then
        coVerify(exactly = 1) {
            scheduleDAO.deleteByComplete(isComplete)
        }
        assertThat(result.isSuccess, equalTo(true))
    }

    @Test
    fun `Given scheduleIds, When delete by ids, Then dao deletes by scheduleIds and return success`() = runTest {
        // given
        val ids = List(genInt(min = 2, max = 5)) { ScheduleId(it.toLong()) }
        val scheduleDAO: ScheduleDAO = mockk(relaxed = true)
        val repository = genLocalScheduleRepository(scheduleDAO = scheduleDAO)

        // when
        val result = repository.delete(DeleteScheduleQuery.ByIds(ids.toSet()))

        // then
        coVerify(exactly = 1) {
            scheduleDAO.deleteByScheduleIds(scheduleIds = ids.map { it.rawId }.toSet())
        }
        assertThat(result.isSuccess, equalTo(true))
    }

    @Test
    fun `Given all query, When collect schedules, Then return schedules from dao`() = runTest {
        // given
        val (schedule, entity) = genScheduleAndEntity()
        val query = GetScheduleQuery.All

        // when
        val repository = genLocalScheduleRepository(
            scheduleRepeatDetailDAO = mockk {
                every { getAsStream() } returns flowOf(mapOf(entity.scheduleEntity to entity.repeatDetailEntities))
            },
            scheduleTagListDAO = mockk {
                every { findByScheduleIdsAsStream(scheduleIds = setOf(schedule.id.rawId)) } returns flowOf(
                    entity.scheduleTagListEntities.toTypedArray()
                )
            }
        )
        val result = repository
            .getSchedulesAsStream(query)
            .first()

        // then
        assertThat(result, equalTo(setOf(schedule)))
    }

    @Test
    fun `Given all query, When collect schedules, Then return schedules with no tags from dao`() = runTest {
        // given
        val (schedule, entity) = genScheduleAndEntity(
            schedule = genSchedule(
                content = genScheduleContent(tagIds = emptySet())
            )
        )
        val query = GetScheduleQuery.All

        // when
        val repository = genLocalScheduleRepository(
            scheduleRepeatDetailDAO = mockk {
                every { getAsStream() } returns flowOf(mapOf(entity.scheduleEntity to entity.repeatDetailEntities))
            },
            scheduleTagListDAO = mockk {
                every { findByScheduleIdsAsStream(scheduleIds = setOf(schedule.id.rawId)) } returns flowOf(
                    emptyArray()
                )
            }
        )
        val result = repository
            .getSchedulesAsStream(query)
            .first()

        // then
        assertThat(
            result.first().content.tagIds.isEmpty(),
            equalTo(true)
        )
    }

    @Test
    fun `Given completion status, When collect schedules, Then return schedules from dao`() = runTest {
        // given
        val (schedule, entity) = genScheduleAndEntity()
        val repository = genLocalScheduleRepository(
            scheduleRepeatDetailDAO = mockk {
                every { findByCompleteAsStream(isComplete = schedule.isComplete) } returns flowOf(
                    mapOf(entity.scheduleEntity to entity.repeatDetailEntities)
                )
            },
            scheduleTagListDAO = mockk {
                every { findByScheduleIdsAsStream(scheduleIds = setOf(schedule.id.rawId)) } returns flowOf(
                    entity.scheduleTagListEntities.toTypedArray()
                )
            }
        )

        // when
        val result = repository
            .getSchedulesAsStream(GetScheduleQuery.ByComplete(isComplete = schedule.isComplete))
            .first()

        // then
        assertThat(result, equalTo(setOf(schedule)))
    }
}

private fun genLocalScheduleRepository(
    scheduleDAO: ScheduleDAO = mockk(),
    scheduleRepeatDetailDAO: ScheduleRepeatDetailDAO = mockk(),
    scheduleTagListDAO: ScheduleTagListDAO = mockk(),
    insertAndGetScheduleWithExtra: InsertAndGetScheduleWithExtraTransaction = mockk(),
    updateAndGetScheduleWithExtra: UpdateAndGetScheduleWithExtraTransaction = mockk()
) = LocalScheduleRepository(
    scheduleDAO = scheduleDAO,
    scheduleRepeatDetailDAO = scheduleRepeatDetailDAO,
    scheduleTagListDAO = scheduleTagListDAO,
    insertAndGetScheduleWithExtra = insertAndGetScheduleWithExtra,
    updateAndGetScheduleWithExtra = updateAndGetScheduleWithExtra
)