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
import com.nlab.reminder.core.android.content.res.primaryLocale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import kotlin.time.Clock

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class DateTimeFormatPatternsPrinter {
    private lateinit var appContext: Context
    private lateinit var timeZone: TimeZone
    private lateinit var yesterday: LocalDateTime
    private lateinit var today: LocalDateTime
    private lateinit var tomorrow: LocalDateTime
    private lateinit var dayAfterTomorrow: LocalDateTime
    private lateinit var etcDays: LocalDateTime

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        timeZone = TimeZone.currentSystemDefault()

        val now = Clock.System.now()
        today = now.toLocalDateTime(timeZone)
        yesterday = now
            .minus(1, DateTimeUnit.DAY, timeZone)
            .toLocalDateTime(timeZone)
        tomorrow = now
            .plus(1, DateTimeUnit.DAY, timeZone)
            .toLocalDateTime(timeZone)
        dayAfterTomorrow = now
            .plus(2, DateTimeUnit.DAY, timeZone)
            .toLocalDateTime(timeZone)
        etcDays = now
            .plus(3, DateTimeUnit.DAY, timeZone)
            .toLocalDateTime(timeZone)
    }

    @Test
    fun printTriggerAtWithLocaleKorean() {
        printTriggerAt(context = appContext.setLocale(Locale.KOREAN))
    }

    @Test
    fun printTriggerAtWithLocaleEnglish() {
        printTriggerAt(context = appContext.setLocale(Locale.ENGLISH))
    }

    @Test
    fun printTriggerAtAsDateOnlyWithLocaleKorean() {
        printTriggerAtAsDateOnly(context = appContext.setLocale(Locale.KOREAN))
    }

    @Test
    fun printTriggerAtAsDateOnlyWithLocaleEnglish() {
        printTriggerAtAsDateOnly(context = appContext.setLocale(Locale.ENGLISH))
    }

    private fun printTriggerAt(context: Context) {
        fun triggerAtFormatted(
            triggerAt: LocalDateTime,
            entryAt: LocalDateTime,
        ): String = triggerAt.toJavaLocalDateTime().format(
            triggerAtDateTimeFormatPatternForList(
                context.resources,
                triggerAt = triggerAt,
                entryAt = entryAt
            ).toJavaDateTimeFormat(locale = context.resources.primaryLocale)
        )

        println("---- Print triggerAt----")
        println("• Print yesterday : ${triggerAtFormatted(triggerAt = yesterday, entryAt = today)}")
        println("• Print today : ${triggerAtFormatted(triggerAt = today, entryAt = today)}")
        println("• Print tomorrow : ${triggerAtFormatted(triggerAt = tomorrow, entryAt = today)}")
        println(
            "• Print the day after tomorrow : ${
                triggerAtFormatted(
                    triggerAt = dayAfterTomorrow,
                    entryAt = today
                )
            }"
        )
        println("• Print etc : ${triggerAtFormatted(triggerAt = etcDays, entryAt = today)}")
    }

    private fun printTriggerAtAsDateOnly(context: Context) {
        fun triggerAtAsDateOnlyFormatted(
            triggerAt: LocalDate,
            entryAt: LocalDate,
        ): String = triggerAt.toJavaLocalDate().format(
            triggerAtDateTimeFormatPatternForList(
                context.resources,
                triggerAt = triggerAt,
                entryAt = entryAt
            ).toJavaDateTimeFormat(context.resources.primaryLocale)
        )

        println("---- Print triggerAt as date only ----")
        println("• Print yesterday : ${triggerAtAsDateOnlyFormatted(triggerAt = yesterday.date, entryAt = today.date)}")
        println("• Print today : ${triggerAtAsDateOnlyFormatted(triggerAt = today.date, entryAt = today.date)}")
        println("• Print tomorrow : ${triggerAtAsDateOnlyFormatted(triggerAt = tomorrow.date, entryAt = today.date)}")
        println(
            "• Print the day after tomorrow : ${
                triggerAtAsDateOnlyFormatted(
                    triggerAt = dayAfterTomorrow.date,
                    entryAt = today.date
                )
            }"
        )
        println("• Print etc : ${triggerAtAsDateOnlyFormatted(triggerAt = etcDays.date, entryAt = today.date)}")
    }
}