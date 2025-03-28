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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

/**
 * @author Doohyun
 */
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
        .shuffled()
        .let { dayOfWeeks -> dayOfWeeks.take(genInt(min = 1, max = dayOfWeeks.size)) }
        .toNonEmptySet()
): Repeat.Weekly = Repeat.Weekly(
    interval = interval,
    timeZone = timeZone,
    daysOfWeeks = daysOfWeeks
)