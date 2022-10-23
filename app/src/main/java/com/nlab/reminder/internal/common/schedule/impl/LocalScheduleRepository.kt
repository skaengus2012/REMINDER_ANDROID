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

import androidx.paging.*
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
    override fun get(request: ScheduleItemRequest): Flow<List<Schedule>> {
        val resultFlow: Flow<List<ScheduleEntityWithTagEntities>> = when (request) {
            is ScheduleItemRequest.Find -> scheduleDao.find()
            is ScheduleItemRequest.FindByComplete -> scheduleDao.findByComplete(request.isComplete)
        }

        return resultFlow.map { entities -> entities.map(ScheduleEntityWithTagEntities::toSchedule)  }
    }

    override fun getAsPagingData(request: ScheduleItemRequest, pagingConfig: PagingConfig): Flow<PagingData<Schedule>> {
        val pager = Pager(pagingConfig, pagingSourceFactory = {
            when (request) {
                is ScheduleItemRequest.Find -> scheduleDao.findAsPagingSource()
                is ScheduleItemRequest.FindByComplete -> scheduleDao.findAsPagingSourceByComplete(request.isComplete)
            }
        })

        return pager.flow.map { pagingData -> pagingData.map(ScheduleEntityWithTagEntities::toSchedule) }
    }

    override suspend fun updateComplete(requests: List<ScheduleCompleteRequest>): Result<Unit> = catching {
        scheduleDao.updateComplete(
            requests.fold(emptyMap()) { acc, (scheduleId, isComplete) -> acc + (scheduleId.value to isComplete) }
        )
    }
}