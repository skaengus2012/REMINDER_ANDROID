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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.NonEmptySet
import com.nlab.reminder.core.kotlin.collections.toNonEmptySet
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.shuffledSubset
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone

/**
 * @author Doohyun
 */
fun genRepeat(): Repeat = when (genInt(min = 0, max = 4)) {
    0 -> genRepeatHourly()
    1 -> genRepeatDaily()
    2 -> genRepeatWeekly()
    3 -> genRepeatMonthly()
    else -> genRepeatYearly()
}

fun genRepeatHourly(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt()
): Repeat.Hourly = Repeat.Hourly(interval = interval)

fun genRepeatDaily(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt()
): Repeat.Daily = Repeat.Daily(interval = interval)

fun genRepeatWeekly(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    daysOfWeeks: NonEmptySet<DayOfWeek> = DayOfWeek.entries
        .shuffledSubset()
        .toNonEmptySet()
): Repeat.Weekly = Repeat.Weekly(
    interval = interval,
    timeZone = timeZone,
    daysOfWeeks = daysOfWeeks
)

fun genRepeatMonthly(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    detail: MonthlyRepeatDetail = genMonthlyRepeatDetail()
): Repeat.Monthly = Repeat.Monthly(
    interval = interval,
    timeZone = timeZone,
    detail = detail
)

fun genRepeatYearly(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    months: NonEmptySet<Month> = Month.entries
        .shuffledSubset()
        .toNonEmptySet(),
    daysOfWeekOption: YearlyDaysOfWeekOption? = genYearlyDaysOfWeekOption()
): Repeat.Yearly = Repeat.Yearly(
    interval = interval,
    timeZone = timeZone,
    months = months,
    daysOfWeekOption = daysOfWeekOption
)