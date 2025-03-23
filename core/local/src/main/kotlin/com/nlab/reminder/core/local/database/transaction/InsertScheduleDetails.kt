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

import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.local.database.dao.RepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.model.RepeatDetailContentDTO
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
internal class InsertScheduleDetails @Inject constructor(
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val repeatDetailDAO: RepeatDetailDAO,
) {
    suspend fun insert(
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