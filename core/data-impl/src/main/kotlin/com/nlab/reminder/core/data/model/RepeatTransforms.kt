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
import com.nlab.reminder.core.local.database.model.RepeatDTO
import com.nlab.reminder.core.local.database.model.RepeatDetailDTO
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.RepeatType
import kotlinx.datetime.TimeZone

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
        timeZone = settingToValuesTables.getTimeZone(),
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
        timeZone = propertyCodeToValuesTables.getTimeZone(),
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

private fun Map<String, List<String>>.getTimeZone(): TimeZone = getValue(REPEAT_SETTING_PROPERTY_ZONE_ID)
    .first()
    .let(TimeZone::of)

internal fun Repeat.toDTO(): RepeatDTO = when (this) {
    is Repeat.Hourly -> {
        RepeatDTO(
            type = REPEAT_HOURLY,
            interval = interval,
            details = emptySet()
        )
    }

    is Repeat.Daily -> {
        RepeatDTO(
            type = REPEAT_DAILY,
            interval = interval,
            details = emptySet()
        )
    }

    is Repeat.Weekly -> {
        RepeatDTO(
            type = REPEAT_WEEKLY,
            interval = interval,
            details = convertRepeatDetailDTOsFromWeeklyRepeat(repeat = this)
        )
    }

    is Repeat.Monthly -> {
        RepeatDTO(
            type = REPEAT_MONTHLY,
            interval = interval,
            details = convertRepeatDetailDTOsFromMonthlyRepeat(repeat = this)
        )
    }

    is Repeat.Yearly -> {
        RepeatDTO(
            type = REPEAT_YEARLY,
            interval = interval,
            details = convertRepeatDetailDTOsFromYearlyRepeat(repeat = this)
        )
    }

}

private fun convertRepeatDetailDTOsFromWeeklyRepeat(repeat: Repeat.Weekly): Set<RepeatDetailDTO> = buildSet {
    this += RepeatDetailDTO(
        propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
        value = repeat.timeZone.id
    )
    repeat.daysOfWeeks.value.forEach { daysOfWeek ->
        this += RepeatDetailDTO(
            propertyCode = REPEAT_SETTING_PROPERTY_WEEKLY,
            value = daysOfWeek.toRepeatWeek()
        )
    }
}

private fun convertRepeatDetailDTOsFromMonthlyRepeat(repeat: Repeat.Monthly): Set<RepeatDetailDTO> = buildSet {
    this += RepeatDetailDTO(
        propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
        value = repeat.timeZone.id
    )
    when (val typedDetail = repeat.detail) {
        is MonthlyRepeatDetail.Each -> {
            typedDetail.days.value.forEach { daysOfMonth ->
                this += RepeatDetailDTO(
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY,
                    value = daysOfMonth.rawValue.toString()
                )
            }
        }

        is MonthlyRepeatDetail.Customize -> {
            this += RepeatDetailDTO(
                propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER,
                value = typedDetail.order.toRepeatDayOrder()
            )
            this += RepeatDetailDTO(
                propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK,
                value = typedDetail.day.toRepeatDays()
            )
        }
    }
}

private fun convertRepeatDetailDTOsFromYearlyRepeat(repeat: Repeat.Yearly): Set<RepeatDetailDTO> = buildSet {
    this += RepeatDetailDTO(
        propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
        value = repeat.timeZone.id
    )
    repeat.months.value.forEach { month ->
        this += RepeatDetailDTO(
            propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH,
            value = month.toRepeatMonth()
        )
    }
    repeat.daysOfWeekOption?.let { option ->
        this += RepeatDetailDTO(
            propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER,
            value = option.order.toRepeatDayOrder()
        )
        this += RepeatDetailDTO(
            propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK,
            value = option.day.toRepeatDays()
        )
    }
}