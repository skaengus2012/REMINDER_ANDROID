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
import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.local.database.entity.ScheduleEntity
import com.nlab.reminder.core.local.database.entity.EMPTY_GENERATED_ID
import com.nlab.reminder.core.local.database.entity.RepeatType
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

    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete")
    protected abstract suspend fun findByComplete(isComplete: Boolean): List<ScheduleEntity>

    @Query("SELECT schedule_id FROM schedule WHERE is_complete = :isComplete")
    protected abstract suspend fun findIdsByComplete(isComplete: Boolean): List<Long>

    @Query("SELECT * FROM schedule WHERE schedule_id = :scheduleId")
    protected abstract suspend fun findById(scheduleId: Long): ScheduleEntity?

    @Query("SELECT * FROM schedule WHERE schedule_id IN (:scheduleIds)")
    protected abstract suspend fun findByIdsInternal(scheduleIds: Set<Long>): List<ScheduleEntity>

    private suspend fun findByIds(scheduleIds: Set<Long>): List<ScheduleEntity> =
        if (scheduleIds.isEmpty()) emptyList()
        else findByIdsInternal(scheduleIds)

    @Query(
        """
        SELECT visible_priority
        FROM schedule 
        WHERE is_complete = :isComplete
        ORDER BY visible_priority DESC 
        LIMIT 1
        """
    )
    protected abstract suspend fun findMaxVisiblePriorityByComplete(isComplete: Boolean): Long?

    private suspend inline fun findMaxVisiblePriorityByCompleteOrElse(
        isComplete: Boolean,
        defaultValue: () -> Long
    ): Long = findMaxVisiblePriorityByComplete(isComplete) ?: defaultValue()

    @Query("UPDATE schedule SET visible_priority = :visiblePriority WHERE schedule_id = :scheduleId")
    protected abstract suspend fun updateVisiblePriorityInternal(scheduleId: Long, visiblePriority: Long)

    @Query("DELETE FROM schedule WHERE is_complete = :isComplete")
    protected abstract suspend fun deleteByCompleteInternal(isComplete: Boolean)

    @Transaction
    open suspend fun insertAndGet(
        headline: ScheduleHeadlineSaveInput,
        timing: ScheduleTimingSaveInput?
    ): ScheduleEntity {
        val currentMaxVisiblePriority = findMaxVisiblePriorityByCompleteOrElse(
            isComplete = false,
            defaultValue = { -1 }
        )
        val entity = ScheduleEntity(
            title = headline.title.value,
            description = headline.description?.value,
            link = headline.link?.value,
            triggerTimeUtc = timing?.triggerTimeUtc,
            isTriggerTimeDateOnly = timing?.isTriggerTimeDateOnly,
            repeatType = timing?.repeatInput?.type,
            repeatInterval = timing?.repeatInput?.interval?.value,
            visiblePriority = currentMaxVisiblePriority + 1,
            isComplete = false
        )
        return checkNotNull(findById(scheduleId = insert(entity)))
    }

    @Transaction
    open suspend fun updateAndGet(scheduleId: Long, headline: ScheduleHeadlineSaveInput): ScheduleEntity {
        val oldEntity = checkNotNull(findById(scheduleId))
        if (oldEntity.contentEquals(headline)) return oldEntity // No changes

        val newEntity = oldEntity.copy(
            title = headline.title.value,
            description = headline.description?.value,
            link = headline.link?.value,
        )
        update(newEntity)

        return newEntity
    }

    @Transaction
    open suspend fun updateAndGet(
        scheduleId: Long,
        headline: ScheduleHeadlineSaveInput,
        timing: ScheduleTimingSaveInput?
    ): ScheduleEntity {
        val oldEntity = checkNotNull(findById(scheduleId))
        if (oldEntity.contentEquals(headline) && oldEntity.contentEquals(timing)) return oldEntity // No changes

        val newEntity = oldEntity.copy(
            title = headline.title.value,
            description = headline.description?.value,
            link = headline.link?.value,
            triggerTimeUtc = timing?.triggerTimeUtc,
            isTriggerTimeDateOnly = timing?.isTriggerTimeDateOnly,
            repeatType = timing?.repeatInput?.type,
            repeatInterval = timing?.repeatInput?.interval?.value,
        )
        update(newEntity)

        return newEntity
    }

    @Transaction
    open suspend fun updateByVisiblePriorities(idToVisiblePriorityTable: Map<Long, NonNegativeLong>) {
        if (idToVisiblePriorityTable.isEmpty()) return
        val sampleSchedule = findById(scheduleId = idToVisiblePriorityTable.firstNotNullOf { it.key })
        checkNotNull(sampleSchedule)
        check(idToVisiblePriorityTable.keys == findIdsByComplete(sampleSchedule.isComplete).toSet())

        idToVisiblePriorityTable.forEach { (id, visiblePriority) ->
            updateVisiblePriorityInternal(scheduleId = id, visiblePriority = visiblePriority.value)
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

        val entities = findByIds(scheduleIds = idToCompleteTable.keys)
        if (entities.isEmpty()) return

        entities
            .filter { entity -> entity.isComplete != idToCompleteTable.getValue(entity.scheduleId) }
            .groupBy { it.isComplete }
            .forEach { (isComplete, entities) ->
                updateByCompletesInternal(entities = entities, isComplete = isComplete.not())
            }
    }

    @Transaction
    open suspend fun deleteByScheduleIds(scheduleIds: Set<Long>) {
        val entities = findByIds(scheduleIds = scheduleIds)
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

data class ScheduleHeadlineSaveInput(
    val title: NonBlankString,
    val description: NonBlankString?,
    val link: NonBlankString?,
)

data class ScheduleTimingSaveInput(
    val triggerTimeUtc: Instant,
    val isTriggerTimeDateOnly: Boolean,
    val repeatInput: ScheduleRepeatSaveInput?
)

data class ScheduleRepeatSaveInput(
    @RepeatType val type: String,
    val interval: PositiveInt,
)

private fun ScheduleEntity.contentEquals(headline: ScheduleHeadlineSaveInput): Boolean =
    title == headline.title.value
            && description == headline.description?.value
            && link == headline.link?.value

private fun ScheduleEntity.contentEquals(timing: ScheduleTimingSaveInput?): Boolean =
    triggerTimeUtc == timing?.triggerTimeUtc
            && isTriggerTimeDateOnly == timing?.isTriggerTimeDateOnly
            && repeatType == timing?.repeatInput?.type
            && repeatInterval == timing?.repeatInput?.interval?.value