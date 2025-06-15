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

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nlab.reminder.core.data.model.Days
import com.nlab.reminder.core.data.model.DaysOfMonth
import com.nlab.reminder.core.data.model.DaysOfWeekOrder
import com.nlab.reminder.core.data.model.MonthlyRepeatDetail
import com.nlab.reminder.core.kotlin.collections.NonEmptySet
import com.nlab.reminder.core.kotlin.collections.toNonEmptySet
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.shuffleAndGetFirst
import com.nlab.testkit.faker.shuffledSubset
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class RepeatDisplayFormatsPrinter {
    private lateinit var appContext: Context

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun printWithLocaleKorean() {
        printContentEveryRepeats(context = appContext.setLocale(Locale.KOREAN))
    }

    @Test
    fun printWithLocaleEnglish() {
        printContentEveryRepeats(context = appContext.setLocale(Locale.ENGLISH))
    }

    private fun printContentEveryRepeats(context: Context) {
        println("---- Print content every repeat ----")
        println("• Print hourly")
        println("  ◦ ${contentEveryHours(resources = context.resources, interval = 1.toPositiveInt())}")
        println("  ◦ ${contentEveryHours(resources = context.resources, interval = 2.toPositiveInt())}")
        println("• Print daily")
        println("  ◦ ${contentEveryDays(resources = context.resources, interval = 1.toPositiveInt())}")
        println("  ◦ ${contentEveryDays(resources = context.resources, interval = 2.toPositiveInt())}")
        println("• Print weekly")
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    dayOfWeeks = NonEmptySet(DayOfWeek.entries.random()),
                    isSameDayOfWeek = true
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = 2.toPositiveInt(),
                    dayOfWeeks = NonEmptySet(DayOfWeek.entries.random()),
                    isSameDayOfWeek = true
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    dayOfWeeks = NonEmptySet(DayOfWeek.entries.random()),
                    isSameDayOfWeek = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    dayOfWeeks = NonEmptySet(DayOfWeek.SUNDAY, DayOfWeek.MONDAY),
                    isSameDayOfWeek = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    dayOfWeeks = NonEmptySet(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY),
                    isSameDayOfWeek = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    dayOfWeeks = NonEmptySet(
                        DayOfWeek.MONDAY, 
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    ),
                    isSameDayOfWeek = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = genInt(min = 2, max = 10).toPositiveInt(),
                    dayOfWeeks = DayOfWeek.entries.toNonEmptySet(),
                    isSameDayOfWeek = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = genInt(min = 2, max = 10).toPositiveInt(),
                    dayOfWeeks = NonEmptySet(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY),
                    isSameDayOfWeek = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryWeeks(
                    resources = context.resources,
                    interval = genInt(min = 2, max = 10).toPositiveInt(),
                    dayOfWeeks = NonEmptySet(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    ),
                    isSameDayOfWeek = false
                )
            }"
        )
        println("• Print monthly")
        println(
            "  ◦ ${
                contentEveryMonthsWithEachOption(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    option = MonthlyRepeatDetail.Each(
                        days = NonEmptySet(
                            DaysOfMonth.entries.shuffleAndGetFirst()
                        )
                    ),
                    isSameDay = true
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryMonthsWithEachOption(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    option = MonthlyRepeatDetail.Each(
                        days = NonEmptySet(
                            DaysOfMonth.entries.shuffleAndGetFirst()
                        )
                    ),
                    isSameDay = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryMonthsWithEachOption(
                    resources = context.resources,
                    interval = genInt(min = 2, max = 10).toPositiveInt(),
                    option = MonthlyRepeatDetail.Each(
                        days = DaysOfMonth.entries
                            .shuffledSubset(generateMinSize = 2)
                            .toNonEmptySet()
                    ),
                    isSameDay = false
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryMonthsWithCustomizeOption(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    option = MonthlyRepeatDetail.Customize(
                        order = DaysOfWeekOrder.entries.random(),
                        day = Days.entries.random()
                    )
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryMonthsWithCustomizeOption(
                    resources = context.resources,
                    interval = genInt(min = 2, max = 10).toPositiveInt(),
                    option = MonthlyRepeatDetail.Customize(
                        order = DaysOfWeekOrder.entries.random(),
                        day = Days.entries.random()
                    )
                )
            }"
        )
        println("• Print yearly")
        println(
            "  ◦ ${
                contentEveryYears(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    months = NonEmptySet(Month.entries.random()),
                    daysOfWeekOption = null,
                    isSameMonth = true,
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryYears(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    months = NonEmptySet(Month.entries.random()),
                    daysOfWeekOption = null,
                    isSameMonth = false,
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryYears(
                    resources = context.resources,
                    interval = genInt(min = 2, max = 10).toPositiveInt(),
                    months = NonEmptySet(Month.entries.random()),
                    daysOfWeekOption = null,
                    isSameMonth = false,
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryYears(
                    resources = context.resources,
                    interval = 1.toPositiveInt(),
                    months = Month.entries
                        .shuffledSubset(generateMinSize = 2)
                        .toNonEmptySet(),
                    daysOfWeekOption = null,
                    isSameMonth = false,
                )
            }"
        )
        println(
            "  ◦ ${
                contentEveryYears(
                    resources = context.resources,
                    interval = genInt(min = 2, max = 10).toPositiveInt(),
                    months = Month.entries
                        .shuffledSubset(generateMinSize = 2)
                        .toNonEmptySet(),
                    daysOfWeekOption = null,
                    isSameMonth = false,
                )
            }"
        )
    }
}