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

package com.nlab.reminder.core.component.displayformat.ui

import android.content.res.Resources
import com.nlab.reminder.core.component.displayformat.ScheduleTimingDisplayResource
import com.nlab.reminder.core.data.model.MonthlyRepeatDetail
import com.nlab.reminder.core.data.model.Repeat
import com.nlab.reminder.core.data.model.rawValue
import kotlinx.datetime.LocalDate

/**
 * Generates a localized display string for a given [Repeat].
 *
 * This function takes a [Repeat] object and a [triggerAt] date to produce a human-readable string
 * describing how often an event repeats. It handles various repeat types like hourly, daily, weekly,
 * monthly, and yearly, and adapts the output based on the provided [Resources].
 *
 * Here's how to display information using [ScheduleTimingDisplayResource].
 * ```
 * val scheduleTimingDisplayResource = ScheduleTimingDisplayResource(...)
 * val context: Context = getContext() // Android context
 * val repeat: Repeat?
 * val triggerAt: LocalDate
 * when (scheduleTimingDisplayResource) {
 *     is ScheduleTimingDisplayResource.DateOnly -> {
 *          repeat = scheduleTimingDisplayResource.repeat
 *          triggerAt = scheduleTimingDisplayResource.triggerAt
 *     }
 *
 *     is ScheduleTimingDisplayResource.DateTime -> {
 *          repeat = scheduleTimingDisplayResource.repeat
 *          triggerAt = scheduleTimingDisplayResource.triggerAt.date
 *     }
 * }
 * val result = if (repeat == null) ""
 * else {
 *     repeatDisplayText(
 *         resources = context.resources,
 *         repeat = repeat,
 *         triggerAt = triggerAt
 *     )
 * }
 * ```
 *
 * @author Doohyun
 */
fun repeatDisplayText(
    resources: Resources,
    repeat: Repeat,
    triggerAt: LocalDate
): String = when (repeat) {
    is Repeat.Hourly -> {
        contentEveryHours(resources, repeat.interval)
    }

    is Repeat.Daily -> {
        contentEveryDays(resources, repeat.interval)
    }

    is Repeat.Weekly -> {
        contentEveryWeeks(
            resources,
            repeat.interval,
            repeat.daysOfWeeks,
            isSameDayOfWeek = repeat.daysOfWeeks.value.let { it.size == 1 && it.first() == triggerAt.dayOfWeek }
        )
    }

    is Repeat.Monthly -> {
        when (val repeatDetail = repeat.detail) {
            is MonthlyRepeatDetail.Each -> {
                contentEveryMonthsWithEachOption(
                    resources,
                    repeat.interval,
                    repeatDetail,
                    isSameDay = repeatDetail.days.value.let {
                        it.size == 1 && it.first().rawValue == triggerAt.day
                    },
                )
            }

            is MonthlyRepeatDetail.Customize -> {
                contentEveryMonthsWithCustomizeOption(
                    resources,
                    repeat.interval,
                    repeatDetail
                )
            }
        }
    }

    is Repeat.Yearly -> {
        contentEveryYears(
            resources,
            repeat.interval,
            repeat.months,
            repeat.daysOfWeekOption,
            isSameMonth = repeat.months.value.let { it.size == 1 && it.first() == triggerAt.month },
        )
    }
}