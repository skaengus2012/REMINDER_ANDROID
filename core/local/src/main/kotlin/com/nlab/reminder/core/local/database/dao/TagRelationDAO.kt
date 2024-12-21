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

import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.model.TagEntity
import com.nlab.reminder.core.local.database.util.TransactionScope

/**
 * @author Doohyun
 */
class TagRelationDAO(
    private val transactionScope: TransactionScope,
    private val tagDAO: TagDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO
) {
    suspend fun updateOrReplaceAndGet(
        tagId: Long,
        name: String
    ): TagEntity {
        val tagEntity = tagDAO.findByName(name)
        return when {
            tagEntity == null -> {
                // case1 : not conflict name, just update
                tagDAO.updateAndGet(tagId, name)
            }

            tagEntity.tagId == tagId -> {
                // case2 : request same name
                TagEntity(tagId = tagId, name = name)
            }

            else -> transactionScope {
                // case3 : conflict name, replace
                scheduleTagListDAO.copyScheduleIds(fromTagId = tagId, toTagId = tagEntity.tagId)
                tagDAO.deleteById(tagId)
                tagEntity
            }
        }
    }
}

private suspend fun ScheduleTagListDAO.copyScheduleIds(fromTagId: Long, toTagId: Long) {
    val fromScheduleIds = findScheduleIdsByTagId(fromTagId).toSet()
    val toScheduleIds = findScheduleIdsByTagId(toTagId).toSet()
    val targetScheduleIds = fromScheduleIds - toScheduleIds
    targetScheduleIds.forEach { scheduleId ->
        insert(ScheduleTagListEntity(scheduleId = scheduleId, tagId = toTagId))
    }
}