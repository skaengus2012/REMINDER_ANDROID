/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

import androidx.room.*
import com.nlab.reminder.core.local.database.entity.ScheduleDeletionBacklogEntity

/**
 * @author Doohyun
 */
@Dao
abstract class ScheduleDeletionBacklogDAO {
    @Insert
    protected abstract suspend fun insertAllInternal(
        entities: List<ScheduleDeletionBacklogEntity>
    ): List<Long>

    suspend fun insertAll(scheduleIds: Set<Long>) {
        val entities = scheduleIds.map { ScheduleDeletionBacklogEntity(scheduleId = it) }
        insertAllInternal(entities)
    }

    @Query("SELECT * FROM schedule_deletion_backlog")
    abstract suspend fun getAll(): List<ScheduleDeletionBacklogEntity>
}