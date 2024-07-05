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

package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.kotlin.annotation.Generated
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import kotlinx.coroutines.flow.Flow

/**
 * @author thalys
 */
interface ScheduleRepository {
    fun getTodaySchedulesCount(): Flow<Long>
    fun getTimetableSchedulesCount(): Flow<Long>
    fun getAllSchedulesCount(): Flow<Long>
    fun getAsStream(request: ScheduleGetStreamRequest): Flow<List<Schedule>>
    suspend fun update(request: ScheduleUpdateRequest): Result<Unit>
    suspend fun delete(request: ScheduleDeleteRequest): Result<Unit>
}

sealed class ScheduleGetStreamRequest private constructor() {
    @Generated
    data object All : ScheduleGetStreamRequest()

    @Generated
    data class ByComplete(val isComplete: Boolean) : ScheduleGetStreamRequest()
}

sealed class ScheduleUpdateRequest private constructor() {
    @Generated
    data class Completes(val idToCompleteTable: Map<ScheduleId, Boolean>) : ScheduleUpdateRequest()

    @Generated
    data class VisiblePriority(val idToVisiblePriorityTable: Map<ScheduleId, Long>) : ScheduleUpdateRequest()
}

sealed class ScheduleDeleteRequest private constructor() {
    @Generated
    data class ByComplete(val isComplete: Boolean) : ScheduleDeleteRequest()

    @Generated
    data class ById(val scheduleId: ScheduleId) : ScheduleDeleteRequest()

    @Generated
    data class ByIds(val scheduleIds: Collection<ScheduleId>) : ScheduleDeleteRequest()
}