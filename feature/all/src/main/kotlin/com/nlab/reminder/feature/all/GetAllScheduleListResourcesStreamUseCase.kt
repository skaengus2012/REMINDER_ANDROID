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

package com.nlab.reminder.feature.all

import com.nlab.reminder.core.component.schedulelist.content.GetScheduleListResourcesStreamUseCase
import com.nlab.reminder.core.component.schedulelist.content.ScheduleListResource
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.qualifiers.ScheduleData
import com.nlab.reminder.core.data.qualifiers.ScheduleDataOption.All
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.kotlinx.coroutines.flow.channelFlow
import com.nlab.reminder.core.kotlinx.coroutines.flow.combine
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * @author Thalys
 */
internal class GetAllScheduleListResourcesStreamUseCase @Inject constructor(
    @param:ScheduleData(All) private val completedScheduleShownRepository: CompletedScheduleShownRepository,
    private val scheduleRepository: ScheduleRepository,
    private val getScheduleListResourcesStream: GetScheduleListResourcesStreamUseCase,
) {
    private val scheduleComparator = compareBy<Schedule> {
        // isComplete asc (false -> true)
        it.isComplete
    }.thenComparing { it.visiblePriority.value }

    operator fun invoke(): Flow<List<ScheduleListResource>> = completedScheduleShownRepository
        .getAsStream()
        .flatMapLatest { isCompletedScheduleVisible ->
            channelFlow {
                val schedulesStateFlow = scheduleRepository
                    .getSchedulesAsStream(
                        request = if (isCompletedScheduleVisible) {
                            GetScheduleQuery.All
                        } else {
                            GetScheduleQuery.ByComplete(isComplete = false)
                        }
                    )
                    .stateIn(
                        scope = this,
                        started = SharingStarted.Eagerly,
                        initialValue = emptySet()
                    )
                combine(
                    schedulesStateFlow.map { schedules -> schedules.associateBy { it.id } },
                    getScheduleListResourcesStream(schedulesStateFlow),
                ) { idToScheduleTable, scheduleListResources ->
                    if (idToScheduleTable.size != scheduleListResources.size
                        && scheduleListResources.any { it.id !in idToScheduleTable }
                    ) {
                        // invalid state
                        return@combine null
                    }
                    scheduleListResources.sortedWith { resource1, resource2 ->
                        val schedule1 = idToScheduleTable.getValue(resource1.id)
                        val schedule2 = idToScheduleTable.getValue(resource2.id)
                        scheduleComparator.compare(schedule1, schedule2)
                    }
                }.filterNotNull().onEach { send(it) }.launchIn(scope = this)
            }
        }

    private fun getScheduleResourcesWith(
        isCompletedScheduleVisible: Boolean
    ): Flow<List<ScheduleListResource>> = channelFlow {
        val schedulesStateFlow = scheduleRepository
            .getSchedulesAsStream(
                request = if (isCompletedScheduleVisible) {
                    GetScheduleQuery.All
                } else {
                    GetScheduleQuery.ByComplete(isComplete = false)
                }
            )
            .stateIn(
                scope = this,
                started = SharingStarted.Eagerly,
                initialValue = emptySet()
            )
        combine(
            schedulesStateFlow
                .map { schedules -> schedules.associateBy { it.id } }
                .distinctUntilChanged(),
            getScheduleListResourcesStream(schedulesStateFlow),
        ) { idToScheduleTable, scheduleListResources ->
            if (idToScheduleTable.size != scheduleListResources.size
                && scheduleListResources.any { it.id !in idToScheduleTable }
            ) {
                // invalid state
                return@combine null
            }
            scheduleListResources.sortedWith { resource1, resource2 ->
                val schedule1 = idToScheduleTable.getValue(resource1.id)
                val schedule2 = idToScheduleTable.getValue(resource2.id)
                scheduleComparator.compare(schedule1, schedule2)
            }
        }.filterNotNull().onEach { send(it) }.launchIn(scope = this)
    }
}