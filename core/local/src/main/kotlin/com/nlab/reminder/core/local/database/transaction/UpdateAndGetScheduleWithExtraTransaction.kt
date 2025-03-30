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
import com.nlab.reminder.core.local.database.model.ScheduleContentDTO
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity
import com.nlab.reminder.core.local.database.util.TransactionScope
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
class UpdateAndGetScheduleWithExtraTransaction @Inject internal constructor(
    private val transactionScope: TransactionScope,
    private val scheduleTransactionValidator: ScheduleTransactionValidator,
    private val scheduleDAO: ScheduleDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val repeatDetailDAO: RepeatDetailDAO,
    private val insertAndGetScheduleExtra: InsertAndGetScheduleExtra
) {
    suspend operator fun invoke(
        scheduleId: Long,
        contentDTO: ScheduleContentDTO,
        tagIds: Set<Long>
    ): UpdateScheduleWithExtraResult {
        scheduleTransactionValidator.validate(contentDTO)
        return transactionScope.runIn {
            updateAndGetScheduleWithExtra(scheduleId, contentDTO, tagIds)
        }
    }

    private suspend fun updateAndGetScheduleWithExtra(
        scheduleId: Long,
        contentDTO: ScheduleContentDTO,
        tagIds: Set<Long>
    ): UpdateScheduleWithExtraResult {
        clearScheduleExtra(scheduleId)
        val scheduleEntity = scheduleDAO.updateAndGet(scheduleId, contentDTO)
        val result = insertAndGetScheduleExtra(
            scheduleId,
            tagIds,
            contentDTO.repeatDTO?.details.orEmpty()
        )
        return UpdateScheduleWithExtraResult(
            scheduleWithDetailsEntity = ScheduleWithDetailsEntity(
                schedule = scheduleEntity,
                repeatDetails = result.repeatDetailEntities
            ),
            scheduleTagListEntities = result.scheduleTagListEntities
        )
    }

    private suspend fun clearScheduleExtra(scheduleId: Long) {
        scheduleTagListDAO.deleteByScheduleId(scheduleId)
        repeatDetailDAO.deleteByScheduleId(scheduleId)
    }
}

data class UpdateScheduleWithExtraResult(
    val scheduleWithDetailsEntity: ScheduleWithDetailsEntity,
    val scheduleTagListEntities: Set<ScheduleTagListEntity>
)