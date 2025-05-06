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

import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_APR
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_AUG
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_DEC
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_FEB
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_JAN
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_JUL
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_JUN
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_MAR
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_MAY
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_NOV
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_OCT
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_SEP
import com.nlab.reminder.core.local.database.entity.RepeatMonth
import kotlinx.datetime.Month

/**
 * @author Doohyun
 */
fun Month(@RepeatMonth month: String): Month = when (month) {
    REPEAT_MONTH_JAN -> Month.JANUARY
    REPEAT_MONTH_FEB -> Month.FEBRUARY
    REPEAT_MONTH_MAR -> Month.MARCH
    REPEAT_MONTH_APR -> Month.APRIL
    REPEAT_MONTH_MAY -> Month.MAY
    REPEAT_MONTH_JUN -> Month.JUNE
    REPEAT_MONTH_JUL -> Month.JULY
    REPEAT_MONTH_AUG -> Month.AUGUST
    REPEAT_MONTH_SEP -> Month.SEPTEMBER
    REPEAT_MONTH_OCT -> Month.OCTOBER
    REPEAT_MONTH_NOV -> Month.NOVEMBER
    REPEAT_MONTH_DEC -> Month.DECEMBER
    else -> throw IllegalArgumentException("Invalid repeat month : $month")
}

@RepeatMonth
fun Month.toRepeatMonth(): String = when (this) {
    Month.JANUARY -> REPEAT_MONTH_JAN
    Month.FEBRUARY -> REPEAT_MONTH_FEB
    Month.MARCH -> REPEAT_MONTH_MAR
    Month.APRIL -> REPEAT_MONTH_APR
    Month.MAY -> REPEAT_MONTH_MAY
    Month.JUNE -> REPEAT_MONTH_JUN
    Month.JULY -> REPEAT_MONTH_JUL
    Month.AUGUST -> REPEAT_MONTH_AUG
    Month.SEPTEMBER -> REPEAT_MONTH_SEP
    Month.OCTOBER -> REPEAT_MONTH_OCT
    Month.NOVEMBER -> REPEAT_MONTH_NOV
    Month.DECEMBER -> REPEAT_MONTH_DEC
}