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

import com.nlab.reminder.core.data.model.Days
import com.nlab.reminder.core.data.model.DaysOfMonth
import com.nlab.reminder.core.data.model.DaysOfWeekOrder
import com.nlab.reminder.core.data.model.MonthlyRepeatDetail
import com.nlab.reminder.core.data.model.YearlyDaysOfWeekOption
import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.NonEmptySet
import com.nlab.reminder.core.text.PluralsUiText
import com.nlab.reminder.core.text.UiText
import com.nlab.reminder.core.text.joinToUiText
import com.nlab.reminder.core.translation.PluralsIds
import com.nlab.reminder.core.translation.StringIds
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

/**
 * @author Doohyun
 */
internal fun contentEveryHours(interval: PositiveInt): UiText =
    if (interval.value == 1) UiText(resId = StringIds.content_every_hours_fixed)
    else UiText(resId = StringIds.content_every_hours_interval, interval.value)

internal fun contentEveryDays(interval: PositiveInt): UiText =
    if (interval.value == 1) UiText(resId = StringIds.content_every_days_fixed)
    else UiText(resId = StringIds.content_every_days_interval, interval.value)

internal fun contentEveryWeeks(
    interval: PositiveInt,
    dayOfWeeks: NonEmptySet<DayOfWeek>,
    isSameDayOfWeek: Boolean
): UiText {
    if (isSameDayOfWeek) return UiText(resId = StringIds.content_every_weeks_fixed)
    val contentEveryWeeks: UiText
    if (interval.value == 1) {
        if (dayOfWeeks.isExactlyWeekday()) return UiText(StringIds.content_every_weeks_weekdays)
        if (dayOfWeeks.isExactlyWeekend()) return UiText(StringIds.content_every_weeks_weekends)
        contentEveryWeeks = UiText(resId = StringIds.content_every_weeks_fixed)
    } else {
        contentEveryWeeks = UiText(resId = StringIds.content_every_weeks_interval, interval.value)
    }
    val dayOfWeeksText = if (dayOfWeeks.value.size == 1) {
        contentDayOfWeekUiText(dayOfWeeks.value.first())
    } else {
        val sortedDayOfWeeks = dayOfWeeks.value.sortedWith(sundayFirstComparator)
        UiText(
            resId = StringIds.content_every_weeks_token,
            sortedDayOfWeeks
                .take(sortedDayOfWeeks.size - 1)
                .joinToUiText(separatorRes = StringIds.comma_combine, transform = ::contentDayOfWeekUiText),
            contentDayOfWeekUiText(sortedDayOfWeeks.last())
        )
    }
    return UiText(
        resId = StringIds.content_every_weeks_combine,
        contentEveryWeeks,
        dayOfWeeksText
    )
}

internal fun contentEveryMonthsWithEachOption(
    interval: PositiveInt,
    option: MonthlyRepeatDetail.Each,
    isSameDay: Boolean,
): UiText {
    if (isSameDay) return UiText(resId = StringIds.content_every_months_fixed)
    val daysText: UiText = if (option.days.value.size == 1) {
        contentEveryMonthsDaysUiText(option.days.value.first())
    } else {
        val sortedDays = option.days.value.sorted()
        UiText(
            resId = StringIds.content_every_months_days_token,
            sortedDays
                .take(sortedDays.size - 1)
                .joinToUiText(separatorRes = StringIds.comma_combine, transform = ::contentEveryMonthsDaysUiText),
            contentEveryMonthsDaysUiText(sortedDays.last())
        )
    }
    return if (interval.value == 1) {
        UiText(
            resId = StringIds.content_every_months_days_combine_fixed,
            UiText(resId = StringIds.content_every_months_fixed_other_day),
            daysText
        )
    } else {
        UiText(
            resId = StringIds.content_every_months_days_combine_interval,
            UiText(resId = StringIds.content_every_months_interval, interval.value),
            daysText
        )
    }
}

internal fun contentEveryMonthsWithCustomizeOption(
    interval: PositiveInt,
    option: MonthlyRepeatDetail.Customize
): UiText = UiText(
    resId = StringIds.content_every_months_customize_combine,
    if (interval.value == 1) UiText(resId = StringIds.content_every_months_fixed_other_day)
    else UiText(resId = StringIds.content_every_months_interval, interval.value),
    contentOrderingDaysCombineUiText(
        order = option.order,
        day = option.day
    )
)

internal fun convertEveryYears(
    interval: PositiveInt,
    months: NonEmptySet<Month>,
    daysOfWeekOption: YearlyDaysOfWeekOption?,
    isSameMonth: Boolean,
): UiText {
    if (isSameMonth && daysOfWeekOption == null) return UiText(resId = StringIds.content_every_years_fixed)
    val contentEveryYears: UiText =
        if (interval.value == 1) UiText(resId = StringIds.content_every_years_fixed_other_month)
        else UiText(resId = StringIds.content_every_years_interval, interval.value)
    val monthsText = if (months.value.size == 1) contentMonthUiText(months.value.first())
    else {
        val sortedMonths = months.value.sorted()
        UiText(
            resId = StringIds.content_every_years_months_token,
            sortedMonths
                .take(sortedMonths.size - 1)
                .joinToUiText(separatorRes = StringIds.comma_combine, transform = ::contentMonthUiText),
            contentMonthUiText(sortedMonths.last())
        )
    }

    return if (daysOfWeekOption == null) {
        UiText(
            resId = StringIds.content_every_years_months_combine,
            contentEveryYears,
            monthsText
        )
    } else {
        UiText(
            resId = StringIds.content_every_years_ordering_days_months_combine,
            contentEveryYears,
            contentOrderingDaysCombineUiText(
                order = daysOfWeekOption.order,
                day = daysOfWeekOption.day
            ),
            monthsText
        )
    }
}

private fun contentDayOfWeekUiText(dayOfWeek: DayOfWeek): UiText =
    UiText(resId = dayOfWeek.resourceId)

private fun contentEveryMonthsDaysUiText(daysOfMonth: DaysOfMonth): UiText =
    UiText(resId = StringIds.content_every_months_days, daysOfMonth.rawValue)

private fun contentMonthUiText(month: Month): UiText =
    UiText(resId = month.fullNameResourceId)

private fun contentOrderingDaysCombineUiText(
    order: DaysOfWeekOrder,
    day: Days
): UiText = UiText(
    resId = StringIds.content_ordering_days_combine,
    UiText(resId = order.resourceId),
    UiText(resId = day.resourceId)
)