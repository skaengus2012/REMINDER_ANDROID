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

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.local.database.model.RepeatDetailContentDTO
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleContentDTO
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity

/**
 * @author Thalys
 */
class ScheduleRelationDAO(
    private val database: RoomDatabase,
    private val scheduleDAO: ScheduleDAO,
    private val tagDAO: TagDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val repeatDetailDAO: RepeatDetailDAO
) {
    suspend fun insertAndGet(
        contentDTO: ScheduleContentDTO,
        tagIds: Set<Long>,
        repeatDetailContentDTOs: Set<RepeatDetailContentDTO>
    ): ScheduleEntity = database.withTransaction {
        val scheduleEntity = scheduleDAO.insertAndGet(contentDTO)
        insertExtras(
            scheduleId = scheduleEntity.scheduleId,
            tagIds = tagIds,
            repeatDetailContentDTOs = repeatDetailContentDTOs
        )
        return@withTransaction scheduleEntity
    }

    suspend fun updateAndGet(
        scheduleId: Long,
        contentDTO: ScheduleContentDTO,
        tagIds: Set<Long>,
        repeatDetailContentDTOs: Set<RepeatDetailContentDTO>
    ): ScheduleEntity = database.withTransaction {
        val scheduleEntity = scheduleDAO.updateAndGet(scheduleId, contentDTO)
        scheduleTagListDAO.deleteByScheduleId(scheduleId)

        return@withTransaction scheduleEntity
    }

    private suspend fun insertExtras(
        scheduleId: Long,
        tagIds: Set<Long>,
        repeatDetailContentDTOs: Set<RepeatDetailContentDTO>
    ) {
        scheduleTagListDAO.insert(
            entities = tagIds.toSet { tagId ->
                ScheduleTagListEntity(scheduleId = scheduleId, tagId = tagId)
            }
        )
        repeatDetailDAO.insert(
            entities = repeatDetailContentDTOs.toSet { dto ->
                RepeatDetailEntity(
                    scheduleId = scheduleId,
                    frequencySetting = dto.frequencySetting,
                    value = dto.value
                )
            }
        )
    }
}