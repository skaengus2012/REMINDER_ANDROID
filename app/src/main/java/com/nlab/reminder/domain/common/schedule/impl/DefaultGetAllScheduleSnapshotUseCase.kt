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

package com.nlab.reminder.domain.common.schedule.impl

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleSnapshot
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleSnapshotUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author thalys
 */
class DefaultGetAllScheduleSnapshotUseCase(
    private val coroutineScope: CoroutineScope,
    private val pagingConfig: PagingConfig,
    private val scheduleRepository: ScheduleRepository,
    private val scheduleUiStatePagingFlowFactory: ScheduleUiStatePagingFlowFactory,
) : GetAllScheduleSnapshotUseCase {
    override fun invoke(): Flow<AllScheduleSnapshot> {
        val isDoneScheduleShown: Boolean = true
        return getScheduleFlow(isDoneScheduleShown)
            .cachedIn(coroutineScope)
            .let(scheduleUiStatePagingFlowFactory::with)
            .map { scheduleUiStates -> AllScheduleSnapshot(emptyList(), isDoneScheduleShown, scheduleUiStates) }
    }

    private fun getScheduleFlow(isDoneScheduleShown: Boolean): Flow<PagingData<Schedule>> =
        scheduleRepository.getAsPagingData(
            request = when (isDoneScheduleShown) {
                true -> ScheduleItemRequest.Find
                false -> ScheduleItemRequest.FindByComplete(isComplete = false)
            },
            pagingConfig
        )
}