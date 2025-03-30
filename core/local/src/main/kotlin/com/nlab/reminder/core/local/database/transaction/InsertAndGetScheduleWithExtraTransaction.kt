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

import com.nlab.reminder.core.local.database.dao.ScheduleDAO
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
class InsertAndGetScheduleWithExtraTransaction @Inject internal constructor(
    private val transactionScope: TransactionScope,
    private val scheduleTransactionValidator: ScheduleTransactionValidator,
    private val scheduleDAO: ScheduleDAO,
    private val insertAndGetScheduleExtra: InsertAndGetScheduleExtra,
) {
    suspend operator fun invoke(
        contentDTO: ScheduleContentDTO,
        tagIds: Set<Long>,
    ): InsertScheduleWithExtraResult {
        scheduleTransactionValidator.validate(contentDTO)
        return transactionScope.runIn { insertAndGetScheduleWithExtra(contentDTO, tagIds) }
    }

    private suspend fun insertAndGetScheduleWithExtra(
        contentDTO: ScheduleContentDTO,
        tagIds: Set<Long>
    ): InsertScheduleWithExtraResult {
        val scheduleEntity = scheduleDAO.insertAndGet(contentDTO)
        val result = insertAndGetScheduleExtra(
            scheduleEntity.scheduleId,
            tagIds,
            contentDTO.repeatDTO?.details.orEmpty()
        )
        return InsertScheduleWithExtraResult(
            scheduleWithDetailsEntity = ScheduleWithDetailsEntity(
                schedule = scheduleEntity,
                repeatDetails = result.repeatDetailEntities,
            ),
            scheduleTagListEntities = result.scheduleTagListEntities,
        )
    }
}

data class InsertScheduleWithExtraResult(
    val scheduleWithDetailsEntity: ScheduleWithDetailsEntity,
    val scheduleTagListEntities: Set<ScheduleTagListEntity>
)