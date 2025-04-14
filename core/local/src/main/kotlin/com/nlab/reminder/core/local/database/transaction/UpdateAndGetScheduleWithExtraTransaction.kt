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
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleContentDTO
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.util.TransactionScope
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
class UpdateAndGetScheduleWithExtraTransaction @Inject internal constructor(
    private val transactionScope: TransactionScope,
    private val scheduleContentValidator: ScheduleContentValidator,
    private val scheduleDAO: ScheduleDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val repeatDetailDAO: RepeatDetailDAO,
    private val insertAndGetScheduleExtra: InsertAndGetScheduleExtra
) {
    suspend operator fun invoke(scheduleId: Long, contentDTO: ScheduleContentDTO): UpdateScheduleWithExtraResult {
        scheduleContentValidator.validate(contentDTO)
        return transactionScope.runIn {
            updateAndGetScheduleWithExtra(scheduleId, contentDTO)
        }
    }

    private suspend fun updateAndGetScheduleWithExtra(
        scheduleId: Long,
        contentDTO: ScheduleContentDTO
    ): UpdateScheduleWithExtraResult {
        clearScheduleExtra(scheduleId)
        val scheduleEntity = scheduleDAO.updateAndGet(scheduleId, contentDTO)
        val result = insertAndGetScheduleExtra(
            scheduleId = scheduleId,
            tagIds = contentDTO.tagIds,
            repeatDetailDTOs = contentDTO.timingDTO?.repeatDTO?.details.orEmpty()
        )
        return UpdateScheduleWithExtraResult(
            scheduleEntity = scheduleEntity,
            repeatDetailEntities = result.repeatDetailEntities,
            scheduleTagListEntities = result.scheduleTagListEntities
        )
    }

    private suspend fun clearScheduleExtra(scheduleId: Long) {
        scheduleTagListDAO.deleteByScheduleId(scheduleId)
        repeatDetailDAO.deleteByScheduleId(scheduleId)
    }
}

data class UpdateScheduleWithExtraResult(
    val scheduleEntity: ScheduleEntity,
    val repeatDetailEntities: Set<RepeatDetailEntity>,
    val scheduleTagListEntities: Set<ScheduleTagListEntity>
)