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

package com.nlab.reminder.core.local.database.transaction

import com.nlab.reminder.core.kotlin.collections.NonEmptySet
import com.nlab.reminder.core.kotlin.collections.tryToNonEmptySetOrNull
import com.nlab.reminder.core.local.database.entity.*
import kotlinx.datetime.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verify rules Contract3 of [ScheduleEntity] and [RepeatDetailEntity].
 *
 * @author Thalys
 */
@Singleton
internal class ScheduleTimingAggregateValidator @Inject constructor() {
    private val allRepeatWeeks = setOf(
        REPEAT_WEEK_SUN,
        REPEAT_WEEK_MON,
        REPEAT_WEEK_TUE,
        REPEAT_WEEK_WED,
        REPEAT_WEEK_THU,
        REPEAT_WEEK_FRI,
        REPEAT_WEEK_SAT
    )
    private val allRepeatDayOrders = setOf(
        REPEAT_DAY_ORDER_FIRST,
        REPEAT_DAY_ORDER_SECOND,
        REPEAT_DAY_ORDER_THIRD,
        REPEAT_DAY_ORDER_FOURTH,
        REPEAT_DAY_ORDER_FIFTH,
        REPEAT_DAY_ORDER_LAST
    )
    private val allRepeatDays = setOf(
        REPEAT_DAYS_SUN,
        REPEAT_DAYS_MON,
        REPEAT_DAYS_TUE,
        REPEAT_DAYS_WED,
        REPEAT_DAYS_THU,
        REPEAT_DAYS_FRI,
        REPEAT_DAYS_SAT,
        REPEAT_DAYS_DAY,
        REPEAT_DAYS_WEEKDAY,
        REPEAT_DAYS_WEEKEND
    )
    private val allRepeatMonths = setOf(
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
    )

    fun validate(timingAggregate: ScheduleTimingAggregate) {
        timingAggregate.repeat?.let { ensureRepeatValidation(it) }
    }

    private fun ensureRepeatValidation(repeatAggregate: ScheduleRepeatAggregate) {
        when (val repeatCode = repeatAggregate.type) {
            REPEAT_HOURLY,
            REPEAT_DAILY -> {
                require(repeatAggregate.details.isEmpty())
            }

            REPEAT_WEEKLY -> {
                ensureWeeklyRepeatDetails(repeatAggregate.details)
            }

            REPEAT_MONTHLY -> {
                ensureMonthlyRepeatDetails(repeatAggregate.details)
            }

            REPEAT_YEARLY -> {
                ensureYearlyRepeatDetails(repeatAggregate.details)
            }

            else -> {
                throw IllegalArgumentException(
                    "Invalid RepeatType(Inputted repeat code was [$repeatCode]). Please check RepeatType annotation"
                )
            }
        }
    }

    private fun ensureAndGetValidZoneIdRepeatDetail(
        repeatDetailAggregates: Set<ScheduleRepeatDetailAggregate>
    ): ScheduleRepeatDetailAggregate {
        val zoneIdRepeatDetail = repeatDetailAggregates.find { it.propertyCode == REPEAT_SETTING_PROPERTY_ZONE_ID }
        requireNotNull(zoneIdRepeatDetail) { "Cannot found timeZone Value" }
        TimeZone.of(zoneId = zoneIdRepeatDetail.value) // check, zoneId is valid
        return zoneIdRepeatDetail
    }

    private fun ensureWeeklyRepeatDetails(weeklyRepeatDetailAggregates: Set<ScheduleRepeatDetailAggregate>) {
        val zoneIdAggregate = ensureAndGetValidZoneIdRepeatDetail(weeklyRepeatDetailAggregates)
        val weeklyAggregates = weeklyRepeatDetailAggregates - zoneIdAggregate
        require(weeklyAggregates.isNotEmpty()) { "At least one RepeatWeek must exist" }

        require(weeklyAggregates.all { dto ->
            dto.propertyCode == REPEAT_SETTING_PROPERTY_WEEKLY && dto.value in allRepeatWeeks
        }) { "Invalid repeatWeek inputted" }
    }

