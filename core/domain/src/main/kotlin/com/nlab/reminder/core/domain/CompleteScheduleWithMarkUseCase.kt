/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.UpdateSchedulesQuery
import com.nlab.reminder.core.data.repository.getSnapshot
import com.nlab.reminder.core.kotlinx.coroutine.delay.Delay
import com.nlab.reminder.core.kotlin.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * @author thalys
 */
class CompleteScheduleWithMarkUseCase(
    private val scheduleRepository: ScheduleRepository,
    private val completeMarkRepository: ScheduleCompleteMarkRepository,
    private val aggregateDelay: Delay,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(id: ScheduleId, isComplete: Boolean): Result<Unit> = withContext(dispatcher) {
        completeMarkRepository.add(id, isComplete)
        aggregateDelay()
        val completeMarkTable = completeMarkRepository.getSnapshot()

        if (completeMarkTable.isEmpty()) Result.Success(Unit)
        else scheduleRepository.updateBulk(UpdateSchedulesQuery.Completes(completeMarkTable))
    }
}