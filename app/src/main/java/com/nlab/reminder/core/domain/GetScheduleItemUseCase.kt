/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.isEmpty
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.SchedulesStreamRepository
import com.nlab.reminder.core.schedule.ScheduleItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * @author Doohyun
 */
class GetScheduleItemUseCase(
    private val schedulesStreamRepository: SchedulesStreamRepository,
    private val completeMarkRepository: ScheduleCompleteMarkRepository,
    private val linkMetadataTableRepository: LinkMetadataTableRepository,
) {
    operator fun invoke(): Flow<List<ScheduleItem>> = combine(
        schedulesStreamRepository.getStream(),
        completeMarkRepository.get(),
        linkMetadataTableRepository.get(),
        transform = ::createScheduleItems
    )
}

private fun createScheduleItems(
    schedules: List<Schedule>,
    completeMarkTable: Map<ScheduleId, Boolean>,
    linkMetadataTable: LinkMetadataTable
): List<ScheduleItem> = schedules.map { schedule ->
    ScheduleItem(
        schedule,
        isCompleteMarked = completeMarkTable[schedule.scheduleId] ?: schedule.isComplete,
        linkMetadata = schedule.link
            .takeUnless(Link::isEmpty)
            ?.let { linkMetadataTable.value[it] }
    )
}