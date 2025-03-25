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
        REPEAT_WEEK_SUN,
        REPEAT_WEEK_MON,
        REPEAT_WEEK_TUE,
        REPEAT_WEEK_WED,
        REPEAT_WEEK_THU,
        REPEAT_WEEK_FRI,
        REPEAT_WEEK_SAT
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class RepeatWeek

const val REPEAT_WEEK_SUN = "REPEAT_WEEK_SUN"
const val REPEAT_WEEK_MON = "REPEAT_WEEK_MON"
const val REPEAT_WEEK_TUE = "REPEAT_WEEK_TUE"
const val REPEAT_WEEK_WED = "REPEAT_WEEK_WED"
const val REPEAT_WEEK_THU = "REPEAT_WEEK_THU"
const val REPEAT_WEEK_FRI = "REPEAT_WEEK_FRI"
const val REPEAT_WEEK_SAT = "REPEAT_WEEK_SAT"