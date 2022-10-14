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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn

/**
 * @author Doohyun
 */
class DefaultGetAllScheduleSnapshotUseCase(
    private val doneScheduleShownRepository: DoneScheduleShownRepository,
    private val scheduleRepository: ScheduleRepository,
    private val scheduleUiStateFlowFactory: ScheduleUiStateFlowFactory,
    private val dispatcher: CoroutineDispatcher
) : GetAllScheduleSnapshotUseCase {
    @FlowPreview
    override fun invoke(): Flow<AllScheduleSnapshot> =
        doneScheduleShownRepository.get()
            .flatMapConcat(this::createAllScheduleReportFlow)
            .flowOn(dispatcher)

    private fun getScheduleFlow(isDoneScheduleShown: Boolean): Flow<List<Schedule>> =
        scheduleRepository.get(
            if (isDoneScheduleShown) ScheduleItemRequest.Find
            else ScheduleItemRequest.FindByComplete(isComplete = false)
        )

    private fun createAllScheduleReportFlow(isDoneScheduleShown: Boolean): Flow<AllScheduleSnapshot> {
        return getScheduleFlow(isDoneScheduleShown)
            .let(scheduleUiStateFlowFactory::with)
            .map { scheduleItems -> AllScheduleSnapshot(scheduleItems, isDoneScheduleShown) }
    }
}