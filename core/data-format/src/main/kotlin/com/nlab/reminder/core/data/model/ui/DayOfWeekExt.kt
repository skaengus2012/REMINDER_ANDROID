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

package com.nlab.reminder.core.data.model.ui

import androidx.annotation.StringRes
import com.nlab.reminder.core.translation.StringIds
import kotlinx.datetime.DayOfWeek

/**
 * @author Doohyun
 */
@get:StringRes
internal val DayOfWeek.resourceId: Int
    get() = when (this) {
        DayOfWeek.SUNDAY -> StringIds.sunday
        DayOfWeek.MONDAY -> StringIds.monday
        DayOfWeek.TUESDAY -> StringIds.tuesday
        DayOfWeek.WEDNESDAY -> StringIds.wednesday
        DayOfWeek.THURSDAY -> StringIds.thursday
        DayOfWeek.FRIDAY -> StringIds.friday
        DayOfWeek.SATURDAY -> StringIds.saturday
    }

internal val sundayFirstComparator = object : Comparator<DayOfWeek> {
    private val dayOfWeekToOrderTable = mapOf(
        DayOfWeek.SUNDAY to 1,
        DayOfWeek.MONDAY to 2,
        DayOfWeek.TUESDAY to 3,
        DayOfWeek.WEDNESDAY to 4,
        DayOfWeek.THURSDAY to 5,
        DayOfWeek.FRIDAY to 6,
        DayOfWeek.SATURDAY to 7
    )

    override fun compare(o1: DayOfWeek?, o2: DayOfWeek?): Int {
        if (o1 == null && o2 == null) return 0
        if (o1 == null) return 1   // nulls last
        if (o2 == null) return -1

        val order1 = dayOfWeekToOrderTable[o1] ?: Int.MAX_VALUE
        val order2 = dayOfWeekToOrderTable[o2] ?: Int.MAX_VALUE

        return order1.compareTo(order2)
    }
}