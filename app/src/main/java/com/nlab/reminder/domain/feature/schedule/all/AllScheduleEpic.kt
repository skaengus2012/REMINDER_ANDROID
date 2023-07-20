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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.kotlin.coroutine.flow.flatMapLatest
import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.domain.common.data.repository.CompletedScheduleShownAllData
import com.nlab.reminder.domain.common.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.domain.common.data.repository.ScheduleGetStreamRequest
import com.nlab.reminder.domain.common.data.repository.ScheduleRepository
import com.nlab.reminder.domain.common.kotlin.coroutine.inject.DefaultDispatcher
import com.nlab.statekit.middleware.epic.Epic
import com.nlab.statekit.util.buildDslEpic
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * @author thalys
 */
internal class AllScheduleEpic @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    @CompletedScheduleShownAllData private val completedScheduleShownRepository: CompletedScheduleShownRepository,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : Epic<AllScheduleAction> by buildDslEpic(buildDSL = {
    whileStateUsed {
        completedScheduleShownRepository
            .getAsStream()
            .flatMapLatest(scheduleRepository::getLoadedActionAsStream)
            .flowOn(dispatcher)
    }
})

private fun ScheduleRepository.getLoadedActionAsStream(
    isCompletedScheduleShown: Boolean
): Flow<AllScheduleAction.ScheduleLoaded> =
    getAsStream(
        when (isCompletedScheduleShown) {
            true -> ScheduleGetStreamRequest.All
            else -> ScheduleGetStreamRequest.ByComplete(isComplete = false)
        }
    ).map { schedules -> AllScheduleAction.ScheduleLoaded(schedules, isCompletedScheduleShown) }