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

package com.nlab.reminder.core.component.schedule.infra

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nlab.reminder.core.data.model.genScheduleDeletionBacklogs
import com.nlab.reminder.core.data.repository.DeleteScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleDeletionBacklogRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test

/**
 * @author Doohyun
 */
class ScheduleDeletionWorkerTest {
    @Test
    fun `Given backlogs are empty, When doWork, Then return success`() = runTest {
        val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
        val scheduleDeletionBacklogRepository: ScheduleDeletionBacklogRepository = mockk {
            coEvery { getBacklogs() } returns Result.success(emptySet())
        }
        val worker = genScheduleDeletionWorker(
            scheduleRepository = scheduleRepository,
            scheduleDeletionBacklogRepository = scheduleDeletionBacklogRepository
        )
        val result = worker.doWork()
        assertThat(result, equalTo(ListenableWorker.Result.success()))
        verify { scheduleRepository wasNot Called }
    }

    @Test
    fun `Given backlogs are present, When doWork, Then delete schedules and return success`() = runTest {
        val backlogs = genScheduleDeletionBacklogs()
        val scheduleIds = backlogs.toSet { it.scheduleId }
        val scheduleRepository: ScheduleRepository = mockk {
            coEvery { delete(DeleteScheduleQuery.ByIds(scheduleIds)) } returns com.nlab.reminder.core.kotlin.Result.Success(Unit)
        }
        val scheduleDeletionBacklogRepository: ScheduleDeletionBacklogRepository = mockk {
            coEvery { getBacklogs() } returns Result.success(backlogs)
        }
        val worker = genScheduleDeletionWorker(
            scheduleRepository = scheduleRepository,
            scheduleDeletionBacklogRepository = scheduleDeletionBacklogRepository
        )
        val result = worker.doWork()
        
        assertThat(result, equalTo(ListenableWorker.Result.success()))
        coVerify(exactly = 1) {
            scheduleRepository.delete(DeleteScheduleQuery.ByIds(scheduleIds))
        }
    }

    @Test
    fun `Given loading backlogs fails, When doWork, Then return failure`() = runTest {
        val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
        val errorMessage = "Test error"
        val scheduleDeletionBacklogRepository: ScheduleDeletionBacklogRepository = mockk {
            coEvery { getBacklogs() } returns Result.failure(RuntimeException(errorMessage))
        }
        val worker = genScheduleDeletionWorker(
            scheduleRepository = scheduleRepository,
            scheduleDeletionBacklogRepository = scheduleDeletionBacklogRepository
        )
        val result = worker.doWork()
        
        assertThat(
            result,
            equalTo(ListenableWorker.Result.failure(workDataOf("key_schedule_deletion_error_message" to errorMessage)))
        )
    }

    @Test
    fun `Given deleting schedules fails, When doWork, Then return failure`() = runTest {
        val backlogs = genScheduleDeletionBacklogs()
        val scheduleIds = backlogs.toSet { it.scheduleId }
        val errorMessage = "Delete failed"
        val scheduleRepository: ScheduleRepository = mockk {
            coEvery { delete(DeleteScheduleQuery.ByIds(scheduleIds)) } returns com.nlab.reminder.core.kotlin.Result.Failure(RuntimeException(errorMessage))
        }
        val scheduleDeletionBacklogRepository: ScheduleDeletionBacklogRepository = mockk {
            coEvery { getBacklogs() } returns Result.success(backlogs)
        }
        val worker = genScheduleDeletionWorker(
            scheduleRepository = scheduleRepository,
            scheduleDeletionBacklogRepository = scheduleDeletionBacklogRepository
        )
        val result = worker.doWork()
        
        assertThat(
            result,
            equalTo(ListenableWorker.Result.failure(workDataOf("key_schedule_deletion_error_message" to errorMessage)))
        )
    }


}

private fun genScheduleDeletionWorker(
    appContext: Context = mockk(relaxed = true),
    workerParams: WorkerParameters = mockk(relaxed = true),
    scheduleRepository: ScheduleRepository = mockk(),
    scheduleDeletionBacklogRepository: ScheduleDeletionBacklogRepository = mockk()
): ScheduleDeletionWorker = ScheduleDeletionWorker(
    appContext = appContext,
    workerParams = workerParams,
    scheduleRepository = scheduleRepository,
    scheduleDeletionBacklogRepository = scheduleDeletionBacklogRepository
)
