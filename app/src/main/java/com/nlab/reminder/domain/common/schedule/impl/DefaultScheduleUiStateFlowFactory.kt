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

import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.schedule.isSelected
import com.nlab.reminder.domain.common.util.link.LinkMetadata
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
class DefaultScheduleUiStateFlowFactory(
    private val completeMarkRepository: CompleteMarkRepository,
    private val selectionRepository: SelectionRepository
) : ScheduleUiStateFlowFactory {
    override fun with(schedulesStream: Flow<List<Schedule>>): Flow<List<ScheduleUiState>> =
        combine(
            schedulesStream,
            completeMarkRepository.get(),
            selectionRepository.selectionTableStream(),
            transform = this::transformToScheduleUiStates
        )

    @ExcludeFromGeneratedTestReport
    private fun transformToScheduleUiStates(
        schedules: List<Schedule>,
        completeMarkTable: CompleteMarkTable,
        selectionTable: SelectionTable,
    ): List<ScheduleUiState> = schedules.map { schedule ->
        ScheduleUiState(
            schedule,
            linkMetadata = LinkMetadata.Empty,
            isCompleteMarked = completeMarkTable.isCompleteMarked(schedule),
            isSelected = selectionTable.isSelected(schedule)
        )
    }
}