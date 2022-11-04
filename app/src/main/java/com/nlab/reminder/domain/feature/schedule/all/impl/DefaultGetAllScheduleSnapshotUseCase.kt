/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.schedule.all.impl

import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleSnapshot
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleSnapshotUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * @author thalys
 */
class DefaultGetAllScheduleSnapshotUseCase(
    scheduleRepository: ScheduleRepository,
    private val scheduleUiStateFlowFactory: ScheduleUiStateFlowFactory,
    private val completedScheduleShownRepository: CompletedScheduleShownRepository,
) : GetAllScheduleSnapshotUseCase {
    private val findAllSchedules: Flow<List<Schedule>> =
        scheduleRepository.get(ScheduleRequest.Find)
    private val findNotCompleteSchedules: Flow<List<Schedule>> =
        scheduleRepository.get(ScheduleRequest.FindWithComplete(isComplete = false))

    @ExperimentalCoroutinesApi
    override fun invoke(): Flow<AllScheduleSnapshot> =
        completedScheduleShownRepository.get().flatMapLatest(this::getSnapshot)

    private fun getSnapshot(isCompletedScheduleShown: Boolean): Flow<AllScheduleSnapshot> =
        getSchedules(isCompletedScheduleShown)
            .let(scheduleUiStateFlowFactory::with)
            .map { scheduleUiStates -> AllScheduleSnapshot(scheduleUiStates, isCompletedScheduleShown) }

    private fun getSchedules(isDoneScheduleShown: Boolean): Flow<List<Schedule>> =
        if (isDoneScheduleShown) findAllSchedules
        else findNotCompleteSchedules
}