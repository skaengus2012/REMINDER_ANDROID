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
import com.nlab.reminder.core.data.repository.ScheduleSelectedIdRepository
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.domain.common.kotlin.coroutine.inject.DefaultDispatcher
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * @author Doohyun
 */
@ViewModelScoped
class MapToScheduleElementsUseCase @Inject constructor(
    private val completeMarkRepository: ScheduleCompleteMarkRepository,
    private val linkMetadataTableRepository: LinkMetadataTableRepository,
    private val selectionRepository: ScheduleSelectedIdRepository,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    operator fun invoke(
        schedulesStream: Flow<List<Schedule>>
    ): Flow<List<ScheduleElement>> = combine(
        schedulesStream,
        completeMarkRepository.getStream(),
        linkMetadataTableRepository.getStream(),
        selectionRepository.getStream(),
        transform = ::createScheduleItems
    ).flowOn(dispatcher)
}

private fun createScheduleItems(
    schedules: List<Schedule>,
    completeMarkTable: Map<ScheduleId, Boolean>,
    linkMetadataTable: LinkMetadataTable,
    selectedIds: Set<ScheduleId>,
): List<ScheduleElement> = schedules.map { schedule ->
    ScheduleElement(
        schedule,
        isCompleteMarked = completeMarkTable[schedule.id] ?: schedule.isComplete,
        linkMetadata = schedule.link
            .takeUnless(Link::isEmpty)
            ?.let { linkMetadataTable.value[it] },
        isSelected = schedule.id in selectedIds
    )
}