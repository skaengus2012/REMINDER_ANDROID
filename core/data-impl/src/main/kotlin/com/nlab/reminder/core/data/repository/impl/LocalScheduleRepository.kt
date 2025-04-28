/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.toDTO
import com.nlab.reminder.core.data.repository.DeleteScheduleQuery
import com.nlab.reminder.core.data.repository.GetScheduleCountQuery
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.UpdateAllScheduleQuery
import com.nlab.reminder.core.data.repository.fake.FakeScheduleRepositoryDelegate
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.tryToNonNegativeLongOrZero
import com.nlab.reminder.core.kotlinx.coroutine.flow.flatMapLatest
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleRepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.transaction.InsertAndGetScheduleWithExtraTransaction
import com.nlab.reminder.core.local.database.transaction.UpdateAndGetScheduleWithExtraTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * @author Doohyun
 */
class LocalScheduleRepository(
    private val scheduleDAO: ScheduleDAO,
    private val scheduleRepeatDetailDAO: ScheduleRepeatDetailDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val insertAndGetScheduleWithExtra: InsertAndGetScheduleWithExtraTransaction,
    private val updateAndGetScheduleWithExtra: UpdateAndGetScheduleWithExtraTransaction
) : ScheduleRepository {
    override suspend fun save(query: SaveScheduleQuery): Result<Schedule> = catching {
        when (query) {
            is SaveScheduleQuery.Add -> {
                val insertResult = insertAndGetScheduleWithExtra(contentDTO = query.content.toDTO())
                Schedule(
                    scheduleEntity = insertResult.scheduleEntity,
                    scheduleTagListEntities = insertResult.scheduleTagListEntities,
                    repeatDetailEntities = insertResult.repeatDetailEntities,
                )
            }

            is SaveScheduleQuery.Modify -> {
                val updateResult = updateAndGetScheduleWithExtra(
                    scheduleId = query.id.rawId,
                    contentDTO = query.content.toDTO()
                )
                Schedule(
                    scheduleEntity = updateResult.scheduleEntity,
                    scheduleTagListEntities = updateResult.scheduleTagListEntities,
                    repeatDetailEntities = updateResult.repeatDetailEntities,
                )
            }
        }
    }

    override suspend fun updateAll(query: UpdateAllScheduleQuery): Result<Unit> = catching {
        when (query) {
            is UpdateAllScheduleQuery.Completes -> {
                scheduleDAO.updateByCompletes(
                    idToCompleteTable = query.idToCompleteTable.mapKeys { (key) -> key.rawId }
                )
            }

            is UpdateAllScheduleQuery.VisiblePriorities -> {
                scheduleDAO.updateByVisiblePriorities(
                    idToVisiblePriorityTable = query.idToVisiblePriorityTable
                        .entries
                        .associate { (id, visiblePriority) -> id.rawId to visiblePriority }
                )
            }
        }
    }

    override suspend fun delete(query: DeleteScheduleQuery): Result<Unit> = catching {
        when (query) {
            is DeleteScheduleQuery.ByComplete -> {
                scheduleDAO.deleteByComplete(query.isComplete)
            }

            is DeleteScheduleQuery.ByIds -> {
                scheduleDAO.deleteByScheduleIds(scheduleIds = query.scheduleIds.toSet { it.rawId })
            }
        }
    }

    override fun getSchedulesAsStream(request: GetScheduleQuery): Flow<Set<Schedule>> {
        fun transformSchedules(
            scheduleToRepeatDetailEntitiesTable: Map<ScheduleEntity, Set<RepeatDetailEntity>>,
            scheduleTagListEntities: List<ScheduleTagListEntity>
        ): Set<Schedule> {
            val scheduleIdToEntityTable = scheduleTagListEntities.groupBy { it.scheduleId }
            return scheduleToRepeatDetailEntitiesTable.entries.toSet { (scheduleEntity, repeatDetailEntities) ->
                Schedule(
                    scheduleEntity = scheduleEntity,
                    scheduleTagListEntities = scheduleIdToEntityTable[scheduleEntity.scheduleId]
                        .orEmpty()
                        .toSet(),
                    repeatDetailEntities = repeatDetailEntities
                )
            }
        }

        val entitiesFlow = when (request) {
            is GetScheduleQuery.All -> scheduleRepeatDetailDAO.getAsStream()
            is GetScheduleQuery.ByComplete -> scheduleRepeatDetailDAO.findByCompleteAsStream(request.isComplete)
        }

        return entitiesFlow.distinctUntilChanged().flatMapLatest { entities ->
            scheduleTagListDAO
                .findByScheduleIdsAsStream(scheduleIds = entities.keys.toSet { it.scheduleId })
                .distinctUntilChanged()
                .map { scheduleTagListEntities ->
                    transformSchedules(
                        scheduleToRepeatDetailEntitiesTable = entities,
                        scheduleTagListEntities = scheduleTagListEntities
                    )
                }
        }
    }

    @ExcludeFromGeneratedTestReport
    override fun getScheduleCountAsStream(query: GetScheduleCountQuery): Flow<NonNegativeLong> {
        val rawCountFlow = when (query) {
            is GetScheduleCountQuery.Today -> FakeScheduleRepositoryDelegate.getTodaySchedulesCount()
            is GetScheduleCountQuery.Timetable -> FakeScheduleRepositoryDelegate.getTimetableSchedulesCount()
            is GetScheduleCountQuery.All -> FakeScheduleRepositoryDelegate.getAllSchedulesCount()
        }
        return rawCountFlow.distinctUntilChanged().map(Long::tryToNonNegativeLongOrZero)
    }
}