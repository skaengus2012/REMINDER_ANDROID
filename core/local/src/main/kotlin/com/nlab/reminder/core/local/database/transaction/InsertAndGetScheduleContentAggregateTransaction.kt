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
import com.nlab.reminder.core.local.database.util.TransactionScope
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
class InsertAndGetScheduleContentAggregateTransaction @Inject internal constructor(
    private val transactionScope: TransactionScope,
    private val scheduleTimingAggregateValidator: ScheduleTimingAggregateValidator,
    private val scheduleDAO: ScheduleDAO,
    private val insertAndGetScheduleExtra: InsertAndGetScheduleExtra,
) {
    suspend operator fun invoke(
        scheduleContentAggregate: ScheduleContentAggregate
    ): ScheduleContentAggregateSavedSnapshot {
        scheduleContentAggregate.timing?.run(scheduleTimingAggregateValidator::validate)
        return transactionScope.runIn {
            insertAndGetScheduleWithExtra(scheduleContentAggregate)
        }
    }

    private suspend fun insertAndGetScheduleWithExtra(
        scheduleContentAggregate: ScheduleContentAggregate
    ): ScheduleContentAggregateSavedSnapshot {
        val scheduleEntity = scheduleDAO.insertAndGet(
            headline = scheduleContentAggregate.headline,
            timing = scheduleContentAggregate.timing?.toScheduleTimingSaveInput()
        )
        val snapshot = insertAndGetScheduleExtra(
            scheduleId = scheduleEntity.scheduleId,
            tagIds = scheduleContentAggregate.tagIds,
            repeatDetailAggregates = scheduleContentAggregate.timing?.repeat?.details.orEmpty()
        )
        return ScheduleContentAggregateSavedSnapshot(
            scheduleEntity = scheduleEntity,
            repeatDetailEntities = snapshot.repeatDetailEntities,
            scheduleTagListEntities = snapshot.scheduleTagListEntities,
        )
    }
}