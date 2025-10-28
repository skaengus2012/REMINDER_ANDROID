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

package com.nlab.reminder.core.component.displayformat

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.data.model.Repeat
import com.nlab.reminder.core.data.model.ScheduleTiming
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * @author Doohyun
 */
sealed class ScheduleTimingDisplayResource {
    @ExcludeFromGeneratedTestReport
    data class DateTime(
        val triggerAt: LocalDateTime,
        val entryAt: LocalDateTime,
        val repeat: Repeat?
    ) : ScheduleTimingDisplayResource()

    @ExcludeFromGeneratedTestReport
    data class Date(
        val triggerAt: LocalDate,
        val entryAt: LocalDateTime,
        val repeat: Repeat?
    ) : ScheduleTimingDisplayResource()
}

fun ScheduleTimingDisplayResource(
    scheduleTiming: ScheduleTiming,
    timeZone: TimeZone,
    entryAt: Instant,
): ScheduleTimingDisplayResource {
    val entryAtLocalDateTime = entryAt.toLocalDateTime(timeZone)
    return when (scheduleTiming) {
        is ScheduleTiming.Date -> {
            ScheduleTimingDisplayResource.Date(
                triggerAt = scheduleTiming.triggerAt
                    .toLocalDateTime(TimeZone.UTC)
                    .date,
                entryAt = entryAtLocalDateTime,
                repeat = scheduleTiming.dateOnlyRepeat
            )
        }

        is ScheduleTiming.DateTime -> {
            ScheduleTimingDisplayResource.DateTime(
                triggerAt = scheduleTiming.triggerAt.toLocalDateTime(timeZone),
                entryAt = entryAtLocalDateTime,
                repeat = scheduleTiming.repeat
            )
        }
    }
}