/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

import app.cash.turbine.test
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genScheduleCompletionBacklogAndEntities
import com.nlab.reminder.core.data.model.genScheduleCompletionBacklogId
import com.nlab.reminder.core.data.model.genScheduleCompletionBacklogs
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.GetScheduleCompletionBacklogQuery
import com.nlab.reminder.core.data.repository.GetScheduleCompletionBacklogStreamQuery
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.local.database.dao.ScheduleCompletionBacklogDAO
import com.nlab.reminder.core.local.database.entity.ScheduleCompletionBacklogEntity
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genLong
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsSame.sameInstance
import org.junit.Test

/**
 * @author Doohyun
 */
class LocalScheduleCompletionBacklogRepositoryTest {
    @Test
    fun `Given dao insert succeeds, When save, Then returns new backlog`() = runTest {
        suspend fun verify(scheduleId: ScheduleId, targetCompleted: Boolean) {
            val scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO = mockk {
                coEvery { insertAndGet(scheduleId.rawId, targetCompleted) } returns ScheduleCompletionBacklogEntity(
                    backlogId = genLong(),
                    scheduleId = scheduleId.rawId,
                    targetCompleted = targetCompleted,
                    insertOrder = genLong()
                )
            }
            val repository = genLocalScheduleCompletionBacklogRepository(scheduleCompletionBacklogDAO)
            val newBacklog = repository.save(scheduleId, targetCompleted).getOrThrow()
            assertThat(newBacklog.scheduleId, equalTo(scheduleId))
            assertThat(newBacklog.targetCompleted, equalTo(targetCompleted))
        }

        verify(scheduleId = genScheduleId(), targetCompleted = true)
        verify(scheduleId = genScheduleId(), targetCompleted = false)
    }

    @Test
    fun `Given dao insert fails, When save, Then returns failure`() = runTest {
        val expectedException = RuntimeException()
        val scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO = mockk {
            coEvery { insertAndGet(any(), any()) } throws expectedException
        }
        val repository = genLocalScheduleCompletionBacklogRepository(scheduleCompletionBacklogDAO)
        val actualException = repository
            .save(scheduleId = genScheduleId(), targetCompleted = genBoolean())
            .exceptionOrNull()
        assertThat(actualException, sameInstance(expectedException))
    }

    @Test
    fun `Given backlogIds, When delete, Then calls dao to delete with rawIds`() = runTest {
        val backlogIds = genScheduleCompletionBacklogs().toSet { it.id }
        val backlogRawIds = backlogIds.toSet { it.rawId }
        val scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO = mockk(relaxed = true)
        val repository = genLocalScheduleCompletionBacklogRepository(scheduleCompletionBacklogDAO)
        repository.delete(backlogIds)
        coVerify(exactly = 1) {
            scheduleCompletionBacklogDAO.deleteByIds(backlogRawIds)
        }
    }

    @Test
    fun `Given dao delete fails, When delete, Then returns failure`() = runTest {
        val expectedException = RuntimeException()
        val scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO = mockk {
            coEvery { deleteByIds(any()) } throws expectedException
        }
        val repository = genLocalScheduleCompletionBacklogRepository(scheduleCompletionBacklogDAO)
        val actualException = repository
            .delete(setOf(genScheduleCompletionBacklogId()))
            .exceptionOrNull()
        assertThat(actualException, sameInstance(expectedException))
    }

    @Test
    fun `Given dao returns entities, When getBacklogs, Then returns mapped backlogs`() = runTest {
        suspend fun <T : GetScheduleCompletionBacklogQuery> verify(
            query: T,
            provideSuccessfulDAO: (T, List<ScheduleCompletionBacklogEntity>) -> ScheduleCompletionBacklogDAO
        ) {
            val expectedBacklogAndEntities = genScheduleCompletionBacklogAndEntities()
            val repository = genLocalScheduleCompletionBacklogRepository(
                scheduleCompletionBacklogDAO = provideSuccessfulDAO(
                    query,
                    expectedBacklogAndEntities.map { (_, entity) -> entity }
                )
            )
            val actualBacklogs = repository.getBacklogs(query).getOrThrow()
            assertThat(
                actualBacklogs,
                equalTo(expectedBacklogAndEntities.toSet { (backlog) -> backlog })
            )
        }
        verify(query = GetScheduleCompletionBacklogQuery.All) { _, entities ->
            mockk { coEvery { getAll() } returns entities }
        }
        verify(
            query = GetScheduleCompletionBacklogQuery.ByScheduleIdsUpToPriority(priority = genNonNegativeLong())
        ) { query, entities ->
            mockk {
                coEvery { findAllByScheduleIdsUpToInsertOrder(insertOrder = query.priority.value) } returns entities
            }
        }
    }

    @Test
    fun `Given dao fails to get entities, When getBacklogs, Then returns failure`() = runTest {
        suspend fun <T : GetScheduleCompletionBacklogQuery> verify(
            query: T,
            provideFailingDAO: (T, Throwable) -> ScheduleCompletionBacklogDAO
        ) {
            val expectedException = RuntimeException()
            val repository = genLocalScheduleCompletionBacklogRepository(
                scheduleCompletionBacklogDAO = provideFailingDAO(query, expectedException)
            )
            val actualException = repository.getBacklogs(query).exceptionOrNull()
            assertThat(actualException, sameInstance(expectedException))
        }

        verify(query = GetScheduleCompletionBacklogQuery.All) { _, throwable ->
            mockk { coEvery { getAll() } throws  throwable }
        }
        verify(
            query = GetScheduleCompletionBacklogQuery.ByScheduleIdsUpToPriority(priority = genNonNegativeLong())
        ) { query, throwable ->
            mockk {
                coEvery { findAllByScheduleIdsUpToInsertOrder(insertOrder = query.priority.value) } throws throwable
            }
        }
    }

    @Test
    fun `Given dao returns entity stream, When getting backlog stream, Then returns mapped backlog stream`() = runTest {
        val expectedBacklogAndEntities = genScheduleCompletionBacklogAndEntities()
        val scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO = mockk {
            coEvery { getLatestPerScheduleIdAsStream() } returns flowOf(
                expectedBacklogAndEntities.map { (_, entity) -> entity }
            )
        }
        val repository = genLocalScheduleCompletionBacklogRepository(scheduleCompletionBacklogDAO)
        repository.getBacklogsAsStream(GetScheduleCompletionBacklogStreamQuery.LatestPerScheduleId).test {
            val actualBacklogs = awaitItem()
            assertThat(
                actualBacklogs,
                equalTo(expectedBacklogAndEntities.toSet { (backlog) -> backlog })
            )
            awaitComplete()
        }
    }
}

private fun genLocalScheduleCompletionBacklogRepository(
    scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO = mockk()
): LocalScheduleCompletionBacklogRepository = LocalScheduleCompletionBacklogRepository(
    scheduleCompletionBacklogDAO = scheduleCompletionBacklogDAO
)