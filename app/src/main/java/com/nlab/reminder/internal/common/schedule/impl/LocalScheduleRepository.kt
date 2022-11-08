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
    override fun get(request: ScheduleRequest): Flow<List<Schedule>> {
        val resultFlow: Flow<List<ScheduleEntityWithTagEntities>> = when (request) {
            is ScheduleRequest.Find -> scheduleDao.findAsStream()
            is ScheduleRequest.FindWithComplete -> scheduleDao.findWithCompleteAsStream(request.isComplete)
        }

        return resultFlow.map { entities -> entities.map(ScheduleEntityWithTagEntities::toSchedule) }
    }

    override suspend fun updateCompletes(requests: List<ModifyCompleteRequest>): Result<Unit> =
        catching {
            scheduleDao.updateCompletes(
                requests = requests.map { request -> request.scheduleId.value to request.isComplete }
            )
        }

    override suspend fun updateVisiblePriorities(requests: List<ModifyVisiblePriorityRequest>): Result<Unit> =
        catching {
            scheduleDao.updateVisiblePriorities(
                requests = requests.map { request -> request.scheduleId.value to request.visiblePriority }
            )
        }

    override suspend fun delete(scheduleId: ScheduleId): Result<Unit> =
        catching { scheduleDao.deleteByScheduleId(scheduleId.value) }
}