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

import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
@Dao
interface ScheduleDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(schedule: ScheduleEntity): Long

    @Transaction
    @Query("SELECT * FROM schedule WHERE schedule_id = :scheduleId LIMIT 1")
    fun findById(scheduleId: Long): Flow<List<ScheduleEntityWithTagEntities>>

    @Transaction
    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete ORDER BY $RULE_ORDER_BY")
    fun findByComplete(isComplete: Boolean): Flow<List<ScheduleEntityWithTagEntities>>

    @Transaction
    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete ORDER BY $RULE_ORDER_BY")
    fun findByCompleteAsPagingSource(isComplete: Boolean): PagingSource<Int, ScheduleEntityWithTagEntities>

    @Query("UPDATE schedule SET is_complete = is_pending_complete WHERE is_complete <> is_pending_complete")
    suspend fun syncComplete()

    @Query("UPDATE schedule SET is_complete = :isComplete WHERE schedule_id = :scheduleId")
    suspend fun updateComplete(scheduleId: Long, isComplete: Boolean)

    @Query("UPDATE schedule SET is_pending_complete = :isPendingComplete WHERE schedule_id = :scheduleId")
    suspend fun updatePendingComplete(scheduleId: Long, isPendingComplete: Boolean)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    companion object {
        private const val RULE_ORDER_BY = "visible_priority"
    }
}