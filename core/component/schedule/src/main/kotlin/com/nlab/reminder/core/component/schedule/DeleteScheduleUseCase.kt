/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.repository.ScheduleDeletionBacklogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/**
 * @author Doohyun
 */
interface DeleteScheduleUseCase {
    suspend operator fun invoke(scheduleIds: Set<ScheduleId>): ScheduleJobResult
}

internal class DefaultDeleteScheduleUseCase(
    private val deletionBacklogRepository: ScheduleDeletionBacklogRepository,
    private val registerScheduleDeletionJob: RegisterScheduleDeletionJobUseCase
) : DeleteScheduleUseCase {
    override suspend fun invoke(scheduleIds: Set<ScheduleId>): ScheduleJobResult {
        if (scheduleIds.isEmpty()) return ScheduleJobResult.Success
        return deletionBacklogRepository.save(scheduleIds).fold(
            onSuccess = { registerScheduleDeletionJob() },
            onFailure = { ScheduleJobResult.Failure(it) }
        )
    }
}

internal class EnsuredDeleteScheduleUseCase(
    private val coroutineScope: CoroutineScope,
    private val deleteScheduleUseCase: DeleteScheduleUseCase
) : DeleteScheduleUseCase {
    @ExcludeFromGeneratedTestReport
    override suspend fun invoke(
        scheduleIds: Set<ScheduleId>
    ): ScheduleJobResult = coroutineScope
        .async { deleteScheduleUseCase.invoke(scheduleIds) }
        .await()
}
