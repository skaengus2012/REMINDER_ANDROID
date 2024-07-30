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

import com.nlab.reminder.core.data.model.*
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.NonNegativeLong
import kotlinx.coroutines.flow.Flow

/**
 * @author thalys
 */
interface ScheduleRepository {
    suspend fun save(query: SaveScheduleQuery): Result<Schedule>
    suspend fun updateBulk(query: UpdateSchedulesQuery): Result<Unit>
    suspend fun delete(query: DeleteScheduleQuery): Result<Unit>
    fun getSchedulesAsStream(request: GetScheduleQuery): Flow<Collection<Schedule>>
    fun getScheduleCountAsStream(query: GetScheduleCountQuery): Flow<Long>
}

sealed class SaveScheduleQuery {
    data class Add(val content: ScheduleContent) : SaveScheduleQuery()
    data class Modify(val id: ScheduleId, val content: ScheduleContent) : SaveScheduleQuery()
}

sealed class UpdateSchedulesQuery private constructor() {
    class Completes(val idToCompleteTable: Map<ScheduleId, Boolean>) : UpdateSchedulesQuery()

    data class VisiblePriorities(
        val idToVisiblePriorityTable: Map<ScheduleId, NonNegativeLong>,
        val isCompletedRange: Boolean
    ) : UpdateSchedulesQuery()
}

sealed class DeleteScheduleQuery private constructor() {
    data class ByComplete(val isComplete: Boolean) : DeleteScheduleQuery()
    data class ByIds(val scheduleIds: Set<ScheduleId>) : DeleteScheduleQuery()
}

sealed class GetScheduleQuery private constructor() {
    data object All : GetScheduleQuery()
    data class ByComplete(val isComplete: Boolean) : GetScheduleQuery()
}

sealed class GetScheduleCountQuery private constructor() {
    data object Today : GetScheduleCountQuery()
    data object Timetable : GetScheduleCountQuery()
    data object All : GetScheduleCountQuery()
}