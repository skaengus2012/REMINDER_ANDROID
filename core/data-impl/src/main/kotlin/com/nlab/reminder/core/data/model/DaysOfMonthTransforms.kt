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

import androidx.annotation.IntRange

/**
 * @author Thalys
 */
internal fun DaysOfMonth(@IntRange(from = 1, to = 31) day: Int): DaysOfMonth = when (day) {
    1 -> DaysOfMonth.DAY_1
    2 -> DaysOfMonth.DAY_2
    3 -> DaysOfMonth.DAY_3
    4 -> DaysOfMonth.DAY_4
    5 -> DaysOfMonth.DAY_5
    6 -> DaysOfMonth.DAY_6
    7 -> DaysOfMonth.DAY_7
    8 -> DaysOfMonth.DAY_8
    9 -> DaysOfMonth.DAY_9
    10 -> DaysOfMonth.DAY_10
    11 -> DaysOfMonth.DAY_11
    12 -> DaysOfMonth.DAY_12
    13 -> DaysOfMonth.DAY_13
    14 -> DaysOfMonth.DAY_14
    15 -> DaysOfMonth.DAY_15
    16 -> DaysOfMonth.DAY_16
    17 -> DaysOfMonth.DAY_17
    18 -> DaysOfMonth.DAY_18
    19 -> DaysOfMonth.DAY_19
    20 -> DaysOfMonth.DAY_20
    21 -> DaysOfMonth.DAY_21
    22 -> DaysOfMonth.DAY_22
    23 -> DaysOfMonth.DAY_23
    24 -> DaysOfMonth.DAY_24
    25 -> DaysOfMonth.DAY_25
    26 -> DaysOfMonth.DAY_26
    27 -> DaysOfMonth.DAY_27
    28 -> DaysOfMonth.DAY_28
    29 -> DaysOfMonth.DAY_29
    30 -> DaysOfMonth.DAY_30
    31 -> DaysOfMonth.DAY_31
    else -> throw IllegalArgumentException("Invalid repeat day of month : $day")
}

@get:IntRange(from = 1, to = 31)
internal val DaysOfMonth.rawValue: Int
    get() = when (this) {
        DaysOfMonth.DAY_1 -> 1
        DaysOfMonth.DAY_2 -> 2
        DaysOfMonth.DAY_3 -> 3
        DaysOfMonth.DAY_4 -> 4
        DaysOfMonth.DAY_5 -> 5
        DaysOfMonth.DAY_6 -> 6
        DaysOfMonth.DAY_7 -> 7
        DaysOfMonth.DAY_8 -> 8
        DaysOfMonth.DAY_9 -> 9
        DaysOfMonth.DAY_10 -> 10
        DaysOfMonth.DAY_11 -> 11
        DaysOfMonth.DAY_12 -> 12
        DaysOfMonth.DAY_13 -> 13
        DaysOfMonth.DAY_14 -> 14
        DaysOfMonth.DAY_15 -> 15
        DaysOfMonth.DAY_16 -> 16
        DaysOfMonth.DAY_17 -> 17
        DaysOfMonth.DAY_18 -> 18
        DaysOfMonth.DAY_19 -> 19
        DaysOfMonth.DAY_20 -> 20
        DaysOfMonth.DAY_21 -> 21
        DaysOfMonth.DAY_22 -> 22
        DaysOfMonth.DAY_23 -> 23
        DaysOfMonth.DAY_24 -> 24
        DaysOfMonth.DAY_25 -> 25
        DaysOfMonth.DAY_26 -> 26
        DaysOfMonth.DAY_27 -> 27
        DaysOfMonth.DAY_28 -> 28
        DaysOfMonth.DAY_29 -> 29
        DaysOfMonth.DAY_30 -> 30
        DaysOfMonth.DAY_31 -> 31
    }