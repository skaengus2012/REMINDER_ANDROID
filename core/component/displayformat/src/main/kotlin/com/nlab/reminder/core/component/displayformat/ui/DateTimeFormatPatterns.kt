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
import com.nlab.reminder.core.translation.StringIds
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.daysUntil

/**
 * @author Doohyun
 */
fun triggerAtDateTimeFormatPatternForList(
    resources: Resources,
    triggerAt: LocalDate,
    entryAt: LocalDate,
): String = triggerAtDateTimeFormatPattern(
    resources = resources,
    triggerAt = triggerAt,
    entryAt = entryAt,
    onYesterday = { StringIds.datetime_format_list_date_only_yesterday },
    onToday = { StringIds.datetime_format_list_date_only_today },
    onTomorrow = { StringIds.datetime_format_list_date_only_tomorrow },
    onDayAfterTomorrow = { StringIds.datetime_format_list_date_only_the_day_after_tomorrow },
    onElse = { StringIds.datetime_format_list_date_only_default }
)

fun triggerAtDateTimeFormatPatternForList(
    resources: Resources,
    triggerAt: LocalDateTime,
    entryAt: LocalDateTime,
) = triggerAtDateTimeFormatPattern(
    resources = resources,
    triggerAt = triggerAt.date,
    entryAt = entryAt.date,
    onYesterday = { StringIds.datetime_format_list_yesterday },
    onToday = { StringIds.datetime_format_list_today },
    onTomorrow = { StringIds.datetime_format_list_tomorrow },
    onDayAfterTomorrow = { StringIds.datetime_format_list_the_day_after_tomorrow },
    onElse = { StringIds.datetime_format_list_default }
)

private inline fun triggerAtDateTimeFormatPattern(
    resources: Resources,
    triggerAt: LocalDate,
    entryAt: LocalDate,
    onYesterday: () -> Int,
    onToday: () -> Int,
    onTomorrow: () -> Int,
    onDayAfterTomorrow: () -> Int,
    onElse: () -> Int,
): String = resources.getString(
    when (triggerAt.daysUntil(entryAt)) {
        1 -> onYesterday()
        0 -> onToday()
        -1 -> onTomorrow()
        -2 -> onDayAfterTomorrow()
        else -> onElse()
    }
)