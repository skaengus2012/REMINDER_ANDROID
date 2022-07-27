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

package com.nlab.reminder.domain.feature.schedule.all.impl

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.ScheduleItemPagingRequest
import com.nlab.reminder.domain.common.schedule.ScheduleItemRequest
import com.nlab.reminder.domain.common.schedule.ScheduleRepository
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleReport
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleReportUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * @author Doohyun
 */
class DefaultGetAllScheduleReportUseCase(
    private val scheduleRepository: ScheduleRepository,
    private val pagingConfig: PagingConfig,
    private val dispatcher: CoroutineDispatcher
) : GetAllScheduleReportUseCase {
    override fun invoke(coroutineScope: CoroutineScope): Flow<AllScheduleReport> =
        createAllScheduleReportFlow(coroutineScope)
            .flowOn(dispatcher)

    private fun createAllScheduleReportFlow(coroutineScope: CoroutineScope): Flow<AllScheduleReport> =
        combine(
            scheduleRepository
                .get(ScheduleItemRequest.FindByComplete(isComplete = false)),
            scheduleRepository
                .getAsPagingData(ScheduleItemPagingRequest.FindByComplete(isComplete = true), pagingConfig)
                .cachedIn(coroutineScope),
            transform = ::transformToScheduleReport
        )

    companion object {
        private fun transformToScheduleReport(
            doingSchedules: List<Schedule>,
            doneSchedules: PagingData<Schedule>
        ): AllScheduleReport = AllScheduleReport(
            doingSchedules,
            doneSchedules,
            isDoneScheduleShown = true
        )
    }
}