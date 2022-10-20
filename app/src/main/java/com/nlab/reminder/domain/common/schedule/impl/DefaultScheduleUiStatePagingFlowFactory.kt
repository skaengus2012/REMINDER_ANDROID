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

import androidx.paging.PagingData
import androidx.paging.map
import com.nlab.reminder.domain.common.schedule.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * @author thalys
 */
class DefaultScheduleUiStatePagingFlowFactory(
    private val completeMarkRepository: CompleteMarkRepository
) : ScheduleUiStatePagingFlowFactory {
    override fun with(schedules: Flow<PagingData<Schedule>>): Flow<PagingData<ScheduleUiState>> =
        schedules.combine(
            completeMarkRepository.get().distinctUntilChanged(),
            transform = ::transformToScheduleUiStates
        )

    companion object {
        private fun transformToScheduleUiStates(
            schedules: PagingData<Schedule>,
            completeMarkSnapshot: Map<ScheduleId, CompleteMark>
        ): PagingData<ScheduleUiState> = schedules.map(mapToScheduleUiState(completeMarkSnapshot))

        private fun mapToScheduleUiState(
            completeMarkSnapshot: Map<ScheduleId, CompleteMark>
        ): (Schedule) -> ScheduleUiState = { schedule -> ScheduleUiState(schedule, completeMarkSnapshot) }
    }
}