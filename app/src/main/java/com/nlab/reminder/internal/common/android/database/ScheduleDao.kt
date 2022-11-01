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

    @Query(
        """
        SELECT visible_priority 
        FROM schedule 
        WHERE is_complete = :isComplete
        ORDER BY visible_priority 
        DESC LIMIT 1
        """
    )
    abstract fun getMaxVisiblePriority(isComplete: Boolean): Long?

    @Query("UPDATE schedule SET visible_priority = :visiblePriority WHERE schedule_id = :scheduleId")
    abstract suspend fun updateVisiblePriority(scheduleId: Long, visiblePriority: Long)

    @Transaction
    open suspend fun updateVisiblePriorities(requests: List<Pair<Long, Long>>) {
        requests.forEach { (scheduleId, visiblePriority) -> updateVisiblePriority(scheduleId, visiblePriority) }
    }

    @Query(
        """
        UPDATE schedule 
        SET is_complete = :isComplete, visible_priority = :visiblePriority 
        WHERE schedule_id = :scheduleId
        """
    )
    abstract suspend fun updateComplete(scheduleId: Long, isComplete: Boolean, visiblePriority: Long)

    @Transaction
    open suspend fun updateCompletes(requests: List<Pair<Long, Boolean>>) {
        updateCompletesInternal(
            scheduleIds = requests.filter { (_, isComplete) -> isComplete }.map { (scheduleId) -> scheduleId },
            isComplete = true
        )
        updateCompletesInternal(
            scheduleIds = requests.filter { (_, isComplete) -> isComplete.not() }.map { (scheduleId) -> scheduleId },
            isComplete = false
        )
    }

    private suspend fun updateCompletesInternal(scheduleIds: List<Long>, isComplete: Boolean) {
        if (scheduleIds.isEmpty()) return

        val maxVisiblePriority: Long = getMaxVisiblePriority(isComplete) ?: -1
        scheduleIds.forEachIndexed { index, scheduleId ->
            updateComplete(scheduleId, isComplete, visiblePriority = maxVisiblePriority + index + 1)
        }
    }

    @Delete
    abstract suspend fun delete(schedule: ScheduleEntity)
}