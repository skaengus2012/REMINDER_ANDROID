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

package com.nlab.reminder.core.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Dao
abstract class ScheduleTagListDAO {
    @Insert
    abstract suspend fun insert(entities: Set<ScheduleTagListEntity>)

    @Query("SELECT DISTINCT tag_id FROM schedule_tag_list")
    abstract suspend fun getTagIds(): Array<Long>

    @Query("SELECT COUNT(schedule_id) FROM schedule_tag_list WHERE tag_id = :tagId")
    abstract suspend fun findTagUsageCount(tagId: Long): Long

    @Query("SELECT schedule_id FROM schedule_tag_list WHERE tag_id = :tagId")
    abstract suspend fun findScheduleIdsByTagId(tagId: Long): Array<Long>

    @Query("SELECT * FROM schedule_tag_list WHERE tag_id IN (:scheduleIds)")
    protected abstract fun findByScheduleIdsAsStreamInternal(scheduleIds: Set<Long>): Flow<Array<ScheduleTagListEntity>>

    fun findByScheduleIdsAsStream(scheduleIds: Set<Long>): Flow<Array<ScheduleTagListEntity>> =
        if (scheduleIds.isEmpty()) flowOf(emptyArray())
        else findByScheduleIdsAsStreamInternal(scheduleIds)

    @Query("DELETE FROM schedule_tag_list WHERE schedule_id = :scheduleId")
    abstract fun deleteByScheduleId(scheduleId: Long)
}