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

package com.nlab.reminder.core.local.database.entity

import androidx.annotation.StringDef

/**
 * @author Thalys
 */
@StringDef(
    value = [
        REPEAT_MONTH_JAN,
        REPEAT_MONTH_FEB,
        REPEAT_MONTH_MAR,
        REPEAT_MONTH_APR,
        REPEAT_MONTH_MAY,
        REPEAT_MONTH_JUN,
        REPEAT_MONTH_JUL,
        REPEAT_MONTH_AUG,
        REPEAT_MONTH_SEP,
        REPEAT_MONTH_OCT,
        REPEAT_MONTH_NOV,
        REPEAT_MONTH_DEC
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class RepeatMonth

const val REPEAT_MONTH_JAN = "REPEAT_MONTH_JAN"
const val REPEAT_MONTH_FEB = "REPEAT_MONTH_FEB"
const val REPEAT_MONTH_MAR = "REPEAT_MONTH_MAR"
const val REPEAT_MONTH_APR = "REPEAT_MONTH_APR"
const val REPEAT_MONTH_MAY = "REPEAT_MONTH_MAY"
const val REPEAT_MONTH_JUN = "REPEAT_MONTH_JUN"
const val REPEAT_MONTH_JUL = "REPEAT_MONTH_JUL"
const val REPEAT_MONTH_AUG = "REPEAT_MONTH_AUG"
const val REPEAT_MONTH_SEP = "REPEAT_MONTH_SEP"
const val REPEAT_MONTH_OCT = "REPEAT_MONTH_OCT"
const val REPEAT_MONTH_NOV = "REPEAT_MONTH_NOV"
const val REPEAT_MONTH_DEC = "REPEAT_MONTH_DEC"
