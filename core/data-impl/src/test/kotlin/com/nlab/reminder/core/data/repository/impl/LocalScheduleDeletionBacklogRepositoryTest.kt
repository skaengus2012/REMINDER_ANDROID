/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

import com.nlab.reminder.core.data.model.genScheduleDeletionBacklogAndEntities
import com.nlab.reminder.core.data.model.genScheduleDeletionBacklogId
import com.nlab.reminder.core.data.model.genScheduleDeletionBacklogs
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.local.database.dao.ScheduleDeletionBacklogDAO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsSame.sameInstance
import org.junit.Test

/**
 * @author Doohyun
 */
class LocalScheduleDeletionBacklogRepositoryTest {
    @Test
    fun `Given dao insert succeeds, When save, Then returns success`() = runTest {
        val scheduleIds = setOf(genScheduleId())
        val scheduleRawIds = scheduleIds.toSet { it.rawId }
        val scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO = mockk(relaxed = true)
        val repository = genLocalScheduleDeletionBacklogRepository(scheduleDeletionBacklogDAO)
        
        val result = repository.save(scheduleIds)
        assertThat(result.isSuccess, equalTo(true))
        coVerify(exactly = 1) {
            scheduleDeletionBacklogDAO.insertAll(scheduleRawIds)
        }
    }

    @Test
    fun `Given dao insert fails, When save, Then returns failure`() = runTest {
        val expectedException = RuntimeException()
        val scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO = mockk {
            coEvery { insertAll(any()) } throws expectedException
        }
        val repository = genLocalScheduleDeletionBacklogRepository(scheduleDeletionBacklogDAO)
        
        val actualException = repository.save(setOf(genScheduleId())).exceptionOrNull()
        assertThat(actualException, sameInstance(expectedException))
    }

    @Test
    fun `Given backlogIds, When delete, Then calls dao to delete with rawIds`() = runTest {
        val backlogIds = genScheduleDeletionBacklogs().toSet { it.id }
        val backlogRawIds = backlogIds.toSet { it.value }
        val scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO = mockk(relaxed = true)
        val repository = genLocalScheduleDeletionBacklogRepository(scheduleDeletionBacklogDAO)
        
        val result = repository.delete(backlogIds)
        assertThat(result.isSuccess, equalTo(true))
        coVerify(exactly = 1) {
            scheduleDeletionBacklogDAO.deleteByIds(backlogRawIds)
        }
    }

    @Test
    fun `Given dao delete fails, When delete, Then returns failure`() = runTest {
        val expectedException = RuntimeException()
        val scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO = mockk {
            coEvery { deleteByIds(any()) } throws expectedException
        }
        val repository = genLocalScheduleDeletionBacklogRepository(scheduleDeletionBacklogDAO)
        
        val actualException = repository
            .delete(setOf(genScheduleDeletionBacklogId()))
            .exceptionOrNull()
        assertThat(actualException, sameInstance(expectedException))
    }

    @Test
    fun `Given dao returns entities, When getBacklogs, Then returns mapped backlogs`() = runTest {
        val expectedBacklogAndEntities = genScheduleDeletionBacklogAndEntities()
        val entities = expectedBacklogAndEntities.map { (_, entity) -> entity }
        val scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO = mockk {
            coEvery { getAll() } returns entities
        }
        val repository = genLocalScheduleDeletionBacklogRepository(scheduleDeletionBacklogDAO)
        
        val actualBacklogs = repository.getBacklogs().getOrThrow()
        assertThat(
            actualBacklogs,
            equalTo(expectedBacklogAndEntities.toSet { (backlog) -> backlog })
        )
    }

    @Test
    fun `Given dao fails to get entities, When getBacklogs, Then returns failure`() = runTest {
        val expectedException = RuntimeException()
        val scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO = mockk {
            coEvery { getAll() } throws expectedException
        }
        val repository = genLocalScheduleDeletionBacklogRepository(scheduleDeletionBacklogDAO)
        
        val actualException = repository.getBacklogs().exceptionOrNull()
        assertThat(actualException, sameInstance(expectedException))
    }
}

private fun genLocalScheduleDeletionBacklogRepository(
    scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO = mockk()
): LocalScheduleDeletionBacklogRepository = LocalScheduleDeletionBacklogRepository(
    scheduleDeletionBacklogDAO = scheduleDeletionBacklogDAO
)
