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

package com.nlab.reminder.core.component.schedule

import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.ScheduleDeletionBacklogRepository
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test

/**
 * @author Doohyun
 */
class DefaultDeleteScheduleUseCaseTest {
    @Test
    fun `Given save succeeds, When delete, Then save and register`() = runTest {
        val scheduleIds = setOf(genScheduleId())
        val scheduleDeletionBacklogRepository: ScheduleDeletionBacklogRepository = mockk {
            coEvery { save(scheduleIds) } returns Result.success(Unit)
        }
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase = mockk {
            coEvery { this@mockk.invoke() } returns ScheduleJobResult.Success
        }
        val useCase = DefaultDeleteScheduleUseCase(
            deletionBacklogRepository = scheduleDeletionBacklogRepository,
            requestScheduleDeletionJob = requestScheduleDeletionJob
        )

        val result = useCase.invoke(scheduleIds)

        assertThat(result, equalTo(ScheduleJobResult.Success))
        coVerifyOrder {
            scheduleDeletionBacklogRepository.save(scheduleIds)
            requestScheduleDeletionJob.invoke()
        }
    }

    @Test
    fun `Given save fails, When delete, Then does not register`() = runTest {
        val scheduleIds = setOf(genScheduleId())
        val expectedException = RuntimeException()
        val useCase = DefaultDeleteScheduleUseCase(
            deletionBacklogRepository = mockk {
                coEvery { save(scheduleIds) } returns Result.failure(expectedException)
            },
            requestScheduleDeletionJob = mockk(relaxed = true)
        )
        
        val result = useCase.invoke(scheduleIds)

        assertThat(
            result,
            equalTo(ScheduleJobResult.Failure(expectedException))
        )
    }

    @Test
    fun `Given empty scheduleIds, When delete, Then do nothing`() = runTest {
        val useCase = DefaultDeleteScheduleUseCase(
            deletionBacklogRepository = mockk(relaxed = true),
            requestScheduleDeletionJob = mockk(relaxed = true)
        )
        
        val result = useCase.invoke(emptySet())
        
        assertThat(result, equalTo(ScheduleJobResult.Success))
    }

    @Test
    fun `Given save succeeds and job fails, When delete, Then return failure`() = runTest {
        val scheduleIds = setOf(genScheduleId())
        val expectedException = RuntimeException()
        val useCase = DefaultDeleteScheduleUseCase(
            deletionBacklogRepository = mockk {
                coEvery { save(scheduleIds) } returns Result.success(Unit)
            },
            requestScheduleDeletionJob = mockk {
                coEvery {
                    this@mockk.invoke()
                } returns ScheduleJobResult.Failure(expectedException)
            }
        )
        
        val result = useCase.invoke(scheduleIds)
        
        assertThat(
            result,
            equalTo(ScheduleJobResult.Failure(expectedException))
        )
    }

    @Test
    fun `Given save succeeds and job is retrying, When delete, Then return retrying`() = runTest {
        val scheduleIds = setOf(genScheduleId())
        val useCase = DefaultDeleteScheduleUseCase(
            deletionBacklogRepository = mockk {
                coEvery { save(scheduleIds) } returns Result.success(Unit)
            },
            requestScheduleDeletionJob = mockk {
                coEvery { this@mockk.invoke() } returns ScheduleJobResult.Retrying
            }
        )
        
        val result = useCase.invoke(scheduleIds)
        
        assertThat(result, equalTo(ScheduleJobResult.Retrying))
    }
}
