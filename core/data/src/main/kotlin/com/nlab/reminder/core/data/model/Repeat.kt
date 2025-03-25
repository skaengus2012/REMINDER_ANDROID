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
import kotlinx.datetime.TimeZone

/**
 * @author Doohyun
 */
sealed class Repeat {
    data class Hourly(val interval: PositiveInt) : Repeat()

    data class Daily(val interval: PositiveInt) : Repeat()

    data class Weekly(
        val interval: PositiveInt,
        val timeZone: TimeZone,
        val daysOfWeeks: NonEmptySet<DayOfWeek>
    ) : Repeat()

    data class Monthly(
        val interval: PositiveInt,
        val timeZone: TimeZone,
        val detail: MonthlyRepeatDetail
    ) : Repeat()

    data class Yearly(
        val interval: PositiveInt,
        val timeZone: TimeZone,
        val months: NonEmptySet<Month>,
        val daysOfWeekOption: YearlyDaysOfWeekOption?
    ) : Repeat()
}
