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

import com.nlab.reminder.core.local.database.dao.RepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.model.RepeatDetailContentDTO
import com.nlab.reminder.core.local.database.model.ScheduleContentDTO
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.util.TransactionScope
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
class UpdateAndGetScheduleWithExtraTransaction @Inject internal constructor(
    private val transactionScope: TransactionScope,
    private val scheduleDAO: ScheduleDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val repeatDetailDAO: RepeatDetailDAO
) {
    private val insertScheduleExtra = InsertScheduleExtra(
        scheduleTagListDAO = scheduleTagListDAO,
        repeatDetailDAO = repeatDetailDAO
    )

    suspend operator fun invoke(
        scheduleId: Long,
        contentDTO: ScheduleContentDTO,
        tagIds: Set<Long>,
        repeatDetailContentDTOs: Set<RepeatDetailContentDTO>
    ): UpdateScheduleWithExtraResult = transactionScope.runIn {
        val scheduleEntity = scheduleDAO.updateAndGet(scheduleId, contentDTO)
        // replace extra
        // rm extra
        scheduleTagListDAO.deleteByScheduleId(scheduleId)
        repeatDetailDAO.deleteByScheduleId(scheduleId)
        // add extra
        insertScheduleExtra.insert(
            scheduleId,
            tagIds,
            repeatDetailContentDTOs
        )
        return@runIn UpdateScheduleWithExtraResult(
            scheduleEntity = scheduleEntity,
            tagIds = tagIds,
            repeatDetailContentDTOs = repeatDetailContentDTOs
        )
    }
}

data class UpdateScheduleWithExtraResult(
    val scheduleEntity: ScheduleEntity,
    val tagIds: Set<Long>,
    val repeatDetailContentDTOs: Set<RepeatDetailContentDTO>
)