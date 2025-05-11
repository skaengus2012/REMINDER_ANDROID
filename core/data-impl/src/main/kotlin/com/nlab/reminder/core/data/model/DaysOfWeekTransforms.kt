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

import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_FRI
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_MON
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_SAT
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_SUN
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_THU
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_TUE
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_WED
import com.nlab.reminder.core.local.database.entity.RepeatWeek
import kotlinx.datetime.DayOfWeek

/**
 * @author Thalys
 */
internal fun DayOfWeek(@RepeatWeek repeatWeek: String): DayOfWeek = when (repeatWeek) {
    REPEAT_WEEK_SUN -> DayOfWeek.SUNDAY
    REPEAT_WEEK_MON -> DayOfWeek.MONDAY
    REPEAT_WEEK_TUE -> DayOfWeek.TUESDAY
    REPEAT_WEEK_WED -> DayOfWeek.WEDNESDAY
    REPEAT_WEEK_THU -> DayOfWeek.THURSDAY
    REPEAT_WEEK_FRI -> DayOfWeek.FRIDAY
    REPEAT_WEEK_SAT -> DayOfWeek.SATURDAY
    else -> throw IllegalArgumentException("Invalid repeat week : $repeatWeek")
}

@RepeatWeek
internal fun DayOfWeek.toRepeatWeek(): String = when (this) {
    DayOfWeek.SUNDAY -> REPEAT_WEEK_SUN
    DayOfWeek.MONDAY -> REPEAT_WEEK_MON
    DayOfWeek.TUESDAY -> REPEAT_WEEK_TUE
    DayOfWeek.WEDNESDAY -> REPEAT_WEEK_WED
    DayOfWeek.THURSDAY -> REPEAT_WEEK_THU
    DayOfWeek.FRIDAY -> REPEAT_WEEK_FRI
    DayOfWeek.SATURDAY -> REPEAT_WEEK_SAT
}