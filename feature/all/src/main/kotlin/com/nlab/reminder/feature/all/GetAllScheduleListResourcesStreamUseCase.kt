/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.feature.all

import com.nlab.reminder.core.component.schedulelist.content.GetScheduleListResourcesStreamUseCase
import com.nlab.reminder.core.component.schedulelist.content.ScheduleListResource
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.qualifiers.ScheduleData
import com.nlab.reminder.core.data.qualifiers.ScheduleDataOption.All
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * @author Thalys
 */
internal class GetAllScheduleListResourcesStreamUseCase @Inject constructor(
    @param:ScheduleData(All) private val completedScheduleShownRepository: CompletedScheduleShownRepository,
    private val scheduleRepository: ScheduleRepository,
    private val getScheduleListResourcesStream: GetScheduleListResourcesStreamUseCase,
) {
    operator fun invoke(): Flow<List<ScheduleListResource>> = completedScheduleShownRepository
        .getAsStream()
        .flatMapLatest { isCompletedScheduleVisible ->
            scheduleRepository.getSchedulesAsStream(
                if (isCompletedScheduleVisible) GetScheduleQuery.All
                else GetScheduleQuery.ByComplete(isComplete = false)
            )
        }
        .map { schedules ->
            schedules.sortedWith(
                comparator = compareBy<Schedule> {
                    // isComplete asc (false -> true)
                    it.isComplete
                }.thenComparing { it.visiblePriority.value }
            )
        }
        .let(getScheduleListResourcesStream::invoke)
}