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

/**
 * @author Doohyun
 */
sealed interface ScheduleJobResult {
    @ExcludeFromGeneratedTestReport
    data object Success : ScheduleJobResult

    @ExcludeFromGeneratedTestReport
    data object Cancelled : ScheduleJobResult

    @ExcludeFromGeneratedTestReport
    data object Retrying : ScheduleJobResult

    @ExcludeFromGeneratedTestReport
    data class Failure(val throwable: Throwable) : ScheduleJobResult
}
