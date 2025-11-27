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

/**
 * @author Doohyun
 */
fun genRepeat(): Repeat = when (genInt(min = 0, max = 4)) {
    0 -> genHourlyRepeat()
    1 -> genDailyRepeat()
    2 -> genWeeklyRepeat()
    3 -> genMonthlyRepeat()
    else -> genYearlyRepeat()
}

fun genDateOnlyRepeat(): DateOnlyRepeat = when (genInt(min = 0, max = 3)) {
    0 -> genDailyRepeat()
    1 -> genWeeklyRepeat()
    2 -> genMonthlyRepeat()
    else -> genYearlyRepeat()
}

fun genHourlyRepeat(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt()
): HourlyRepeat = HourlyRepeat(interval = interval)

fun genDailyRepeat(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt()
): DailyRepeat = DailyRepeat(interval = interval)

fun genWeeklyRepeat(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt(),
    daysOfWeeks: NonEmptySet<DayOfWeek> = DayOfWeek.entries
        .shuffledSubset()
        .toNonEmptySet()
): WeeklyRepeat = WeeklyRepeat(
    interval = interval,
    daysOfWeeks = daysOfWeeks
)

fun genMonthlyRepeat(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt(),
    detail: MonthlyRepeatDetail = genMonthlyRepeatDetail()
): MonthlyRepeat = MonthlyRepeat(
    interval = interval,
    detail = detail
)

fun genYearlyRepeat(
    interval: PositiveInt = genInt(min = 1, max = 999).toPositiveInt(),
    months: NonEmptySet<Month> = Month.entries
        .shuffledSubset()
        .toNonEmptySet(),
    daysOfWeekOption: YearlyDaysOfWeekOption? = genYearlyDaysOfWeekOption()
): YearlyRepeat = YearlyRepeat(
    interval = interval,
    months = months,
    daysOfWeekOption = daysOfWeekOption
)