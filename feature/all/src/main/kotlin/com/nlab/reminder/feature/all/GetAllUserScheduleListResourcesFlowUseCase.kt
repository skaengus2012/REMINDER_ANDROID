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

import com.nlab.reminder.core.component.schedulelist.content.GetUserScheduleListResourcesFlowUseCase
import com.nlab.reminder.core.component.schedulelist.content.UserScheduleListResource
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.SchedulesLookup
import com.nlab.reminder.core.data.qualifiers.ScheduleData
import com.nlab.reminder.core.data.qualifiers.ScheduleDataOption.All
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.kotlinx.coroutines.flow.channelFlow
import com.nlab.reminder.core.kotlinx.coroutines.flow.combine
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
internal class GetAllUserScheduleListResourcesFlowUseCase @Inject constructor(
    @param:ScheduleData(All) private val completedScheduleShownRepository: CompletedScheduleShownRepository,
    private val scheduleRepository: ScheduleRepository,
    private val getUserScheduleListResourcesFlow: GetUserScheduleListResourcesFlowUseCase,
) {
    operator fun invoke(): Flow<List<UserScheduleListResource>> = completedScheduleShownRepository
        .getAsStream()
        .flatMapLatest { isCompletedScheduleVisible ->
            scheduleRepository
                .getSchedulesAsStream(
                    request = if (isCompletedScheduleVisible) {
                        GetScheduleQuery.All
                    } else {
                        GetScheduleQuery.ByComplete(isComplete = false)
                    }
                )
                .let(::getScheduleResourcesFlowWith)
        }

    private fun getScheduleResourcesFlowWith(scheduleFlow: Flow<Set<Schedule>>) = channelFlow {
        val resultFlow = MutableStateFlow<List<UserScheduleListResource>?>(null).also { flow ->
            flow.filterNotNull()
                .onEach { send(it) }
                .launchIn(scope = this)
        }
        val schedulesLookupFlow = scheduleFlow
            .map(::SchedulesLookup)
            .shareIn(scope = this, started = SharingStarted.Lazily, replay = 1)
        combine(
            schedulesLookupFlow,
            getUserScheduleListResourcesFlow(schedulesLookupFlow)
        ) { schedulesLookup, userScheduleListResources ->
            if (schedulesLookup.values.size != userScheduleListResources.size) return@combine null
            if (userScheduleListResources.any { it.schedule.id !in schedulesLookup }) return@combine null

            userScheduleListResources.sortedWith(
                // 1. Uncompleted schedules precede completed schedules.
                // 2. compared by displayPriority
                comparator = compareBy<UserScheduleListResource> { userScheduleListResource ->
                    schedulesLookup.requireValue(userScheduleListResource.schedule.id).isComplete
                }.thenComparing { userScheduleListResource ->
                    schedulesLookup.requireValue(userScheduleListResource.schedule.id).visiblePriority.value
                }
            ).also {
                println("Hello newSource first ${it.firstOrNull()?.schedule?.id} ${it.firstOrNull()?.completionChecked}")
            }
        }.filterNotNull().onEach { resultFlow.value = it }.launchIn(scope = this)
    }
}