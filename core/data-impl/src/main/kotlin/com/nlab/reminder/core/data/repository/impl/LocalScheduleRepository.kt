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
import com.nlab.reminder.core.data.model.toLocalDTO
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.data.repository.fake.FakeScheduleRepositoryDelegate
import com.nlab.reminder.core.data.repository.DeleteScheduleQuery
import com.nlab.reminder.core.data.repository.GetScheduleCountQuery
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.UpdateSchedulesQuery
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.tryToNonNegativeLongOrZero
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * @author Doohyun
 */
/**
class LocalScheduleRepository(
    private val scheduleDAO: ScheduleDAO,
) : ScheduleRepository {
    override suspend fun save(query: SaveScheduleQuery): Result<Schedule> = catching {
        val entity = when (query) {
            is SaveScheduleQuery.Add -> scheduleDAO.insertAndGet(query.content.toLocalDTO())
            is SaveScheduleQuery.Modify -> {
                scheduleDAO.updateAndGet(scheduleId = query.id.rawId, contentDTO = query.content.toLocalDTO())
            }
        }
        return@catching Schedule(entity)
    }

    override suspend fun updateBulk(query: UpdateSchedulesQuery): Result<Unit> = when (query) {
        // When outside the catch block, jacoco does not recognize. 😭
        is UpdateSchedulesQuery.Completes -> catching {
            scheduleDAO.updateByCompletes(
                idToCompleteTable = query.idToCompleteTable.mapKeys { (key) -> key.rawId }
            )
        }

        is UpdateSchedulesQuery.VisiblePriorities -> catching {
            scheduleDAO.updateByVisiblePriorities(
                idToVisiblePriorityTable = buildMap {
                    query.idToVisiblePriorityTable.forEach { (id, visiblePriority) ->
                        put(id.rawId, visiblePriority.value)
                    }
                },
                isCompletedRange = query.isCompletedRange
            )
        }
    }

    override suspend fun delete(query: DeleteScheduleQuery): Result<Unit> = when (query) {
        // When outside the catch block, jacoco does not recognize. 😭
        is DeleteScheduleQuery.ByComplete -> catching {
            scheduleDAO.deleteByComplete(query.isComplete)
        }

        is DeleteScheduleQuery.ByIds -> catching {
            scheduleDAO.deleteByScheduleIds(
                scheduleIds = buildSet { query.scheduleIds.mapTo(destination = this) { it.rawId } }
            )
        }
    }

    override fun getSchedulesAsStream(request: GetScheduleQuery): Flow<Set<Schedule>> {
        val scheduleEntitiesFlow = when (request) {
            is GetScheduleQuery.All -> scheduleDAO.getAsStream()
            is GetScheduleQuery.ByComplete -> scheduleDAO.findByCompleteAsStream(request.isComplete)
        }

        return scheduleEntitiesFlow.distinctUntilChanged().map { entities -> entities.toSet(::Schedule) }
    }

    @ExcludeFromGeneratedTestReport
    override fun getScheduleCountAsStream(query: GetScheduleCountQuery): Flow<NonNegativeLong> {
        val rawCountFlow = when (query) {
            is GetScheduleCountQuery.Today -> FakeScheduleRepositoryDelegate.getTodaySchedulesCount()
            is GetScheduleCountQuery.Timetable -> FakeScheduleRepositoryDelegate.getTimetableSchedulesCount()
            is GetScheduleCountQuery.All -> FakeScheduleRepositoryDelegate.getAllSchedulesCount()
        }
        return rawCountFlow.map(Long::tryToNonNegativeLongOrZero)
    }
}*/