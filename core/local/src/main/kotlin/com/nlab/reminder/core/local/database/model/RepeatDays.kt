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

package com.nlab.reminder.core.local.database.model

import androidx.annotation.StringDef

/**
 * @author Thalys
 */
@StringDef(
    value = [
        REPEAT_DAYS_SUN,
        REPEAT_DAYS_MON,
        REPEAT_DAYS_TUE,
        REPEAT_DAYS_WED,
        REPEAT_DAYS_THU,
        REPEAT_DAYS_FRI,
        REPEAT_DAYS_SAT,
        REPEAT_DAYS_DAY,
        REPEAT_DAYS_WEEKDAY,
        REPEAT_DAYS_WEEKEND
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class RepeatDays

const val REPEAT_DAYS_SUN = "REPEAT_DAYS_SUN"
const val REPEAT_DAYS_MON = "REPEAT_DAYS_MON"
const val REPEAT_DAYS_TUE = "REPEAT_DAYS_TUE"
const val REPEAT_DAYS_WED = "REPEAT_DAYS_WED"
const val REPEAT_DAYS_THU = "REPEAT_DAYS_THU"
const val REPEAT_DAYS_FRI = "REPEAT_DAYS_FRI"
const val REPEAT_DAYS_SAT = "REPEAT_DAYS_SAT"
const val REPEAT_DAYS_DAY = "REPEAT_DAYS_DAY"
const val REPEAT_DAYS_WEEKDAY = "REPEAT_DAYS_WEEKDAY"
const val REPEAT_DAYS_WEEKEND = "REPEAT_DAYS_WEEKEND"