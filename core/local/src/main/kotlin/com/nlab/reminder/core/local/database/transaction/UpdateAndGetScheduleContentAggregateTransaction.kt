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
import com.nlab.reminder.core.local.database.util.TransactionScope
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
class UpdateAndGetScheduleContentAggregateTransaction @Inject internal constructor(
    private val transactionScope: TransactionScope,
    private val scheduleTimingAggregateValidator: ScheduleTimingAggregateValidator,
    private val scheduleDAO: ScheduleDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val repeatDetailDAO: RepeatDetailDAO,
    private val insertAndGetScheduleExtra: InsertAndGetScheduleExtra
) {
    suspend operator fun invoke(
        scheduleId: Long,
        scheduleContentAggregate: ScheduleContentAggregate
    ): ScheduleContentAggregateSavedSnapshot {
        scheduleContentAggregate.timing?.run(scheduleTimingAggregateValidator::validate)
        return transactionScope.runIn {
            updateAndGetScheduleWithExtra(scheduleId, scheduleContentAggregate)
        }
    }

    private suspend fun updateAndGetScheduleWithExtra(
        scheduleId: Long,
        scheduleContentAggregate: ScheduleContentAggregate
    ): ScheduleContentAggregateSavedSnapshot {
        clearScheduleExtra(scheduleId)
        val scheduleEntity = scheduleDAO.updateAndGet(
            scheduleId,
            headline = scheduleContentAggregate.headline,
            timing = scheduleContentAggregate.timing?.toScheduleTimingSaveInput()
        )
        val snapshot = insertAndGetScheduleExtra(
            scheduleId = scheduleId,
            tagIds = scheduleContentAggregate.tagIds,
            repeatDetailAggregates = scheduleContentAggregate.timing?.repeat?.details.orEmpty()
        )
        return ScheduleContentAggregateSavedSnapshot(
            scheduleEntity = scheduleEntity,
            repeatDetailEntities = snapshot.repeatDetailEntities,
            scheduleTagListEntities = snapshot.scheduleTagListEntities
        )
    }

    private suspend fun clearScheduleExtra(scheduleId: Long) {
        scheduleTagListDAO.deleteByScheduleId(scheduleId)
        repeatDetailDAO.deleteByScheduleId(scheduleId)
    }
}