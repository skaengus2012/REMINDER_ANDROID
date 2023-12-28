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

import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.internal.common.android.database.ScheduleDao
import com.nlab.reminder.internal.common.android.database.ScheduleEntityWithTagEntities
import com.nlab.reminder.internal.common.android.database.toSchedule
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
class LocalScheduleRepository(
    private val scheduleDao: ScheduleDao,
) : ScheduleRepository {
    override fun get(request: GetRequest): Flow<List<Schedule>> {
        val resultFlow: Flow<List<ScheduleEntityWithTagEntities>> = when (request) {
            is GetRequest.All -> scheduleDao.findAsStream()
            is GetRequest.ByComplete -> scheduleDao.findByCompleteAsStream(request.isComplete)
        }

        return resultFlow.map { entities -> entities.map(ScheduleEntityWithTagEntities::toSchedule) }
    }

    override suspend fun update(request: UpdateRequest): Result<Unit> = when (request) {
        // When outside the catch block, jacoco does not recognize. 😭
        is UpdateRequest.Completes -> catching {
            scheduleDao.updateCompletesLegacy(
                requests = request.values.map { request -> request.scheduleId.value to request.isComplete }
            )
        }
        is UpdateRequest.BulkCompletes -> catching {
            scheduleDao.updateCompletesLegacy(scheduleIds = request.scheduleIds.map { it.value }, request.isComplete)
        }
        is UpdateRequest.VisiblePriorities -> catching {
            scheduleDao.updateVisiblePriorities(
                requests = request.values.map { request -> request.scheduleId.value to request.visiblePriority }
            )
        }
    }

    override suspend fun delete(request: DeleteRequest): Result<Unit> = when (request) {
        // When outside the catch block, jacoco does not recognize. 😭
        is DeleteRequest.ById -> catching { scheduleDao.deleteByScheduleIds(listOf(request.scheduleId.value)) }
        is DeleteRequest.ByIds -> catching { scheduleDao.deleteByScheduleIds(request.scheduleIds.map { it.value }) }
        is DeleteRequest.ByComplete -> catching { scheduleDao.deleteByComplete(request.isComplete) }
    }
}