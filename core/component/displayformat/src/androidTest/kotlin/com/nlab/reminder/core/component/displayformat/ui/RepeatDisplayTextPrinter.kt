package com.nlab.reminder.core.component.displayformat.ui

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nlab.reminder.core.data.model.genScheduleTiming
import com.nlab.testkit.faker.genInt
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class RepeatDisplayTextPrinter {
    private lateinit var context: Context
    private lateinit var initialLocale: Locale
    private lateinit var now: Instant
    private lateinit var timeZone: TimeZone

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        initialLocale = context.currentLocale
        now = Clock.System.now()
        timeZone = TimeZone.currentSystemDefault()
    }

    @After
    fun teardown() {
        context = context.setLocale(initialLocale)
    }

    @Test
    fun printToRepeatTextWithLocaleKorean() {
        context = context.setLocale(Locale.KOREAN)
        printRepeatDisplayText()
    }

    @Test
    fun printWithLocaleEnglish() {
        context = context.setLocale(Locale.ENGLISH)
        printRepeatDisplayText()
    }

    private fun printRepeatDisplayText() {
        println("---- Print repeat display text ----")
        val scheduleTimingDisplayResource = ScheduleTimingDisplayResource(
            scheduleTiming = genScheduleTiming(
                triggerAt = Clock.System.now()
                    .plus(genInt(min = 0, max = 2), DateTimeUnit.DAY, timeZone)
                    .plus(genInt(min = 0, max = 2), DateTimeUnit.HOUR, timeZone),
            ),
            timeZone = timeZone,
            entryAt = now
        )
        println(scheduleTimingDisplayResource.toRepeatDisplayText(context.resources))
    }

}