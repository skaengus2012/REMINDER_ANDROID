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
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.local.database.model.*
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.requireSample
import com.nlab.testkit.faker.shuffledSubset
import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.TimeZone
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

    @Test(expected = IllegalArgumentException::class)
    fun `Given invalid repeat type, When convert Repeat, Then throw required exception`() {
        val repeatType = "INVALID_REPEAT_TYPE"
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = emptySet()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given non positive interval, When convert Repeat, Then throw required exception`() {
        val interval = genInt(min = -9999, max = 0)
        Repeat(
            type = REPEAT_HOURLY,
            interval = interval,
            detailEntities = emptySet()
        )
    }

    @Test
    fun `Given hourly, positive number, When convert Repeat, Then return Hourly`() {
        val repeatType = REPEAT_HOURLY
        val interval = genInt(min = 1)
        val expectedRepeat = Repeat.Hourly(interval = interval.toPositiveInt())
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval,
            detailEntities = emptySet()
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test
    fun `Given daily, positive number, When convert Repeat, Then return Daily`() {
        val repeatType = REPEAT_DAILY
        val interval = genInt(min = 1)
        val expectedRepeat = Repeat.Daily(interval = interval.toPositiveInt())
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval,
            detailEntities = emptySet()
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given weekly and empty repeat details, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_WEEKLY
        val repeatDetailEntities = emptySet<RepeatDetailEntity>()
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = repeatDetailEntities
        )
    }

    @Test(expected = IllegalTimeZoneException::class)
    fun `Given weekly and invalid zoneId, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_WEEKLY
        val invalidZoneId = "INVALID_ZONE_ID"
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = setOf(
                RepeatDetailEntity(
                    scheduleId = genScheduleId().rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = invalidZoneId
                )
            )
        )
    }

    @Test
    fun `Given weekly, timeZone and dayOfWeeks, When convert Repeat, Then return Weekly`() {
        val repeatType = REPEAT_WEEKLY
        val interval = genPositiveInt()
        val timeZone = TimeZone.currentSystemDefault()
        val dayOfWeeks = sampleDaysOfWeekCodes
        val expectedRepeat = Repeat.Weekly(
            interval = interval,
            timeZone = timeZone,
            daysOfWeeks = dayOfWeeks.toSet(::DayOfWeek).toNonEmptySet()
        )

        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = timeZone.id
                )
                this += createRepeatDetailEntities(
                    codes = dayOfWeeks,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_WEEKLY
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    private fun createRepeatDetailEntities(
        codes: Iterable<String>,
        scheduleId: ScheduleId,
        propertyCode: String
    ): List<RepeatDetailEntity> = codes.map { code ->
        RepeatDetailEntity(
            scheduleId = scheduleId.rawId,
            propertyCode = propertyCode,
            value = code
        )
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given monthly and empty repeat details, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_MONTHLY
        val repeatDetailEntities = emptySet<RepeatDetailEntity>()
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = repeatDetailEntities
        )
    }

    @Test(expected = IllegalTimeZoneException::class)
    fun `Given monthly and invalid zoneId, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_MONTHLY
        val invalidZoneId = "INVALID_ZONE_ID"
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = setOf(
                RepeatDetailEntity(
                    scheduleId = genScheduleId().rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = invalidZoneId
                )
            )
        )
    }

    @Test
    fun `Given monthly, timeZone and monthly days, When convert Repeat, Then return Monthly with Each`() {
        val repeatType = REPEAT_MONTHLY
        val interval = genPositiveInt()
        val timeZone = TimeZone.currentSystemDefault()
        val monthlyDays = (1..31).shuffledSubset()
        val expectedRepeat = Repeat.Monthly(
            interval = interval,
            timeZone = timeZone,
            detail = MonthlyRepeatDetail.Each(monthlyDays.toSet(::DaysOfMonth).toNonEmptySet())
        )

        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = timeZone.id
                )
                this += createRepeatDetailEntities(
                    codes = monthlyDays.map { it.toString() },
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test
    fun `Given monthly, timeZone, dayOrder and days, When convert Repeat, Then return Monthly with customize`() {
        val repeatType = REPEAT_MONTHLY
        val interval = genPositiveInt()
        val timeZone = TimeZone.currentSystemDefault()
        val dayOrder = sampleDayOrderCode
        val days = sampleDaysCode
        val expectedRepeat = Repeat.Monthly(
            interval = interval,
            timeZone = timeZone,
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
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = timeZone.id
                )
                this += createRepeatDetailEntities(
                    codes = listOf(dayOrder),
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER
                )
                this += createRepeatDetailEntities(
                    codes = listOf(days),
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given yearly and empty repeat details, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_YEARLY
        val repeatDetailEntities = emptySet<RepeatDetailEntity>()
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = repeatDetailEntities
        )
    }

    @Test(expected = IllegalTimeZoneException::class)
    fun `Given yearly and invalid zoneId, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_YEARLY
        val invalidZoneId = "INVALID_ZONE_ID"
        Repeat(
            type = repeatType,
            interval = genInt(min = 1),
            detailEntities = setOf(
                RepeatDetailEntity(
                    scheduleId = genScheduleId().rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = invalidZoneId
                )
            )
        )
    }

    @Test
    fun `Given yearly, timeZone and repeatMonths, When convert Repeat, Then return Yearly`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val timeZone = TimeZone.currentSystemDefault()
        val months = sampleYearlyMonthCodes
        val expectedRepeat = Repeat.Yearly(
            interval = interval,
            timeZone = timeZone,
            months = months.toSet(::Month).toNonEmptySet(),
            daysOfWeekOption = null
        )
        val actualRepeat = Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = timeZone.id
                )
                this += createRepeatDetailEntities(
                    codes = months,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given yearly, timeZone, month and dayOrder, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val timeZone = TimeZone.currentSystemDefault()
        val months = sampleYearlyMonthCodes
        val dayOrder = sampleDayOrderCode
        Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = timeZone.id
                )
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER,
                    value = dayOrder
                )
                this += createRepeatDetailEntities(
                    codes = months,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given yearly, timeZone, month and days, When convert Repeat, Then throw exception`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val timeZone = TimeZone.currentSystemDefault()
        val months = sampleYearlyMonthCodes
        val days = sampleDaysCode
        Repeat(
            type = repeatType,
            interval = interval.value,
            detailEntities = buildSet {
                val scheduleId = genScheduleId()
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = timeZone.id
                )
                this += RepeatDetailEntity(
                    scheduleId = scheduleId.rawId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK,
                    value = days
                )
                this += createRepeatDetailEntities(
                    codes = months,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
    }

    @Test
    fun `Given yearly, timeZone, month, dayOrder and days, When convert Repeat, Then return Yearly with option`() {
        val repeatType = REPEAT_YEARLY
        val interval = genPositiveInt()
        val timeZone = TimeZone.currentSystemDefault()
        val dayOrder = sampleDayOrderCode
        val days = sampleDaysCode
        val months = sampleYearlyMonthCodes
        val expectedRepeat = Repeat.Yearly(
            interval = interval,
            timeZone = timeZone,
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
                    propertyCode = REPEAT_SETTING_PROPERTY_ZONE_ID,
                    value = timeZone.id
                )
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
                this += createRepeatDetailEntities(
                    codes = months,
                    scheduleId = scheduleId,
                    propertyCode = REPEAT_SETTING_PROPERTY_YEARLY_MONTH
                )
            }
        )
        assertThat(actualRepeat, equalTo(expectedRepeat))
    }

    @Test
    fun `Given repeat, When convert to REPEAT_TYPE string, Then return valid repeat type`() {
        assertThat(genRepeatHourly().toRepeatType(), equalTo(REPEAT_HOURLY))
        assertThat(genRepeatDaily().toRepeatType(), equalTo(REPEAT_DAILY))
        assertThat(genRepeatWeekly().toRepeatType(), equalTo(REPEAT_WEEKLY))
        assertThat(genRepeatMonthly().toRepeatType(), equalTo(REPEAT_MONTHLY))
        assertThat(genRepeatYearly().toRepeatType(), equalTo(REPEAT_YEARLY))
    }

    @Test
    fun `Given interval and repeat with interval, When convert to interval as int, Then return valid interval`() {
        val hourlyInterval = genPositiveInt()
        val dailyInterval = genPositiveInt()
        val weeklyInterval = genPositiveInt()
        val monthlyInterval = genPositiveInt()
        val yearlyInterval = genPositiveInt()
        assertThat(genRepeatHourly(interval = hourlyInterval).toIntervalAsInt(), equalTo(hourlyInterval.value))
        assertThat(genRepeatDaily(interval = dailyInterval).toIntervalAsInt(), equalTo(dailyInterval.value))
        assertThat(genRepeatWeekly(interval = weeklyInterval).toIntervalAsInt(), equalTo(weeklyInterval.value))
        assertThat(genRepeatMonthly(interval = monthlyInterval).toIntervalAsInt(), equalTo(monthlyInterval.value))
        assertThat(genRepeatYearly(interval = yearlyInterval).toIntervalAsInt(), equalTo(yearlyInterval.value))
    }

    @Test
    fun `Given repeat hourly, When try convert to dto, Then return empty`() {
        val repeat = genRepeatHourly()
        val expectedDTOs = emptySet<RepeatDetailEntity>()
        val actualDTOs = repeat.toRepeatDetailDTOs()
        assertThat(actualDTOs, equalTo(expectedDTOs))
    }

    @Test
    fun `Given repeat daily, When try convert to dto, Then return empty`() {
        val repeat = genRepeatDaily()
        val expectedDTOs = emptySet<RepeatDetailEntity>()
        val actualDTOs = repeat.toRepeatDetailDTOs()
        assertThat(actualDTOs, equalTo(expectedDTOs))
    }

    @Test
    fun `Given repeat weekly, When try convert to dto, Then return valid dto set`() {
        val repeat = genRepeatWeekly()
        val expectedZoneId = repeat.timeZone.id
        val expectedDayOfWeeksCodes = repeat.daysOfWeeks.value.toSet { it.toRepeatWeek() }
        // zoneId + dayOfWeeks.size
        val expectedDTOSize = 1 + expectedDayOfWeeksCodes.size

        val actualRepeatDetailDTOs = repeat.toRepeatDetailDTOs()
        assertThat(actualRepeatDetailDTOs.size, equalTo(expectedDTOSize))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailDTOs)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_ZONE_ID], equalTo(setOf(expectedZoneId)))
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_WEEKLY], equalTo(expectedDayOfWeeksCodes))
    }

    private fun convertPropertyCodeToValuesTable(dto: Set<RepeatDetailDTO>): Map<String, Set<String>> {
        return buildMap<String, MutableSet<String>> {
            dto.forEach { dto ->
                getOrPut(dto.propertyCode) { mutableSetOf() }.add(dto.value)
            }
        }
    }

    @Test
    fun `Given repeat monthly with each, When try convert to dto, Then return valid dto set`() {
        val monthlyRepeatDetail = genMonthlyRepeatDetailEach()
        val repeat = genRepeatMonthly(detail = monthlyRepeatDetail)

        val expectedZoneId = repeat.timeZone.id
        val expectedMonthlyDays = monthlyRepeatDetail.days.value.toSet { it.rawValue.toString() }
        // zoneId + dayOfWeeks.size
        val expectedDTOSize = 1 + expectedMonthlyDays.size

        val actualRepeatDetailDTOs = repeat.toRepeatDetailDTOs()
        assertThat(actualRepeatDetailDTOs.size, equalTo(expectedDTOSize))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailDTOs)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_ZONE_ID], equalTo(setOf(expectedZoneId)))
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_MONTHLY_DAY], equalTo(expectedMonthlyDays))
    }

    @Test
    fun `Given repeat monthly with customize, When try convert to dto, Then return valid dto set`() {
        val monthlyRepeatDetail = genMonthlyRepeatCustomize()
        val repeat = genRepeatMonthly(detail = monthlyRepeatDetail)

        val expectedZoneId = repeat.timeZone.id
        val expectedOrderCode = monthlyRepeatDetail.order.toRepeatDayOrder()
        val expectedDayCode = monthlyRepeatDetail.day.toRepeatDays()
        // zoneId + order + day
        val expectedDTOSize = 3

        val actualRepeatDetailDTOs = repeat.toRepeatDetailDTOs()
        assertThat(actualRepeatDetailDTOs.size, equalTo(expectedDTOSize))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailDTOs)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_ZONE_ID], equalTo(setOf(expectedZoneId)))
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER], equalTo(setOf(expectedOrderCode)))
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK], equalTo(setOf(expectedDayCode)))
    }

    @Test
    fun `Given repeat yearly without dayOfWeekOption, When try convert to dto, Then return valid dto set`() {
        val repeat = genRepeatYearly(daysOfWeekOption = null)
        val expectedZoneId = repeat.timeZone.id
        val expectedMonthCodes = repeat.months.value.toSet { it.toRepeatMonth() }
        // zoneId + month.size
        val expectedDTOSize = 1 + expectedMonthCodes.size

        val actualRepeatDetailDTOs = repeat.toRepeatDetailDTOs()
        assertThat(actualRepeatDetailDTOs.size, equalTo(expectedDTOSize))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailDTOs)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_ZONE_ID], equalTo(setOf(expectedZoneId)))
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_MONTH], equalTo(expectedMonthCodes))
    }

    @Test
    fun `Given repeat yearly with dayOfWeekOption, When try convert to dto, Then return valid dto set`() {
        val dayOfWeekOption = genYearlyDaysOfWeekOption()
        val repeat = genRepeatYearly(daysOfWeekOption = dayOfWeekOption)
        val expectedZoneId = repeat.timeZone.id
        val expectedMonthCodes = repeat.months.value.toSet { it.toRepeatMonth() }
        val expectedDayOrderCode = dayOfWeekOption.order.toRepeatDayOrder()
        val expectedDaysCode = dayOfWeekOption.day.toRepeatDays()
        // zoneId + months + order + day
        val expectedDTOSize = 3 + expectedMonthCodes.size

        val actualRepeatDetailDTOs = repeat.toRepeatDetailDTOs()
        assertThat(actualRepeatDetailDTOs.size, equalTo(expectedDTOSize))

        val actualPropertyToValues = convertPropertyCodeToValuesTable(actualRepeatDetailDTOs)
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_ZONE_ID], equalTo(setOf(expectedZoneId)))
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_MONTH], equalTo(expectedMonthCodes))
        assertThat(
            actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER],
            equalTo(setOf(expectedDayOrderCode))
        )
        assertThat(actualPropertyToValues[REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK], equalTo(setOf(expectedDaysCode)))
    }
}