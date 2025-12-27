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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.ScheduleCompletionBacklog
import com.nlab.reminder.core.data.model.ScheduleCompletionBacklogId
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.repository.GetScheduleCompletionBacklogQuery
import com.nlab.reminder.core.data.repository.GetScheduleCompletionBacklogStreamQuery
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.reminder.core.local.database.dao.ScheduleCompletionBacklogDAO
import com.nlab.reminder.core.local.database.entity.ScheduleCompletionBacklogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * @author Doohyun
 */
class LocalScheduleCompletionBacklogRepository(
    private val scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO
) : ScheduleCompletionBacklogRepository {
    override suspend fun save(
        scheduleId: ScheduleId,
        targetCompleted: Boolean
    ): Result<ScheduleCompletionBacklog> {
        val entityResult = runCatching { scheduleCompletionBacklogDAO.insertAndGet(scheduleId.rawId, targetCompleted) }
        return entityResult.map(::ScheduleCompletionBacklog)
    }

    override suspend fun delete(backlogIds: Set<ScheduleCompletionBacklogId>): Result<Unit> {
        val scheduleCompletionBacklogIds = backlogIds.toSet { it.rawId }
        return runCatching { scheduleCompletionBacklogDAO.deleteByIds(scheduleCompletionBacklogIds) }
    }

    override suspend fun getBacklogs(query: GetScheduleCompletionBacklogQuery): Result<Set<ScheduleCompletionBacklog>> {
        val entities = runCatching {
            when (query) {
                is GetScheduleCompletionBacklogQuery.All -> {
                    scheduleCompletionBacklogDAO.getAll()
                }
                is GetScheduleCompletionBacklogQuery.ByScheduleIdsUpToPriority -> {
                    scheduleCompletionBacklogDAO.findAllByScheduleIdsUpToInsertOrder(
                        insertOrder = query.priority.value
                    )
                }
            }
        }
        return entities.map(List<ScheduleCompletionBacklogEntity>::toBacklogs)
    }

    override fun getBacklogsAsStream(
        query: GetScheduleCompletionBacklogStreamQuery
    ): Flow<Set<ScheduleCompletionBacklog>> {
        val entitiesFlow = when (query) {
            GetScheduleCompletionBacklogStreamQuery.LatestPerScheduleId -> {
                scheduleCompletionBacklogDAO.getLatestPerScheduleIdAsStream()
            }
        }
        return entitiesFlow.distinctUntilChanged().map(List<ScheduleCompletionBacklogEntity>::toBacklogs)
    }
}

private fun List<ScheduleCompletionBacklogEntity>.toBacklogs(): Set<ScheduleCompletionBacklog> =
    toSet(::ScheduleCompletionBacklog)