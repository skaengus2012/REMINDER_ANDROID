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
import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.toNonEmptySet
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.local.database.entity.REPEAT_DAILY
import com.nlab.reminder.core.local.database.entity.REPEAT_HOURLY
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTHLY
import com.nlab.reminder.core.local.database.entity.REPEAT_SETTING_PROPERTY_MONTHLY_DAY
import com.nlab.reminder.core.local.database.entity.REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK
import com.nlab.reminder.core.local.database.entity.REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER
import com.nlab.reminder.core.local.database.entity.REPEAT_SETTING_PROPERTY_WEEKLY
import com.nlab.reminder.core.local.database.entity.REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK
import com.nlab.reminder.core.local.database.entity.REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER
import com.nlab.reminder.core.local.database.entity.REPEAT_SETTING_PROPERTY_YEARLY_MONTH
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEKLY
import com.nlab.reminder.core.local.database.entity.REPEAT_YEARLY
import com.nlab.reminder.core.local.database.entity.RepeatDetailEntity
import com.nlab.reminder.core.local.database.entity.RepeatType
import com.nlab.reminder.core.local.database.transaction.ScheduleRepeatDetailAggregate
import com.nlab.reminder.core.local.database.transaction.ScheduleRepeatAggregate

/**
 * @author Thalys
 */
internal fun Repeat(
    @RepeatType type: String,
    @IntRange(from = 1) interval: Int,
    detailEntities: Collection<RepeatDetailEntity>
): Repeat {
    val intervalAsPositiveInt = interval.toPositiveInt()

    // When it is tied with {when}, Missed Branch will be created in Jacoco Coverage.
    // So it was treated with an if-return pattern.
    if (type == REPEAT_HOURLY) {
        return Repeat.Hourly(intervalAsPositiveInt)
    }

    if (type == REPEAT_DAILY) {
        return Repeat.Daily(intervalAsPositiveInt)
    }

    if (type == REPEAT_WEEKLY) {
        return RepeatWeekly(intervalAsPositiveInt, detailEntities)
    }

    if (type == REPEAT_MONTHLY) {
        return RepeatMonthly(intervalAsPositiveInt, detailEntities)
    }

    if (type == REPEAT_YEARLY) {
        return RepeatYearly(intervalAsPositiveInt, detailEntities)
    }

    throw IllegalArgumentException("Invalid repeat type : $type")
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
        daysOfWeeks = settingToValuesTables
            .getValue(REPEAT_SETTING_PROPERTY_WEEKLY)
            .map(::DayOfWeek)
            .toNonEmptySet()
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

@Suppress("FunctionName")
private fun RepeatYearly(
    interval: PositiveInt,
    repeatDetailEntities: Collection<RepeatDetailEntity>,
): Repeat.Yearly {
    val propertyCodeToValuesTables = repeatDetailEntities.groupBy(
        keySelector = { it.propertyCode },
        valueTransform = { it.value }
    )
    val yearlyDayOrder = propertyCodeToValuesTables[REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER]?.firstOrNull()
    val yearlyDayOfWeeks = propertyCodeToValuesTables[REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK]?.firstOrNull()
    return Repeat.Yearly(
        interval = interval,
        months = propertyCodeToValuesTables
            .getValue(REPEAT_SETTING_PROPERTY_YEARLY_MONTH)
            .map { Month(it) }
            .toNonEmptySet(),
        daysOfWeekOption = when {
            yearlyDayOrder == null && yearlyDayOfWeeks == null -> {
                null
            }
            yearlyDayOrder != null && yearlyDayOfWeeks != null -> {
                YearlyDaysOfWeekOption(
                    order = DaysOfWeekOrder(yearlyDayOrder),
                    day = Days(yearlyDayOfWeeks)
                )
            }
            else -> throw IllegalArgumentException("Invalid yearly week option")
        }
    )
}

internal fun Repeat.toAggregate(): ScheduleRepeatAggregate = when (this) {
    is Repeat.Hourly -> {
        ScheduleRepeatAggregate(
            type = REPEAT_HOURLY,
            interval = interval,
            details = emptySet()
        )
    }

    is Repeat.Daily -> {
        ScheduleRepeatAggregate(
            type = REPEAT_DAILY,
            interval = interval,
            details = emptySet()
        )
    }

    is Repeat.Weekly -> {
        ScheduleRepeatAggregate(
            type = REPEAT_WEEKLY,
            interval = interval,
            details = convertRepeatDetailAggregatesFromWeeklyRepeat(repeat = this)
        )
    }

    is Repeat.Monthly -> {
        ScheduleRepeatAggregate(
            type = REPEAT_MONTHLY,
            interval = interval,
            details = convertRepeatDetailAggregatesFromMonthlyRepeat(repeat = this)
        )
    }

    is Repeat.Yearly -> {
        ScheduleRepeatAggregate(
            type = REPEAT_YEARLY,
            interval = interval,
            details = convertRepeatDetailAggregatesFromYearlyRepeat(repeat = this)
        )
    }

}

private fun convertRepeatDetailAggregatesFromWeeklyRepeat(
    repeat: Repeat.Weekly
): Set<ScheduleRepeatDetailAggregate> = buildSet {
    repeat.daysOfWeeks.value.forEach { daysOfWeek ->
        this += ScheduleRepeatDetailAggregate(
            propertyCode = REPEAT_SETTING_PROPERTY_WEEKLY,
            value = daysOfWeek.toRepeatWeek()
        )
    }
}

private fun convertRepeatDetailAggregatesFromMonthlyRepeat(
    repeat: Repeat.Monthly
): Set<ScheduleRepeatDetailAggregate> = buildSet {
    when (val typedDetail = repeat.detail) {
        is MonthlyRepeatDetail.Each -> {
            typedDetail.days.value.forEach { daysOfMonth ->
                this += ScheduleRepeatDetailAggregate(
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY,
                    value = daysOfMonth.rawValue.toString()
                )
            }
        }

        is MonthlyRepeatDetail.Customize -> {
            this += ScheduleRepeatDetailAggregate(
                propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER,
                value = typedDetail.order.toRepeatDayOrder()
            )
            this += ScheduleRepeatDetailAggregate(
                propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK,
                value = typedDetail.day.toRepeatDays()
            )
        }
    }
}

private fun convertRepeatDetailAggregatesFromYearlyRepeat(
    repeat: Repeat.Yearly
): Set<ScheduleRepeatDetailAggregate> = buildSet {
    repeat.months.value.forEach { month ->
        this += ScheduleRepeatDetailAggregate(
            propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH,
            value = month.toRepeatMonth()
        )
    }
    repeat.daysOfWeekOption?.let { option ->
        this += ScheduleRepeatDetailAggregate(
            propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER,
            value = option.order.toRepeatDayOrder()
        )
        this += ScheduleRepeatDetailAggregate(
            propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK,
            value = option.day.toRepeatDays()
        )
    }
}