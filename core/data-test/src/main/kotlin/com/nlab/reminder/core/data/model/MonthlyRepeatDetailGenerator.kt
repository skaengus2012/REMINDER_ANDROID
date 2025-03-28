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

import com.nlab.reminder.core.kotlin.collections.NonEmptySet
import com.nlab.reminder.core.kotlin.collections.toNonEmptySet
import com.nlab.testkit.faker.genInt

/**
 * @author Thalys
 */
fun genMonthlyRepeatDetail(): MonthlyRepeatDetail =
    if (genInt() % 2 == 0) genMonthlyRepeatDetailEach()
    else genMonthlyRepeatCustomize()

fun genMonthlyRepeatDetailEach(
    days: NonEmptySet<DaysOfMonth> = DaysOfMonth.entries
        .shuffled()
        .let { daysOfMonths -> daysOfMonths.take(genInt(min = 1, max = daysOfMonths.size)) }
        .toNonEmptySet()
): MonthlyRepeatDetail.Each = MonthlyRepeatDetail.Each(days = days)

fun genMonthlyRepeatCustomize(
    order: DaysOfWeekOrder = DaysOfWeekOrder.entries.random(),
    day: Days = Days.entries.random()
): MonthlyRepeatDetail.Customize = MonthlyRepeatDetail.Customize(order = order, day = day)