    private fun ensureMonthlyRepeatDetails(monthlyRepeatDetailAggregates: Set<ScheduleRepeatDetailAggregate>) {
        val zoneIdAggregate = ensureAndGetValidZoneIdRepeatDetail(monthlyRepeatDetailAggregates)
        val monthlyAggregates = (monthlyRepeatDetailAggregates - zoneIdAggregate).tryToNonEmptySetOrNull()
        requireNotNull(monthlyAggregates) { "Monthly repeat detail not existed" }

        if (monthlyAggregates.value.any { it.propertyCode == REPEAT_SETTING_PROPERTY_MONTHLY_DAY }) {
            ensureMonthlyEachRepeatDetails(monthlyAggregates)
        } else {
            ensureDayOfWeekRepeatDetails(
                monthlyAggregates,
                dayOrderCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER,
                dayOfWeekCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK
            )
        }
    }

    private fun ensureMonthlyEachRepeatDetails(
        monthlyRepeatDetailAggregates: NonEmptySet<ScheduleRepeatDetailAggregate>
    ) {
        val monthlyDaysLength = (1..31)
        require(monthlyRepeatDetailAggregates.value.all { dto ->
            dto.propertyCode == REPEAT_SETTING_PROPERTY_MONTHLY_DAY && dto.value.toInt() in monthlyDaysLength
        }) { "Invalid monthly day inputted" }
    }

    private fun ensureDayOfWeekRepeatDetails(
        dayOfWeekRepeatDetailAggregates: NonEmptySet<ScheduleRepeatDetailAggregate>,
        dayOrderCode: String,
        dayOfWeekCode: String
    ) {
        require(dayOfWeekRepeatDetailAggregates.value.size == 2) {
            "Monthly repeat detail must have 2 properties"
        }

        val dayOrderAggregate = dayOfWeekRepeatDetailAggregates
            .value
            .find { it.propertyCode == dayOrderCode }
        requireNotNull(dayOrderAggregate) { "Day order must exist" }
        require(dayOrderAggregate.value in allRepeatDayOrders) { "Invalid day order inputted" }

        val dayOfWeeksAggregate = dayOfWeekRepeatDetailAggregates
            .value
            .find { it.propertyCode == dayOfWeekCode }
        requireNotNull(dayOfWeeksAggregate) { "Day of week must exist" }
        require(dayOfWeeksAggregate.value in allRepeatDays) { "Invalid day of week inputted" }
    }

    private fun ensureYearlyRepeatDetails(yearlyRepeatDetailAggregates: Set<ScheduleRepeatDetailAggregate>) {
        val zoneIdAggregate = ensureAndGetValidZoneIdRepeatDetail(yearlyRepeatDetailAggregates)
        val yearlyAggregates = yearlyRepeatDetailAggregates - zoneIdAggregate
        require(yearlyAggregates.isNotEmpty()) { "Yearly repeat detail not existed" }

        val yearlyMonthAggregates =
            yearlyAggregates.filter { it.propertyCode == REPEAT_SETTING_PROPERTY_YEARLY_MONTH }.toSet()
        require(yearlyMonthAggregates.isNotEmpty()) { "Yearly month repeat detail not existed" }
        require(yearlyMonthAggregates.all { it.value in allRepeatMonths }) { "Invalid yearly month inputted" }

        val yearlyDayOfWeekOptionAggregates = (yearlyAggregates - yearlyMonthAggregates).tryToNonEmptySetOrNull()
        if (yearlyDayOfWeekOptionAggregates != null) {
            ensureDayOfWeekRepeatDetails(
                yearlyDayOfWeekOptionAggregates,
                dayOrderCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER,
                dayOfWeekCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK
            )
        }
    }
}