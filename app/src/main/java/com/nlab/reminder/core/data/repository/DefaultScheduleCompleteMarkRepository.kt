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

package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.ScheduleCompleteMark
import com.nlab.reminder.core.data.model.ScheduleCompleteMarkTable
import com.nlab.reminder.core.data.model.ScheduleId
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.*

/**
 * @author thalys
 */
class DefaultScheduleCompleteMarkRepository(
    private val createCompleteMark: (isComplete: Boolean) -> ScheduleCompleteMark
) : ScheduleCompleteMarkRepository {
    private val chunkRequests = MutableStateFlow(ScheduleCompleteMarkTable())

    override fun get(): StateFlow<ScheduleCompleteMarkTable> = chunkRequests.asStateFlow()

    override suspend fun insert(scheduleId: ScheduleId, isComplete: Boolean) {
        val completeMark = createCompleteMark(isComplete)
        chunkRequests.update { old ->
            ScheduleCompleteMarkTable(old.value.toPersistentMap().put(scheduleId, completeMark))
        }
    }

    override suspend fun clear() {
        chunkRequests.update { ScheduleCompleteMarkTable() }
    }
}