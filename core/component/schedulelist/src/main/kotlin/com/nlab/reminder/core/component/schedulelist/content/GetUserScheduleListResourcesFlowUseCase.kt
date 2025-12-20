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

package com.nlab.reminder.core.component.schedulelist.content

import com.nlab.reminder.core.data.model.SchedulesLookup
import com.nlab.reminder.core.data.repository.GetScheduleCompletionBacklogStreamQuery
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

/**
 * @author Thalys
 */
class GetUserScheduleListResourcesFlowUseCase(
    private val getScheduleListResourcesFlow: GetScheduleListResourcesFlowUseCase,
    private val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository,
    private val userSelectedSchedulesStore: UserSelectedSchedulesStore
) {
    operator fun invoke(
        schedulesLookupFlow: Flow<SchedulesLookup>
    ): Flow<Set<UserScheduleListResource>> = channelFlow {
        val resultFlow = MutableStateFlow<Set<UserScheduleListResource>?>(null).also { flow ->
            flow.filterNotNull()
                .onEach { send(it) }
                .launchIn(scope = this)
        }
        val sharedSchedulesLookupFlow = schedulesLookupFlow.shareIn(
            scope = this,
            started = SharingStarted.Lazily,
            replay = 1
        )
        combine(
            getScheduleListResourcesFlow(schedulesFlow = schedulesLookupFlow.map { it.values }),
            scheduleCompletionBacklogRepository
                .getBacklogsAsStream(query = GetScheduleCompletionBacklogStreamQuery.LatestPerScheduleId)
                .map { backlogs ->
                    backlogs.associateBy(
                        keySelector = { it.scheduleId },
                        valueTransform = { it.targetCompleted }
                    )
                },
            userSelectedIdsFlowOf(sharedSchedulesLookupFlow)
        ) { scheduleResource, idToTargetCompletionTable, userSelectedIds ->
            scheduleResource.toSet { scheduleListResource ->
                UserScheduleListResource(
                    schedule = scheduleListResource,
                    completionChecked = idToTargetCompletionTable
                        .getOrDefault(key = scheduleListResource.id, defaultValue = scheduleListResource.isComplete),
                    selected = scheduleListResource.id in userSelectedIds
                )
            }
        }.onEach { resultFlow.value = it }.launchIn(scope = this)
    }

    private fun userSelectedIdsFlowOf(
        schedulesLookupFlow: Flow<SchedulesLookup>
    ) = combine(
        schedulesLookupFlow,
        userSelectedSchedulesStore.selectedIds
    ) { scheduleLookup, selectedIds ->
        buildSet {
            selectedIds.forEach { scheduleId ->
                if (scheduleId in scheduleLookup) {
                    this += scheduleId
                }
            }
        }
    }.distinctUntilChanged()
}