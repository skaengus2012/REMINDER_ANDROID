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

package com.nlab.reminder.internal.common.schedule

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.internal.common.android.database.ScheduleDao
import com.nlab.reminder.internal.common.android.database.ScheduleEntityWithTagEntities
import com.nlab.reminder.internal.common.android.database.toSchedule
import com.nlab.reminder.internal.common.android.database.toSchedules
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
class LocalScheduleRepository(
    private val scheduleDao: ScheduleDao,
) : ScheduleRepository {
    override fun get(request: ScheduleItemRequest): Flow<List<Schedule>> {
        val resultFlow: Flow<List<ScheduleEntityWithTagEntities>> =
            when (request) {
                is ScheduleItemRequest.FindByScheduleId -> {
                    scheduleDao.findById(scheduleId = request.scheduleId.value)
                }
                is ScheduleItemRequest.FindByComplete -> {
                    scheduleDao.findByComplete(isComplete = request.isComplete)
                }
            }

        return resultFlow.map { it.toSchedules() }
    }

    override fun getAsPagingData(
        request: ScheduleItemPagingRequest,
        pagingConfig: PagingConfig
    ): Flow<PagingData<Schedule>> {
        val pager = Pager(pagingConfig, pagingSourceFactory = {
            when (request) {
                is ScheduleItemPagingRequest.FindByComplete -> {
                    scheduleDao.findByCompleteAsPagingSource(isComplete = request.isComplete)
                }
            }
        })
        return pager.flow.map { pagedData -> pagedData.map(ScheduleEntityWithTagEntities::toSchedule) }
    }

    override suspend fun updatePendingComplete(scheduleId: ScheduleId, isComplete: Boolean) {
        scheduleDao.updatePendingComplete(scheduleId.value, isComplete)
    }

    override suspend fun updateComplete(scheduleId: ScheduleId, isComplete: Boolean) {
        scheduleDao.updateComplete(scheduleId.value, isComplete)
    }
}