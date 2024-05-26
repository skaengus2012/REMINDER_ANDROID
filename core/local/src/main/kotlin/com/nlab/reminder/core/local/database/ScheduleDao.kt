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

package com.nlab.reminder.core.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
@Dao
abstract class ScheduleDao {
    @Insert
    abstract suspend fun insert(schedule: ScheduleEntity): Long

    @Update
    abstract suspend fun update(schedule: ScheduleEntity)

    @Transaction
    @Query("SELECT * FROM schedule ORDER BY is_complete, visible_priority")
    abstract fun findAsStream(): Flow<List<ScheduleEntityWithTagEntities>>

    @Transaction
    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete ORDER BY is_complete, visible_priority")
    abstract fun findByCompleteAsStream(isComplete: Boolean): Flow<List<ScheduleEntityWithTagEntities>>

    @Query("SELECT * FROM schedule WHERE schedule_id IN (:scheduleIds)")
    abstract suspend fun findByScheduleIds(scheduleIds: Collection<Long>): List<ScheduleEntity>

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
    open suspend fun updateCompletes(idToCompleteTable: Map<Long, Boolean>) {
        val completeToEntities: Map<Boolean, List<ScheduleEntity>> =
            findByScheduleIds(scheduleIds = idToCompleteTable.keys)
                .filter { entity -> entity.isComplete != idToCompleteTable.getValue(entity.scheduleId) }
                .groupBy { it.isComplete }
        updateCompletesInternal(completeToEntities[true], targetComplete = false)
        updateCompletesInternal(completeToEntities[false], targetComplete = true)
    }

    private suspend fun updateCompletesInternal(entities: List<ScheduleEntity>?, targetComplete: Boolean) {
        if (entities == null) return
        if (entities.isEmpty()) return

        val maxVisiblePriority: Long = getMaxVisiblePriority(targetComplete) ?: -1
        entities.sortedBy { it.visiblePriority }.forEachIndexed { index, entity ->
            update(
                entity.copy(
                    isComplete = targetComplete,
                    visiblePriority = maxVisiblePriority + index + 1
                )
            )
        }
    }

    @Transaction
    open suspend fun updateVisiblePriorities(idToVisiblePriorityTable: Map<Long, Long>) {
        idToVisiblePriorityTable
            .forEach { (scheduleId, visiblePriority) -> updateVisiblePriority(scheduleId, visiblePriority) }
    }

    // deprecate zone.
    @Query(
        """
        UPDATE schedule 
        SET is_complete = :isComplete, visible_priority = :visiblePriority 
        WHERE schedule_id = :scheduleId
        """
    )
    abstract suspend fun updateComplete(scheduleId: Long, isComplete: Boolean, visiblePriority: Long)

    private suspend fun updateCompletesWithIds(scheduleIds: List<Long>, isComplete: Boolean) {
        if (scheduleIds.isEmpty()) return

        val maxVisiblePriority: Long = getMaxVisiblePriority(isComplete) ?: -1
        scheduleIds.forEachIndexed { index, scheduleId ->
            updateComplete(scheduleId, isComplete, visiblePriority = maxVisiblePriority + index + 1)
        }
    }

    @Transaction
    open suspend fun updateCompletesLegacy(requests: List<Pair<Long, Boolean>>) {
        val curIdToComplete: Map<Long, Boolean> =
            findByScheduleIds(scheduleIds = requests.map { (first) -> first }).associateBy(
                keySelector = { it.scheduleId },
                valueTransform = { it.isComplete }
            )
        val diffCompleteRequests: List<Pair<Long, Boolean>> =
            requests
                .filter { (scheduleId) -> curIdToComplete.containsKey(scheduleId) }
                .filter { (scheduleId, isComplete) -> curIdToComplete[scheduleId] != isComplete }
        updateCompletesInternalLegacy(diffCompleteRequests, isComplete = true)
        updateCompletesInternalLegacy(diffCompleteRequests, isComplete = false)
    }

    private suspend fun updateCompletesInternalLegacy(requests: List<Pair<Long, Boolean>>, isComplete: Boolean) {
        updateCompletesWithIds(
            scheduleIds = requests
                .filter { (_, mappingComplete) -> mappingComplete == isComplete }
                .map { (scheduleId) -> scheduleId },
            isComplete = isComplete
        )
    }

    @Transaction
    open suspend fun updateCompletesLegacy(scheduleIds: List<Long>, isComplete: Boolean) {
        updateCompletesWithIds(
            scheduleIds = findByScheduleIds(scheduleIds)
                .filter { it.isComplete != isComplete }
                .map { it.scheduleId },
            isComplete
        )
    }

    @Delete
    abstract suspend fun delete(schedule: ScheduleEntity)

    @Query("DELETE FROM schedule WHERE schedule_id IN (:scheduleId)")
    abstract suspend fun deleteByScheduleIds(scheduleId: List<Long>)

    @Query("DELETE FROM schedule WHERE is_complete = :isComplete")
    abstract suspend fun deleteByComplete(isComplete: Boolean)
}