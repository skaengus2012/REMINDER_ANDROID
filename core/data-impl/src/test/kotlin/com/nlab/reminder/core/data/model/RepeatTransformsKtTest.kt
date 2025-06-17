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

import com.nlab.reminder.core.kotlin.collections.toNonEmptySet
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genPositiveInt
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
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_DAY
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_FRI
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_MON
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_SAT
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_SUN
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_THU
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_TUE
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_WED
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_WEEKDAY
import com.nlab.reminder.core.local.database.entity.REPEAT_DAYS_WEEKEND
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FIFTH
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FIRST
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FOURTH
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_LAST
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_SECOND
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_THIRD
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_FRI
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_MON
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_SAT
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_SUN
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_THU
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_TUE
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_WED
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_APR
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_AUG
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_DEC
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_FEB
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_JAN
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_JUL
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_JUN
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_MAR
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_MAY
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_NOV
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_OCT
import com.nlab.reminder.core.local.database.entity.REPEAT_MONTH_SEP
import com.nlab.reminder.core.local.database.entity.RepeatDetailEntity
import com.nlab.reminder.core.local.database.transaction.ScheduleRepeatDetailAggregate
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.requireSample
import com.nlab.testkit.faker.shuffledSubset
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class RepeatTransformsKtTest {
    private val sampleDaysOfWeekCodes: Set<String> = listOf(
        REPEAT_WEEK_SUN,
        REPEAT_WEEK_MON,
        REPEAT_WEEK_TUE,
        REPEAT_WEEK_WED,
        REPEAT_WEEK_THU,
        REPEAT_WEEK_FRI,
        REPEAT_WEEK_SAT
    ).shuffledSubset()

    private val sampleDayOrderCode: String = listOf(
        REPEAT_DAY_ORDER_FIRST,
        REPEAT_DAY_ORDER_SECOND,
        REPEAT_DAY_ORDER_THIRD,
        REPEAT_DAY_ORDER_FOURTH,
        REPEAT_DAY_ORDER_FIFTH,
        REPEAT_DAY_ORDER_LAST
    ).requireSample()

    private val sampleDaysCode: String = listOf(
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
    ).requireSample()

    private val sampleYearlyMonthCodes: Set<String> = listOf(
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
    ).shuffledSubset()

    /**
    @Test
    fun `Given type, interval are null, When transform to Repeat, Then return null`() {
        val type = null
        val interval = null
        val actualRepeat = Repeat(
            type = type,
            interval = interval,
            detailEntities = emptySet()
        )
        assertThat(actualRepeat, nullValue())
    }*/

    @Test(expected = IllegalArgumentException::class)
    fun `Given invalid repeat type, When creating Repeat, Then throw exception`() {
        val repeatType = "INVALID_REPEAT_TYPE"
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = emptySet()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given non positive interval, When creating Repeat, Then throw exception`() {
        val interval = genInt(min = -9999, max = 0)
        Repeat(
            type = REPEAT_HOURLY,
            interval = interval,
            detailEntities = emptySet()
        )
    }

    @Test
    fun `Given hourly, positive number, When creating Repeat, Then return Hourly`() {
        val repeatType = REPEAT_HOURLY
        val interval = genPositiveInt()
        val expectedRepeat = Repeat.Hourly(interval = interval)
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = emptySet()
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test
    fun `Given daily, positive number, When creating Repeat, Then return Daily`() {
        val repeatType = REPEAT_DAILY
        val interval = genPositiveInt()
        val expectedRepeat = Repeat.Daily(interval = interval)
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = emptySet()
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given weekly and empty repeat details, When creating Repeat, Then throw exception`() {
        val repeatType = REPEAT_WEEKLY
        val repeatDetailEntities = emptySet<RepeatDetailEntity>()
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = repeatDetailEntities
        )
    }

    @Test
    fun `Given weekly and dayOfWeeks, When creating Repeat, Then return Weekly`() {
        val repeatType = REPEAT_WEEKLY
        val interval = genPositiveInt()
        val dayOfWeeks = sampleDaysOfWeekCodes
        val expectedRepeat = Repeat.Weekly(
            interval = interval,
            daysOfWeeks = dayOfWeeks.toSet(::DayOfWeek).toNonEmptySet()
        )
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                this += RepeatDetailEntities(
                    codes = dayOfWeeks,
                    scheduleId = genScheduleId(),
                    propertyCode = REPEAT_SETTING_PROPERTY_WEEKLY
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given monthly and empty repeat details, When creating Repeat, Then throw exception`() {
        val repeatType = REPEAT_MONTHLY
        val repeatDetailEntities = emptySet<RepeatDetailEntity>()
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = repeatDetailEntities
        )
    }

    @Test
    fun `Given monthly and monthly days, When creating Repeat, Then return Monthly with Each`() {
        val repeatType = REPEAT_MONTHLY
        val interval = genPositiveInt()
        val monthlyDays = (1..31).shuffledSubset()
        val expectedRepeat = Repeat.Monthly(
            interval = interval,
            detail = MonthlyRepeatDetail.Each(monthlyDays.toSet(::DaysOfMonth).toNonEmptySet())
        )
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                this += RepeatDetailEntities(
                    codes = monthlyDays.map { it.toString() },
                    scheduleId = genScheduleId(),
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test
    fun `Given monthly, dayOrder and days, When creating Repeat, Then return Monthly with customize`() {
        val repeatType = REPEAT_MONTHLY
        val interval = genPositiveInt()
        val dayOrder = sampleDayOrderCode
        val days = sampleDaysCode
        val expectedRepeat = Repeat.Monthly(
            interval = interval,
            detail = MonthlyRepeatDetail.Customize(
                order = DaysOfWeekOrder(dayOrder),
                day = Days(days)
            )
        )
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntities(
                    codes = listOf(dayOrder),
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER
                )
                this += RepeatDetailEntities(
                    codes = listOf(days),
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given yearly and empty repeat details, When creating Repeat, Then throw exception`() {
        val repeatType = REPEAT_YEARLY
        val repeatDetailEntities = emptySet<RepeatDetailEntity>()
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = repeatDetailEntities
        )
    }

    @Test
    fun `Given yearly and repeatMonths, When creating Repeat, Then return Yearly`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val months = sampleYearlyMonthCodes
        val expectedRepeat = Repeat.Yearly(
            interval = interval,
            months = months.toSet(::Month).toNonEmptySet(),
            daysOfWeekOption = null
        )
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                this += RepeatDetailEntities(
                    codes = months,
                    scheduleId = genScheduleId(),
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given yearly, month and dayOrder, When creating Repeat, Then throw exception`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val months = sampleYearlyMonthCodes
        val dayOrder = sampleDayOrderCode
        Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER,
                    value = dayOrder
                )
                this += RepeatDetailEntities(
                    codes = months,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given yearly, month and days, When creating Repeat, Then throw exception`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val months = sampleYearlyMonthCodes
        val days = sampleDaysCode
        Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK,
                    value = days
                )
                this += RepeatDetailEntities(
                    codes = months,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
    }

    @Test
    fun `Given yearly, month, dayOrder and days, When creating Repeat, Then return Yearly with option`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val dayOrder = sampleDayOrderCode
        val days = sampleDaysCode
        val months = sampleYearlyMonthCodes
        val expectedRepeat = Repeat.Yearly(
            interval = interval,
            months = months.toSet(::Month).toNonEmptySet(),
            daysOfWeekOption = YearlyDaysOfWeekOption(
                order = DaysOfWeekOrder(dayOrder),
                day = Days(days)
            )
        )
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER,
                    value = dayOrder
                )
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK,
                    value = days
                )
                this += RepeatDetailEntities(
                    codes = months,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test
    fun `Given hourly repeat, When convert to aggregate, Then return matched value`() {
        val repeat = genRepeatHourly()
        val aggregate = repeat.toAggregate()
        assertThat(aggregate.type, equalTo(REPEAT_HOURLY))
        assertThat(aggregate.interval, equalTo(repeat.interval))
        assertThat(aggregate.details, equalTo(emptySet()))
    }

    @Test
    fun `Given daily repeat, When convert to aggregate, Then return matched value`() {
        val repeat = genRepeatDaily()
        val aggregate = repeat.toAggregate()
        assertThat(aggregate.type, equalTo(REPEAT_DAILY))
        assertThat(aggregate.interval, equalTo(repeat.interval))
        assertThat(aggregate.details, equalTo(emptySet()))
    }

    @Test
    fun `Given repeat weekly, When convert to aggregate, Then return matched value`() {
        val repeat = genRepeatWeekly()
        val expectedDayOfWeeksCodes = repeat.daysOfWeeks.value.toSet { it.toRepeatWeek() }

        val actualAggregate = repeat.toAggregate()
        assertThat(actualAggregate.type, equalTo(REPEAT_WEEKLY))
        assertThat(actualAggregate.interval, equalTo(repeat.interval))

        val actualRepeatDetailAggregates = actualAggregate.details
        assertThat(actualRepeatDetailAggregates.size, equalTo(expectedDayOfWeeksCodes.size))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailAggregates)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_WEEKLY], equalTo(expectedDayOfWeeksCodes))
    }

    @Test
    fun `Given repeat monthly with each, When try convert to aggregate, Then return matched value`() {
        val monthlyRepeatDetail = genMonthlyRepeatDetailEach()
        val repeat = genRepeatMonthly(detail = monthlyRepeatDetail)

        val expectedMonthlyDays = monthlyRepeatDetail.days.value.toSet { it.rawValue.toString() }

        val actualAggregate = repeat.toAggregate()
        assertThat(actualAggregate.type, equalTo(REPEAT_MONTHLY))
        assertThat(actualAggregate.interval, equalTo(repeat.interval))

        val actualRepeatDetailAggregates = actualAggregate.details
        assertThat(actualRepeatDetailAggregates.size, equalTo(expectedMonthlyDays.size))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailAggregates)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_MONTHLY_DAY], equalTo(expectedMonthlyDays))
    }

    @Test
    fun `Given repeat monthly with customize, When try convert to aggregate, Then return matched value`() {
        val monthlyRepeatDetail = genMonthlyRepeatCustomize()
        val repeat = genRepeatMonthly(detail = monthlyRepeatDetail)

        val expectedOrderCode = monthlyRepeatDetail.order.toRepeatDayOrder()
        val expectedDayCode = monthlyRepeatDetail.day.toRepeatDays()
        // order + day
        val expectedAggregateSize = 2

        val actualAggregate = repeat.toAggregate()
        assertThat(actualAggregate.type, equalTo(REPEAT_MONTHLY))
        assertThat(actualAggregate.interval, equalTo(repeat.interval))

        val actualRepeatDetailAggregates = actualAggregate.details
        assertThat(actualRepeatDetailAggregates.size, equalTo(expectedAggregateSize))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailAggregates)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER], equalTo(setOf(expectedOrderCode)))
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK], equalTo(setOf(expectedDayCode)))
    }

    @Test
    fun `Given repeat yearly without dayOfWeekOption, When try convert to aggregate, Then return matched value`() {
        val repeat = genRepeatYearly(daysOfWeekOption = null)
        val expectedMonthCodes = repeat.months.value.toSet { it.toRepeatMonth() }

        val actualAggregate = repeat.toAggregate()
        assertThat(actualAggregate.type, equalTo(REPEAT_YEARLY))
        assertThat(actualAggregate.interval, equalTo(repeat.interval))

        val actualRepeatDetailAggregates = actualAggregate.details
        assertThat(actualRepeatDetailAggregates.size, equalTo(expectedMonthCodes.size))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailAggregates)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_MONTH], equalTo(expectedMonthCodes))
    }

    @Test
    fun `Given repeat yearly with dayOfWeekOption, When try convert to aggregate, Then return matched value`() {
        val dayOfWeekOption = genYearlyDaysOfWeekOption()
        val repeat = genRepeatYearly(daysOfWeekOption = dayOfWeekOption)
        val expectedMonthCodes = repeat.months.value.toSet { it.toRepeatMonth() }
        val expectedDayOrderCode = dayOfWeekOption.order.toRepeatDayOrder()
        val expectedDaysCode = dayOfWeekOption.day.toRepeatDays()
        // months + order + day
        val expectedAggregateSize = 2 + expectedMonthCodes.size

        val actualAggregate = repeat.toAggregate()
        assertThat(actualAggregate.type, equalTo(REPEAT_YEARLY))
        assertThat(actualAggregate.interval, equalTo(repeat.interval))

        val actualRepeatDetailAggregates = actualAggregate.details
        assertThat(actualRepeatDetailAggregates.size, equalTo(expectedAggregateSize))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailAggregates)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_MONTH], equalTo(expectedMonthCodes))
        assertThat(
            actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER],
            equalTo(setOf(expectedDayOrderCode))
        )
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK], equalTo(setOf(expectedDaysCode)))
    }
}

@Suppress("TestFunctionName")
private fun RepeatDetailEntities(
    codes: Iterable<String>,
    scheduleId: ScheduleId,
    propertyCode: String
): Set<RepeatDetailEntity> = codes.toSet { code ->
    RepeatDetailEntity(
        scheduleId = scheduleId.rawId,
        propertyCode = propertyCode,
        value = code
    )
}

private fun convertPropertyCodeToValuesTable(
    repeatDetailAggregates: Set<ScheduleRepeatDetailAggregate>
): Map<String, Set<String>> = buildMap<String, MutableSet<String>> {
    repeatDetailAggregates.forEach { repeatDetailAggregate ->
        getOrPut(repeatDetailAggregate.propertyCode) { mutableSetOf() }.add(repeatDetailAggregate.value)
    }
}
