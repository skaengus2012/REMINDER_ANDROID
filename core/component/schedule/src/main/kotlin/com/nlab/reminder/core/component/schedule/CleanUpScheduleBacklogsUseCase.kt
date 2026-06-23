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
import dagger.Reusable
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * @author Doohyun
 */
@Reusable
class CleanUpScheduleBacklogsUseCase @Inject constructor(
    private val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase,
    private val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase
) {
    suspend operator fun invoke(): CleanUpScheduleBacklogsReport {
        // Run deletion first to clear deleted schedules before updating completion states.
        val deletionResult = requestScheduleDeletionJob()
        val completeResult = requestScheduleCompletionJob(
            debounceTimeout = 0.seconds,
            processUntilPriority = null
        )

        return CleanUpScheduleBacklogsReport(
            completionResult = completeResult,
            deletionResult = deletionResult,
        )
    }
}

@ExcludeFromGeneratedTestReport
data class CleanUpScheduleBacklogsReport(
    val completionResult: ScheduleJobResult,
    val deletionResult: ScheduleJobResult,
)
