/*
 * Copyright (C) 2025 The N's lab Open Source Project
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
import com.nlab.reminder.core.local.database.entity.RepeatDetailEntity

/**
 * @author Thalys
 */
@Dao
abstract class RepeatDetailDAO {
    @Insert
    internal abstract suspend fun insert(entities: List<RepeatDetailEntity>)

    @Query("SELECT * FROM repeat_detail WHERE schedule_id = :scheduleId")
    internal abstract suspend fun findByScheduleId(scheduleId: Long): List<RepeatDetailEntity>

    @Query("DELETE FROM repeat_detail WHERE schedule_id = :scheduleId")
    internal abstract suspend fun deleteByScheduleId(scheduleId: Long)
}