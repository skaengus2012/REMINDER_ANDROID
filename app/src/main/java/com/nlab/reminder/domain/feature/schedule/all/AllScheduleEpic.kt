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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.data.repository.AllScheduleData
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.ScheduleGetStreamRequest
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.domain.MapToScheduleItemsUseCase
import com.nlab.reminder.core.kotlin.coroutine.flow.flatMapLatest
import com.nlab.statekit.middleware.epic.Epic
import com.nlab.statekit.util.buildDslEpic
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author thalys
 */
internal class AllScheduleEpic @Inject constructor(
    @AllScheduleData completedScheduleShownRepository: CompletedScheduleShownRepository,
    scheduleRepository: ScheduleRepository,
    mapToScheduleItems: MapToScheduleItemsUseCase
) : Epic<AllScheduleAction> by buildDslEpic(buildDSL = {
    whileStateUsed {
        completedScheduleShownRepository.getAsStream().flatMapLatest { isCompletedScheduleShown ->
            scheduleRepository.getAllSchedulesStream(isCompletedScheduleShown)
                .let(mapToScheduleItems::invoke)
                .map { items -> AllScheduleAction.ScheduleItemsLoaded(items, isCompletedScheduleShown) }
        }
    }
})

private fun ScheduleRepository.getAllSchedulesStream(
    isCompletedScheduleShown: Boolean
): Flow<List<Schedule>> =
    getAsStream(
        when (isCompletedScheduleShown) {
            true -> ScheduleGetStreamRequest.All
            else -> ScheduleGetStreamRequest.ByComplete(isComplete = false)
        }
    )
