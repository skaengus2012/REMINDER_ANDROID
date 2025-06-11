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

package com.nlab.reminder.core.component.displayformat.ui

import android.content.res.Resources
import com.nlab.reminder.core.data.model.Days
import com.nlab.reminder.core.data.model.DaysOfMonth
import com.nlab.reminder.core.data.model.DaysOfWeekOrder
import com.nlab.reminder.core.data.model.MonthlyRepeatDetail
import com.nlab.reminder.core.data.model.YearlyDaysOfWeekOption
import com.nlab.reminder.core.data.model.rawValue
import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.NonEmptySet
import com.nlab.reminder.core.kotlinx.datetime.isExactlyWeekdays
import com.nlab.reminder.core.kotlinx.datetime.isExactlyWeekend
import com.nlab.reminder.core.translation.StringIds
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

/**
 * @author Doohyun
 */
internal fun contentEveryHours(resources: Resources, interval: PositiveInt): String {
    return if (interval.value == 1) resources.getString(StringIds.content_every_hours_fixed)
    else resources.getString(StringIds.content_every_hours_interval, interval.value)
}

internal fun contentEveryDays(resources: Resources, interval: PositiveInt): String {
    return if (interval.value == 1) resources.getString(StringIds.content_every_days_fixed)
    else resources.getString(StringIds.content_every_days_interval, interval.value)
}

internal fun contentEveryWeeks(
    resources: Resources,
    interval: PositiveInt,
    dayOfWeeks: NonEmptySet<DayOfWeek>,
    isSameDayOfWeek: Boolean
): String {
    if (isSameDayOfWeek) return resources.getString(StringIds.content_every_weeks_fixed)
    val intervalText: String
    if (interval.value == 1) {
        if (dayOfWeeks.value.isExactlyWeekdays()) return resources.getString(StringIds.content_every_weeks_weekdays)
        if (dayOfWeeks.value.isExactlyWeekend()) return resources.getString(StringIds.content_every_weeks_weekends)
        intervalText = resources.getString(StringIds.content_every_weeks_fixed)
    } else {
        intervalText = resources.getString(StringIds.content_every_weeks_interval, interval.value)
    }
    val dayOfWeeksText = formatList(
        data = dayOfWeeks,
        sort = { it.value.sortedWith(sundayFirstComparator) },
        transform = { contentDayOfWeekText(resources, it) },
        combineToken = { joinedText, last ->
            resources.getString(
                StringIds.content_every_weeks_token,
                joinedText,
                last
            )
        }
    )
    return resources.getString(
        StringIds.content_every_weeks_combine,
        intervalText,
        dayOfWeeksText
    )
}

internal fun contentEveryMonthsWithEachOption(
    resources: Resources,
    interval: PositiveInt,
    option: MonthlyRepeatDetail.Each,
    isSameDay: Boolean,
): String {
    if (isSameDay) return resources.getString(StringIds.content_every_months_fixed)
    val daysText = formatList(
        data = option.days,
        sort = { it.value.sorted() },
        transform = { contentEveryMonthsDaysText(resources, it) },
        combineToken = { joinedText, last ->
            resources.getString(
                StringIds.content_every_months_days_token,
                joinedText,
                last
            )
        }
    )
    return if (interval.value == 1) {
        resources.getString(
            StringIds.content_every_months_days_combine_fixed,
            resources.getString(StringIds.content_every_months_fixed_other_day),
            daysText
        )
    } else {
        resources.getString(
            StringIds.content_every_months_days_combine_interval,
            resources.getString(StringIds.content_every_months_interval, interval.value),
            daysText
        )
    }
}

internal fun contentEveryMonthsWithCustomizeOption(
    resources: Resources,
    interval: PositiveInt,
    option: MonthlyRepeatDetail.Customize
): String = resources.getString(
    StringIds.content_every_months_customize_combine,
    if (interval.value == 1) resources.getString(StringIds.content_every_months_fixed_other_day)
    else resources.getString(StringIds.content_every_months_interval, interval.value),
    contentOrderingDaysCombineText(resources, option.order, option.day)
)

internal fun contentEveryYears(
    resources: Resources,
    interval: PositiveInt,
    months: NonEmptySet<Month>,
    daysOfWeekOption: YearlyDaysOfWeekOption?,
    isSameMonth: Boolean,
): String {
    if (isSameMonth && daysOfWeekOption == null) return resources.getString(StringIds.content_every_years_fixed)
    val intervalText: String =
        if (interval.value == 1) resources.getString(StringIds.content_every_years_fixed_other_month)
        else resources.getString(StringIds.content_every_years_interval, interval.value)
    val monthsText = formatList(
        data = months,
        sort = { it.value.sorted() },
        transform = { contentMonthText(resources, it) },
        combineToken = { joinedText, last ->
            resources.getString(
                StringIds.content_every_years_months_token,
                joinedText,
                last
            )
        }
    )
    return if (daysOfWeekOption == null) {
        resources.getString(
            StringIds.content_every_years_months_combine,
            intervalText,
            monthsText
        )
    } else {
        resources.getString(
            StringIds.content_every_years_ordering_days_months_combine,
            intervalText,
            contentOrderingDaysCombineText(resources, daysOfWeekOption.order, daysOfWeekOption.day),
            monthsText
        )
    }
}

private fun contentDayOfWeekText(resources: Resources, dayOfWeek: DayOfWeek): String =
    resources.getString(dayOfWeek.resourceId)

private fun contentEveryMonthsDaysText(resources: Resources, daysOfMonth: DaysOfMonth): String =
    resources.getString(StringIds.content_every_months_days, daysOfMonth.rawValue)

private fun contentMonthText(resources: Resources, month: Month): String =
    resources.getString(month.fullNameResourceId)

private fun contentOrderingDaysCombineText(
    resources: Resources,
    order: DaysOfWeekOrder,
    day: Days
): String = resources.getString(
    StringIds.content_ordering_days_combine,
    resources.getString(order.resourceId),
    resources.getString(day.resourceId)
)

private inline fun <T> formatList(
    data: NonEmptySet<T>,
    sort: (NonEmptySet<T>) -> List<T>,
    transform: (T) -> String,
    combineToken: (joinedText: String, last: String) -> String
): String =
    if (data.value.size == 1) transform(data.value.first())
    else {
        val sorted = sort(data)
        combineToken(
            buildString {
                val separatorEndIndex = sorted.size - 2
                for (i in 0 until sorted.size - 1) {
                    append(transform(sorted[i]))
                    if (i < separatorEndIndex) {
                        append(", ")
                    }
                }
            },
            transform(sorted.last())
        )
    }