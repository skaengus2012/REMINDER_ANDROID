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

import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FIFTH
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FIRST
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FOURTH
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_LAST
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_SECOND
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_THIRD
import com.nlab.reminder.core.local.database.entity.RepeatDayOrder

/**
 * @author Thalys
 */
internal fun DaysOfWeekOrder(@RepeatDayOrder order: String): DaysOfWeekOrder = when (order) {
    REPEAT_DAY_ORDER_FIRST -> DaysOfWeekOrder.First
    REPEAT_DAY_ORDER_SECOND -> DaysOfWeekOrder.Second
    REPEAT_DAY_ORDER_THIRD -> DaysOfWeekOrder.Third
    REPEAT_DAY_ORDER_FOURTH -> DaysOfWeekOrder.Fourth
    REPEAT_DAY_ORDER_FIFTH -> DaysOfWeekOrder.Fifth
    REPEAT_DAY_ORDER_LAST -> DaysOfWeekOrder.Last
    else -> throw IllegalArgumentException("Invalid repeat day order : $order")
}

@RepeatDayOrder
internal fun DaysOfWeekOrder.toRepeatDayOrder(): String = when (this) {
    DaysOfWeekOrder.First -> REPEAT_DAY_ORDER_FIRST
    DaysOfWeekOrder.Second -> REPEAT_DAY_ORDER_SECOND
    DaysOfWeekOrder.Third -> REPEAT_DAY_ORDER_THIRD
    DaysOfWeekOrder.Fourth -> REPEAT_DAY_ORDER_FOURTH
    DaysOfWeekOrder.Fifth -> REPEAT_DAY_ORDER_FIFTH
    DaysOfWeekOrder.Last -> REPEAT_DAY_ORDER_LAST
}