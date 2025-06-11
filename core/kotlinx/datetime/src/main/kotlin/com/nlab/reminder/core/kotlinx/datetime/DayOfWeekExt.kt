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

package com.nlab.reminder.core.kotlinx.datetime

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.DayOfWeek

/**
 * @author Doohyun
 */
private val weekdays = persistentSetOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY
)

private val weekend = persistentSetOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.SATURDAY
)

@Suppress("FunctionName")
fun Weekdays(): Set<DayOfWeek> = weekdays

@Suppress("FunctionName")
fun Weekend(): Set<DayOfWeek> = weekend

fun Set<DayOfWeek>.isExactlyWeekdays(): Boolean {
    return this == weekdays
}

fun Set<DayOfWeek>.isExactlyWeekend(): Boolean {
    return this == weekend
}