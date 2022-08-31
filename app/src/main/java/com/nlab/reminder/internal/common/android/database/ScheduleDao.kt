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

package com.nlab.reminder.internal.common.android.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
@Dao
abstract class ScheduleDao {
    @Insert
    abstract suspend fun insert(schedule: ScheduleEntity): Long

    @Transaction
    @Query("SELECT * FROM schedule ORDER BY is_complete, visible_priority")
    abstract fun find(): Flow<List<ScheduleEntityWithTagEntities>>

    @Transaction
    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete ORDER BY is_complete, visible_priority")
    abstract fun findByComplete(isComplete: Boolean): Flow<List<ScheduleEntityWithTagEntities>>

    @Query("UPDATE schedule SET is_complete = :isComplete WHERE schedule_id = :scheduleId")
    abstract suspend fun updateComplete(scheduleId: Long, isComplete: Boolean)

    @Transaction
    open suspend fun updateComplete(requests: Map<Long, Boolean>) {
        requests.forEach { (scheduleId, isComplete) -> updateComplete(scheduleId, isComplete) }
    }

    @Delete
    abstract suspend fun delete(schedule: ScheduleEntity)
}