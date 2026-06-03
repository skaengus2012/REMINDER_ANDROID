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
    protected abstract suspend fun insertAll(
        entities: List<ScheduleDeletionBacklogEntity>
    ): List<Long>

    open suspend fun insertAllAndGet(scheduleIds: Set<Long>): List<ScheduleDeletionBacklogEntity> {
        val inputEntities = scheduleIds.map { ScheduleDeletionBacklogEntity(scheduleId = it) }
        val outputIds = insertAll(inputEntities)
        return inputEntities.zip(outputIds) { inputEntity, outputId ->
            inputEntity.copy(backlogId = outputId)
        }
    }

    @Query("SELECT * FROM schedule_deletion_backlog")
    abstract suspend fun getAll(): List<ScheduleDeletionBacklogEntity>

    @Query("DELETE FROM schedule_deletion_backlog WHERE schedule_deletion_backlog_id IN (:backlogIds)")
    protected abstract suspend fun deleteByIdsInternal(backlogIds: Set<Long>)

    suspend fun deleteByIds(backlogIds: Set<Long>) {
        if (backlogIds.isEmpty()) return
        deleteByIdsInternal(backlogIds)
    }
}
