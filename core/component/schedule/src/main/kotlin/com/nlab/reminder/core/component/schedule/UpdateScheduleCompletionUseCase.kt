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

package com.nlab.reminder.core.component.schedule

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.time.Duration

/**
 * @author Thalys
 */
class UpdateScheduleCompletionUseCase(
    private val coroutineScope: CoroutineScope,
    private val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository,
    private val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase,
    private val debounceTimeout: Duration
) {
    suspend operator fun invoke(
        scheduleId: ScheduleId,
        targetCompleted: Boolean,
    ): Result<Unit> {
        val resultJob = coroutineScope.async {
            scheduleCompletionBacklogRepository
                .save(scheduleId, targetCompleted)
                .onSuccess { backlog ->
                    registerScheduleCompleteJob(debounceTimeout, processUntilPriority = backlog.priority)
                }
        }
        return resultJob.await().map { /* do nothing */ }
    }
}