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
import androidx.room.Query
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
@Dao
abstract class ScheduleRepeatDetailDAO {
    @Query(
        """
            SELECT *
            FROM schedule
            LEFT OUTER JOIN repeat_detail 
                ON schedule.schedule_id = repeat_detail.schedule_id
        """
    )
    abstract fun getAsStream(): Flow<Map<ScheduleEntity, Set<RepeatDetailEntity>>>

    @Query(
        """
            SELECT *
            FROM schedule
            LEFT OUTER JOIN repeat_detail 
                ON schedule.schedule_id = repeat_detail.schedule_id
            WHERE schedule.is_complete = :isComplete
        """
    )
    abstract fun findByCompleteAsStream(isComplete: Boolean): Flow<Map<ScheduleEntity, Set<RepeatDetailEntity>>>
}