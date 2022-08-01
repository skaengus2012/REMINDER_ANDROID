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

package com.nlab.reminder.domain.common.schedule.impl

import com.nlab.reminder.core.kotlin.coroutine.Delay
import com.nlab.reminder.core.kotlin.coroutine.flow.firstElement
import com.nlab.reminder.domain.common.schedule.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * @author Doohyun
 */
class DefaultUpdateScheduleCompleteUseCase(
    private val scheduleRepository: ScheduleRepository,
    private val pendingDelayed: Delay
) : UpdateScheduleCompleteUseCase {
    override suspend fun invoke(scheduleId: ScheduleId, isComplete: Boolean) {
        withContext(NonCancellable) {
            scheduleRepository.updatePendingComplete(scheduleId, isComplete)
            pendingDelayed()

            scheduleRepository
                .get(ScheduleItemRequest.FindByScheduleId(scheduleId))
                .firstElement()
                ?.let { schedule ->
                    println("sadsdasad ${schedule.id()} ${schedule.isComplete}")
                    scheduleRepository.updateComplete(schedule.id(), schedule.isComplete) }
        }
    }
}