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
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.local.database.model.EMPTY_GENERATED_ID
import com.nlab.reminder.core.local.database.model.RepeatFrequency
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * @author Doohyun
 */
@Dao
abstract class ScheduleDAO {
    @Insert
    protected abstract suspend fun insert(entity: ScheduleEntity): Long

    @Update
    protected abstract suspend fun update(entity: ScheduleEntity)

    @Delete
    protected abstract suspend fun delete(entity: ScheduleEntity)

    @Query("SELECT * FROM schedule")
    abstract fun getAsStream(): Flow<Array<ScheduleEntity>>

    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete")
    abstract fun findByCompleteAsStream(isComplete: Boolean): Flow<Array<ScheduleEntity>>

    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete")
    protected abstract suspend fun findByComplete(isComplete: Boolean): Array<ScheduleEntity>

    @Query("SELECT schedule_id FROM schedule WHERE is_complete = :isComplete")
    protected abstract suspend fun findIdsByComplete(isComplete: Boolean): Array<Long>

    @Query("SELECT * FROM schedule WHERE schedule_id = :scheduleId")
    protected abstract suspend fun findById(scheduleId: Long): ScheduleEntity?

    @Query("SELECT * FROM schedule WHERE schedule_id IN (:scheduleIds)")
    protected abstract suspend fun findByScheduleIdsInternal(scheduleIds: Set<Long>): Array<ScheduleEntity>

    private suspend fun findByScheduleIds(scheduleIds: Set<Long>): Array<ScheduleEntity> =
        if (scheduleIds.isEmpty()) emptyArray()
        else findByScheduleIdsInternal(scheduleIds)

    @Query(
        """
        SELECT visible_priority
        FROM schedule 
        WHERE is_complete = :isComplete
        ORDER BY visible_priority DESC 
        LIMIT 1
        """
    )
    protected abstract fun findMaxVisiblePriorityByComplete(isComplete: Boolean): Long?

    private inline fun findMaxVisiblePriorityByCompleteOrElse(
        isComplete: Boolean,
        defaultValue: () -> Long
    ): Long = findMaxVisiblePriorityByComplete(isComplete) ?: defaultValue()

    @Query("UPDATE schedule SET visible_priority = :visiblePriority WHERE schedule_id = :scheduleId")
    protected abstract suspend fun updateVisiblePriorityInternal(scheduleId: Long, visiblePriority: Long)

    @Query("DELETE FROM schedule WHERE is_complete = :isComplete")
    protected abstract suspend fun deleteByCompleteInternal(isComplete: Boolean)

    @Transaction
    open suspend fun insertAndGet(contentDTO: ScheduleContentDTO): ScheduleEntity {
        val entity = ScheduleEntity(
            contentDTO = contentDTO,
            visiblePriority = (findMaxVisiblePriorityByCompleteOrElse(isComplete = false, defaultValue = { -1 }) + 1)
                .toNonNegativeLong(),
        )
        return checkNotNull(findById(scheduleId = insert(entity)))
    }

    @Transaction
    open suspend fun updateAndGet(scheduleId: Long, contentDTO: ScheduleContentDTO): ScheduleEntity {
        val oldEntity = checkNotNull(findById(scheduleId))
        if (contentDTO.equalsContent(oldEntity)) return oldEntity // No changes

        val newEntity = ScheduleEntity(oldEntity, contentDTO)
        update(newEntity)

        return newEntity
    }

    @Transaction
    open suspend fun updateByVisiblePriorities(
        idToVisiblePriorityTable: Map<Long, Long>,
        isCompletedRange: Boolean
    ) {
        check(idToVisiblePriorityTable.keys == findIdsByComplete(isCompletedRange).toSet())

        idToVisiblePriorityTable.forEach { (id, visiblePriority) ->
            updateVisiblePriorityInternal(scheduleId = id, visiblePriority = visiblePriority)
        }
    }

