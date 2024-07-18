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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.foundation.annotation.Generated
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.repository.ScheduleDeleteRequest
import com.nlab.reminder.core.data.repository.ScheduleGetStreamRequest
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.ScheduleUpdateRequest
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.local.database.ScheduleDao
import com.nlab.reminder.core.local.database.ScheduleEntityWithTagEntities
import com.nlab.reminder.core.data.repository.fake.FakeScheduleRepositoryDelegate
import com.nlab.reminder.core.data.local.database.toModel
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
class LocalScheduleRepository(private val scheduleDao: ScheduleDao) : ScheduleRepository {
    @Generated
    override fun getTodaySchedulesCount(): Flow<Long> =
        FakeScheduleRepositoryDelegate.getTodaySchedulesCount()

    @Generated
    override fun getTimetableSchedulesCount(): Flow<Long> =
        FakeScheduleRepositoryDelegate.getTimetableSchedulesCount()

    @Generated
    override fun getAllSchedulesCount(): Flow<Long> =
        FakeScheduleRepositoryDelegate.getAllSchedulesCount()

    override fun getAsStream(request: ScheduleGetStreamRequest): Flow<List<Schedule>> {
        val entitiesFlow = when (request) {
            is ScheduleGetStreamRequest.All -> scheduleDao.findAsStream()
            is ScheduleGetStreamRequest.ByComplete -> scheduleDao.findByCompleteAsStream(request.isComplete)
        }

        return entitiesFlow.map { entities ->
            entities.map(ScheduleEntityWithTagEntities::toModel)
        }
    }

    override suspend fun update(request: ScheduleUpdateRequest): Result<Unit> = when (request) {
        // When outside the catch block, jacoco does not recognize. 😭
        is ScheduleUpdateRequest.Completes -> catching {
            scheduleDao.updateCompletes(request.idToCompleteTable.mapKeys { (key) -> key.value })
        }

        is ScheduleUpdateRequest.VisiblePriority -> catching {
            scheduleDao.updateVisiblePriorities(request.idToVisiblePriorityTable.mapKeys { (key) -> key.value })
        }
    }

    override suspend fun delete(request: ScheduleDeleteRequest): Result<Unit> = when (request) {
        // When outside the catch block, jacoco does not recognize. 😭
        is ScheduleDeleteRequest.ByComplete -> catching {
            scheduleDao.deleteByComplete(request.isComplete)
        }

        is ScheduleDeleteRequest.ById -> catching {
            scheduleDao.deleteByScheduleIds(listOf(request.scheduleId.value))
        }

        is ScheduleDeleteRequest.ByIds -> catching {
            scheduleDao.deleteByScheduleIds(request.scheduleIds.map { it.value })
        }
    }
}