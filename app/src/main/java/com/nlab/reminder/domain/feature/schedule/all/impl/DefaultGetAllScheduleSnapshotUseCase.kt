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
import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleSnapshot
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleSnapshotUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * @author thalys
 */
class DefaultGetAllScheduleSnapshotUseCase(
    coroutineScope: CoroutineScope,
    pagingConfig: PagingConfig,
    scheduleRepository: ScheduleRepository,
    private val doneScheduleShownRepository: DoneScheduleShownRepository,
    private val scheduleUiStatePagingFlowFactory: ScheduleUiStatePagingFlowFactory,
) : GetAllScheduleSnapshotUseCase {
    private val findAllSchedules: Flow<PagingData<Schedule>> =
        scheduleRepository
            .getAsPagingData(ScheduleItemRequest.Find, pagingConfig)
            .cachedIn(coroutineScope)
    private val findNotCompleteSchedules: Flow<PagingData<Schedule>> =
        scheduleRepository
            .getAsPagingData(ScheduleItemRequest.FindByComplete(isComplete = false), pagingConfig)
            .cachedIn(coroutineScope)

    @ExperimentalCoroutinesApi
    override fun invoke(): Flow<AllScheduleSnapshot> =
        doneScheduleShownRepository.get()
            .flatMapLatest(this::getSnapshot)
            .buffer(0)

    private fun getSnapshot(isDoneScheduleShown: Boolean): Flow<AllScheduleSnapshot> =
        getSchedules(isDoneScheduleShown)
            .let(scheduleUiStatePagingFlowFactory::with)
            .map { scheduleUiStates -> AllScheduleSnapshot(scheduleUiStates, isDoneScheduleShown) }

    private fun getSchedules(isDoneScheduleShown: Boolean): Flow<PagingData<Schedule>> =
        if (isDoneScheduleShown) findAllSchedules
        else findNotCompleteSchedules
}