    @Transaction
    open suspend fun updateByCompletes(idToCompleteTable: Map<Long, Boolean>) {
        suspend fun updateByCompletesInternal(entities: Collection<ScheduleEntity>, isComplete: Boolean) {
            if (entities.isEmpty()) return
            val maxVisiblePriority = findMaxVisiblePriorityByCompleteOrElse(isComplete, defaultValue = { -1 })
            entities.sortedBy { it.visiblePriority }.forEachIndexed { index, entity ->
                insert(
                    entity = entity.copy(
                        scheduleId = EMPTY_GENERATED_ID,
                        isComplete = isComplete,
                        visiblePriority = maxVisiblePriority + index + 1
                    )
                )
            }
        }

        findByScheduleIds(scheduleIds = idToCompleteTable.keys)
            .filter { entity -> entity.isComplete != idToCompleteTable.getValue(entity.scheduleId) }
            .groupBy { it.isComplete }
            .forEach { (isComplete, entities) ->
                updateByCompletesInternal(entities = entities, isComplete = isComplete.not())
            }
    }

    @Transaction
    open suspend fun deleteByScheduleIds(scheduleIds: Set<Long>) {
        val entities = findByScheduleIds(scheduleIds = scheduleIds)
        if (entities.isEmpty()) return

        entities.forEach { delete(it) }

        if (entities.any { it.isComplete }) {
            correctVisiblePriorityWithComplete(isComplete = true)
        }
        if (entities.any { it.isComplete.not() }) {
            correctVisiblePriorityWithComplete(isComplete = false)
        }
    }

    @Transaction
    open suspend fun deleteByComplete(isComplete: Boolean) {
        deleteByCompleteInternal(isComplete)
        correctVisiblePriorityWithComplete(isComplete)
    }

    private suspend fun correctVisiblePriorityWithComplete(isComplete: Boolean) {
        val entities = findByComplete(isComplete)
        if (entities.isEmpty()) return

        entities.sortedBy { it.visiblePriority }.forEachIndexed { index, entity ->
            update(entity.copy(visiblePriority = index.toLong()))
        }
    }
}

data class ScheduleContentDTO(
    val title: NonBlankString,
    val description: NonBlankString?,
    val link: NonBlankString?,
    val triggerTimeDTO: TriggerTimeDTO?,
    val frequencyDTO: RepeatFrequencyDTO?
)

data class TriggerTimeDTO(
    val utcTime: Instant,
    val isDateOnly: Boolean
)

data class RepeatFrequencyDTO(
    @RepeatFrequency val code: String,
    val value: Long
)

private fun ScheduleContentDTO.equalsContent(entity: ScheduleEntity): Boolean =
    title.value == entity.title
            && description?.value == entity.description
            && link?.value == entity.link
            && triggerTimeDTO?.utcTime == entity.triggerTimeUtc
            && triggerTimeDTO?.isDateOnly == entity.isTriggerTimeDateOnly

private fun ScheduleEntity(
    contentDTO: ScheduleContentDTO,
    visiblePriority: NonNegativeLong,
): ScheduleEntity = ScheduleEntity(
    title = contentDTO.title.value,
    description = contentDTO.description?.value,
    link = contentDTO.link?.value,
    triggerTimeUtc = contentDTO.triggerTimeDTO?.utcTime,
    isTriggerTimeDateOnly = contentDTO.triggerTimeDTO?.isDateOnly,
    repeatFrequency = contentDTO.frequencyDTO?.code,
    repeatFrequencyValue = contentDTO.frequencyDTO?.value,
    visiblePriority = visiblePriority.value,
    isComplete = false
)

private fun ScheduleEntity(
    baseEntity: ScheduleEntity,
    contentDTO: ScheduleContentDTO
): ScheduleEntity = baseEntity.copy(
    title = contentDTO.title.value,
    description = contentDTO.description?.value,
    link = contentDTO.link?.value,
    triggerTimeUtc = contentDTO.triggerTimeDTO?.utcTime,
    isTriggerTimeDateOnly = contentDTO.triggerTimeDTO?.isDateOnly
)