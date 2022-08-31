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

import com.nlab.reminder.domain.common.schedule.CompleteMark
import com.nlab.reminder.domain.common.schedule.CompleteMarkRepository
import com.nlab.reminder.domain.common.schedule.ScheduleId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thread-safe CompleteRequest Manager
 *
 * @author Doohyun
 */
class ScopedCompleteMarkRepository : CompleteMarkRepository {
    private val chunkRequests: MutableStateFlow<Map<ScheduleId, CompleteMark>> = MutableStateFlow(emptyMap())

    override fun get(): Flow<Map<ScheduleId, CompleteMark>> = chunkRequests.asStateFlow()

    override suspend fun insert(completeMarks: Map<ScheduleId, CompleteMark>) {
        chunkRequests.update { old -> old + completeMarks }
    }

    override suspend fun updateToApplied(completeMarks: Map<ScheduleId, CompleteMark>) {
        chunkRequests.update { old ->
            old.mapValues { (key, value) ->
                if (completeMarks[key] == value) value.copy(isApplied = true)
                else value
            }
        }
    }
}