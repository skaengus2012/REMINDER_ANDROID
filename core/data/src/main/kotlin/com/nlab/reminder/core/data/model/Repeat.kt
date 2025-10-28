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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

/**
 * @author Doohyun
 */
sealed class Repeat
sealed class DateOnlyRepeat : Repeat()

data class HourlyRepeat(val interval: PositiveInt) : Repeat()

data class DailyRepeat(val interval: PositiveInt) : DateOnlyRepeat()

data class WeeklyRepeat(val interval: PositiveInt, val daysOfWeeks: NonEmptySet<DayOfWeek>) : DateOnlyRepeat()

data class MonthlyRepeat(val interval: PositiveInt, val detail: MonthlyRepeatDetail) : DateOnlyRepeat()

data class YearlyRepeat(
    val interval: PositiveInt,
    val months: NonEmptySet<Month>,
    val daysOfWeekOption: YearlyDaysOfWeekOption?
) : DateOnlyRepeat()