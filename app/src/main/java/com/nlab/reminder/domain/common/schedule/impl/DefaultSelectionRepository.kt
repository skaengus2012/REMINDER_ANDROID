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

import com.nlab.reminder.domain.common.schedule.ScheduleId
import com.nlab.reminder.domain.common.schedule.SelectionRepository
import com.nlab.reminder.domain.common.schedule.SelectionTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * @author thalys
 */
class DefaultSelectionRepository(initTable: SelectionTable = SelectionTable()) : SelectionRepository {
    private val selectedStateFlow = MutableStateFlow(initTable)

    override fun selectionTableStream(): StateFlow<SelectionTable> = selectedStateFlow.asStateFlow()

    override suspend fun setSelected(scheduleId: ScheduleId, isSelect: Boolean) {
        selectedStateFlow.update { old -> old + (scheduleId to isSelect) }
    }

    override suspend fun clearSelected() {
        selectedStateFlow.emit(emptyMap())
    }
}