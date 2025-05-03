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

package com.nlab.reminder.core.local.database.transaction

import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.model.TagEntity
import com.nlab.reminder.core.local.database.util.TransactionScope
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
class UpdateOrMergeAndGetTagTransaction @Inject internal constructor(
    private val transactionScope: TransactionScope,
    private val tagDAO: TagDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO
) {
    suspend operator fun invoke(
        tagId: Long,
        name: NonBlankString
    ): TagEntity = transactionScope.runIn {
        val tagEntity = tagDAO.findByName(name.value)
        return@runIn when {
            tagEntity == null -> {
                // case1 : not conflict name, just update
                tagDAO.updateAndGet(tagId, name)
            }

            tagEntity.tagId == tagId -> {
                // case2 : request same name
                tagEntity
            }

            else -> {
                // case3 : conflict name, merge
                copyScheduleIds(fromTagId = tagId, toTagId = tagEntity.tagId)
                tagDAO.deleteById(tagId)
                tagEntity
            }

        }
    }

    private suspend fun copyScheduleIds(fromTagId: Long, toTagId: Long) {
        val fromScheduleIds = scheduleTagListDAO.findScheduleIdsByTagId(fromTagId).toSet()
        val toScheduleIds = scheduleTagListDAO.findScheduleIdsByTagId(toTagId).toSet()
        val targetScheduleIds = fromScheduleIds - toScheduleIds
        scheduleTagListDAO.insert(
            entities = targetScheduleIds.toSet { scheduleId -> ScheduleTagListEntity(scheduleId, toTagId) }
        )
    }
}