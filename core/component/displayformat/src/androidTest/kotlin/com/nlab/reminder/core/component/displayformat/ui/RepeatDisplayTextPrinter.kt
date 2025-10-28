package com.nlab.reminder.core.component.displayformat.ui

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nlab.reminder.core.component.displayformat.ScheduleTimingDisplayResource
import com.nlab.reminder.core.data.model.Repeat
import com.nlab.reminder.core.data.model.genScheduleTiming
import com.nlab.testkit.faker.genInt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class RepeatDisplayTextPrinter {
    private lateinit var appContext: Context
    private lateinit var now: Instant
    private lateinit var timeZone: TimeZone

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        now = Clock.System.now()
        timeZone = TimeZone.currentSystemDefault()
    }

    @Test
    fun printWithLocaleKorean() {
        printRepeatDisplayText(context = appContext.setLocale(Locale.KOREAN))
    }

    @Test
    fun printWithLocaleEnglish() {
        printRepeatDisplayText(context = appContext.setLocale(Locale.ENGLISH))
    }

    private fun printRepeatDisplayText(context: Context) {
        println("---- Print repeat display text ----")
        val scheduleTimingDisplayResource = ScheduleTimingDisplayResource(
            scheduleTiming = genScheduleTiming(
                triggerAt = Clock.System.now()
                    .plus(genInt(min = 0, max = 2), DateTimeUnit.DAY, timeZone)
                    .plus(genInt(min = 0, max = 5), DateTimeUnit.HOUR, timeZone)
                    .plus(genInt(min = 0, max = 59), DateTimeUnit.MINUTE, timeZone),
            ),
            timeZone = timeZone,
            entryAt = now
        )
        val repeat: Repeat?
        val triggerAt: LocalDate
        when (scheduleTimingDisplayResource) {
            is ScheduleTimingDisplayResource.Date -> {
                repeat = scheduleTimingDisplayResource.repeat
                triggerAt = scheduleTimingDisplayResource.triggerAt
            }
            is ScheduleTimingDisplayResource.DateTime -> {
                repeat = scheduleTimingDisplayResource.repeat
                triggerAt = scheduleTimingDisplayResource.triggerAt.date
            }
        }
        val result = if (repeat == null) ""
        else {
            repeatDisplayText(
                resources = context.resources,
                repeat = repeat,
                triggerAt = triggerAt
            )
        }
        println(result)
    }
}