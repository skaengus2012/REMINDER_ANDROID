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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

/**
 * @author Doohyun
 */
sealed class Repeat {
    data class Hourly(val frequency: PositiveInt) : Repeat()

    data class Daily(val frequency: PositiveInt) : Repeat()

    data class Weekly(
        val frequency: PositiveInt,
        val daysOfWeeks: Set<DayOfWeek>
    ) : Repeat()

    data class Monthly(
        val frequency: PositiveInt,
        val detail: MonthlyRepeatDetail
    )

    data class Yearly(
        val month: Month,
        val daysOfWeek: YearlyDaysOfWeekOption?
    )
}
