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

import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.toNonEmptySet
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.local.database.model.REPEAT_DAILY
import com.nlab.reminder.core.local.database.model.REPEAT_HOURLY
import com.nlab.reminder.core.local.database.model.REPEAT_MONTHLY
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_MONTHLY_DAY
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_WEEKLY
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_YEARLY_MONTH
import com.nlab.reminder.core.local.database.model.REPEAT_SETTING_PROPERTY_ZONE_ID
import com.nlab.reminder.core.local.database.model.REPEAT_WEEKLY
import com.nlab.reminder.core.local.database.model.REPEAT_YEARLY
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.RepeatType
import kotlinx.datetime.TimeZone

/**
 * @author Thalys
 */
internal fun Repeat(
    @RepeatType type: String,
    interval: Int,
    detailEntities: Collection<RepeatDetailEntity>
): Repeat {
    val intervalAsPositiveInt = interval.toPositiveInt()
    return when (type) {
        REPEAT_HOURLY -> Repeat.Hourly(intervalAsPositiveInt)
        REPEAT_DAILY -> Repeat.Daily(intervalAsPositiveInt)
        REPEAT_WEEKLY -> RepeatWeekly(intervalAsPositiveInt, detailEntities)
        REPEAT_MONTHLY -> RepeatMonthly(intervalAsPositiveInt, detailEntities)
        REPEAT_YEARLY -> RepeatYearly(intervalAsPositiveInt, detailEntities)
        else -> throw IllegalArgumentException("Invalid repeat type : $type")
    }
}

@Suppress("FunctionName")
private fun RepeatWeekly(
    interval: PositiveInt,
    repeatDetailEntities: Collection<RepeatDetailEntity>,
): Repeat.Weekly {
    val settingToValuesTables = repeatDetailEntities.groupBy(
        keySelector = { it.propertyCode },
        valueTransform = { it.value }
    )
    return Repeat.Weekly(
        interval,
        timeZone = settingToValuesTables.getTimeZone(),
        daysOfWeeks = settingToValuesTables
            .getValue(REPEAT_SETTING_PROPERTY_WEEKLY)
            .toSet(::DayOfWeek)
    )
}

@Suppress("FunctionName")
private fun RepeatMonthly(
    interval: PositiveInt,
    repeatDetailEntities: Collection<RepeatDetailEntity>,
): Repeat.Monthly {
    val propertyCodeToValuesTables = repeatDetailEntities.groupBy(
        keySelector = { it.propertyCode },
        valueTransform = { it.value }
    )
    val hasDaysOfMonth = REPEAT_SETTING_PROPERTY_MONTHLY_DAY in propertyCodeToValuesTables
    return Repeat.Monthly(
        interval,
        timeZone = propertyCodeToValuesTables.getTimeZone(),
        detail = if (hasDaysOfMonth) {
            MonthlyRepeatDetail.Each(
                days = propertyCodeToValuesTables
                    .getValue(REPEAT_SETTING_PROPERTY_MONTHLY_DAY)
                    .map { DaysOfMonth(it.toInt()) }
                    .toNonEmptySet()
            )
        } else {
            MonthlyRepeatDetail.Customize(
                order = propertyCodeToValuesTables
                    .getValue(REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER)
                    .first()
                    .let(::DaysOfWeekOrder),
                day = propertyCodeToValuesTables
                    .getValue(REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK)
                    .first()
                    .let(::Days)
            )
        }
    )
}

private fun RepeatYearly(
    interval: PositiveInt,
    repeatDetailEntities: Collection<RepeatDetailEntity>,
): Repeat.Yearly {
    val propertyCodeToValuesTables = repeatDetailEntities.groupBy(
        keySelector = { it.propertyCode },
        valueTransform = { it.value }
    )
    return Repeat.Yearly(
        interval = interval,
        timeZone = propertyCodeToValuesTables.getTimeZone(),
        month = propertyCodeToValuesTables
            .getValue(REPEAT_SETTING_PROPERTY_YEARLY_MONTH)
            .map { Month(it) }
            .toNonEmptySet(),
        daysOfWeek = propertyCodeToValuesTables[REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER]
            ?.first()
            ?.let(::DaysOfWeekOrder)
            ?.let { order ->
                YearlyDaysOfWeekOption(
                    order = order,
                    day = propertyCodeToValuesTables.getValue(REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK)
                        .first()
                        .let(::Days)
                )
            }
    )
}

private fun Map<String, List<String>>.getTimeZone(): TimeZone = getValue(REPEAT_SETTING_PROPERTY_ZONE_ID)
    .first()
    .let(TimeZone::of)