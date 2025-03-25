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
private val valueToDayOfMonthTable: Map<Int, DaysOfMonth> = DaysOfMonth.entries.associateBy { it.day }

fun DaysOfMonth(@IntRange(from = 1, to = 31) day: Int): DaysOfMonth {
    require(day in DaysOfMonth.DAY_1.day..DaysOfMonth.DAY_31.day)
    return valueToDayOfMonthTable.getValue(day)
}