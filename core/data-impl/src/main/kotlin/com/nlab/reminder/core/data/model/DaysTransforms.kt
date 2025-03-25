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

import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_DAY
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_FRI
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_MON
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_SAT
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_SUN
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_THU
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_TUE
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_WED
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_WEEKDAY
import com.nlab.reminder.core.local.database.model.REPEAT_DAYS_WEEKEND
import com.nlab.reminder.core.local.database.model.RepeatDays

/**
 * @author Thalys
 */
internal fun Days(@RepeatDays days: String): Days = when (days) {
    REPEAT_DAYS_SUN -> Days.Sun
    REPEAT_DAYS_MON -> Days.Mon
    REPEAT_DAYS_TUE -> Days.Tue
    REPEAT_DAYS_WED -> Days.Wed
    REPEAT_DAYS_THU -> Days.Thu
    REPEAT_DAYS_FRI -> Days.Fri
    REPEAT_DAYS_SAT -> Days.Sat
    REPEAT_DAYS_DAY -> Days.Default
    REPEAT_DAYS_WEEKDAY -> Days.Weekday
    REPEAT_DAYS_WEEKEND -> Days.Weekend
    else -> throw IllegalArgumentException("Invalid repeat days : $days")
}

@RepeatDays
internal fun Days.toRepeatDays(): String = when (this) {
    Days.Sun -> REPEAT_DAYS_SUN
    Days.Mon -> REPEAT_DAYS_MON
    Days.Tue -> REPEAT_DAYS_TUE
    Days.Wed -> REPEAT_DAYS_WED
    Days.Thu -> REPEAT_DAYS_THU
    Days.Fri -> REPEAT_DAYS_FRI
    Days.Sat -> REPEAT_DAYS_SAT
    Days.Default -> REPEAT_DAYS_DAY
    Days.Weekday -> REPEAT_DAYS_WEEKDAY
    Days.Weekend -> REPEAT_DAYS_WEEKEND
}