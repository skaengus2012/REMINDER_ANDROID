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
import androidx.room.Transaction
import com.nlab.reminder.core.local.database.entity.ScheduleCompletionBacklogEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
@Dao
abstract class ScheduleCompletionBacklogDAO {
    @Insert
    protected abstract suspend fun insert(entity: ScheduleCompletionBacklogEntity): Long

    @Transaction
    open suspend fun insertAndGet(
        scheduleId: Long,
        targetCompleted: Boolean
    ): ScheduleCompletionBacklogEntity {
        val entity = ScheduleCompletionBacklogEntity(
            scheduleId = scheduleId,
            targetCompleted = targetCompleted,
            insertOrder = findLatest()?.insertOrder?.let { it + 1 } ?: 1
        )
        return entity.copy(backlogId = insert(entity))
    }

    @Query("SELECT * FROM schedule_completion_backlog")
    abstract suspend fun getAll(): List<ScheduleCompletionBacklogEntity>

    @Query(
        """
        SELECT * 
        FROM schedule_completion_backlog
        WHERE schedule_completion_backlog.schedule_id IN (
            SELECT schedule_id
            FROM schedule_completion_backlog
            WHERE schedule_completion_backlog.insert_order <= :insertOrder
        )
        """
    )
    abstract suspend fun findAllByScheduleIdsUpToInsertOrder(
        insertOrder: Long
    ): List<ScheduleCompletionBacklogEntity>

    @Query("SELECT * FROM schedule_completion_backlog ORDER BY insert_order DESC LIMIT 1")
    protected abstract suspend fun findLatest(): ScheduleCompletionBacklogEntity?

    @Query(
        """
        SELECT *
        FROM schedule_completion_backlog
        WHERE (schedule_completion_backlog.schedule_id, schedule_completion_backlog.insert_order) IN (
            SELECT 
                schedule_id, 
                MAX(insert_order)
            FROM schedule_completion_backlog
            GROUP BY schedule_id
        )"""
    )
    abstract fun getLatestPerScheduleIdAsStream(): Flow<List<ScheduleCompletionBacklogEntity>>

    @Query(
        """
        DELETE 
        FROM schedule_completion_backlog 
        WHERE schedule_completion_backlog_id IN (:scheduleCompletionBacklogIds)
        """
    )
    protected abstract suspend fun deleteByIdsInternal(scheduleCompletionBacklogIds: Set<Long>)

    suspend fun deleteByIds(scheduleCompletionBacklogIds: Set<Long>) {
        if (scheduleCompletionBacklogIds.isEmpty()) {
            return
        }
        deleteByIdsInternal(scheduleCompletionBacklogIds)
    }
}