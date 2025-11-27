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

package com.nlab.reminder.core.component.displayformat

import com.nlab.reminder.core.data.model.genScheduleTimingDateTimeType
import com.nlab.reminder.core.data.model.genScheduleTimingDateType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @author Doohyun
 */
class ScheduleTimingDisplayResourceKtTest {
    private lateinit var seoulTimeZone: TimeZone

    @Before
    fun setup() {
        mockkStatic(Instant::toLocalDateTime)
        seoulTimeZone = TimeZone.of("Asia/Seoul")
    }

    @After
    fun teardown() {
        unmockkStatic(Instant::toLocalDateTime)
    }

    @Test
    fun `Given triggerAt is date-only, When creating display resource, Then returns DateOnly with correct value`() {
        val expectedTriggerAtLocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val expectedEntryAtLocalDateTime = Clock.System.now().toLocalDateTime(seoulTimeZone)
        val triggerAt: Instant = mockk {
            val localDateTimeResult: LocalDateTime = mockk {
                every { date } returns expectedTriggerAtLocalDate
            }
            every { toLocalDateTime(TimeZone.UTC) } returns localDateTimeResult
        }
        val entryAt: Instant = mockk {
            every { toLocalDateTime(seoulTimeZone) } returns expectedEntryAtLocalDateTime
        }
        val scheduleTiming = genScheduleTimingDateType(triggerAt = triggerAt)

       val scheduleTimingDisplayResource = ScheduleTimingDisplayResource(
            scheduleTiming = scheduleTiming,
            timeZone = seoulTimeZone,
            entryAt = entryAt
        )

        val dateTypeResource = scheduleTimingDisplayResource as ScheduleTimingDisplayResource.Date
        assertThat(
            dateTypeResource.triggerAt,
            sameInstance(expectedTriggerAtLocalDate)
        )
        assertThat(
            dateTypeResource.entryAt,
            sameInstance(expectedEntryAtLocalDateTime)
        )
        assertThat(
            dateTypeResource.repeat,
            sameInstance(scheduleTiming.dateOnlyRepeat)
        )
    }

    @Test
    fun `Given triggerAt is datetime, When creating display resource, Then returns Datetime with correct values`() {
        val expectedTriggerAtLocalDateTime = Clock.System.now().toLocalDateTime(seoulTimeZone)
        val expectedEntryAtLocalDateTime = Clock.System.now().toLocalDateTime(seoulTimeZone)
        val triggerAt: Instant = mockk {
            every { toLocalDateTime(seoulTimeZone) } returns expectedTriggerAtLocalDateTime
        }
        val entryAt: Instant = mockk {
            every { toLocalDateTime(seoulTimeZone) } returns expectedEntryAtLocalDateTime
        }
        val scheduleTiming = genScheduleTimingDateTimeType(triggerAt = triggerAt)

        val scheduleTimingDisplayResource = ScheduleTimingDisplayResource(
            scheduleTiming = scheduleTiming,
            timeZone = seoulTimeZone,
            entryAt = entryAt
        )

        val dateTimeTypeResource = scheduleTimingDisplayResource as ScheduleTimingDisplayResource.DateTime
        assertThat(
            dateTimeTypeResource.triggerAt,
            sameInstance(expectedTriggerAtLocalDateTime)
        )
        assertThat(
            dateTimeTypeResource.entryAt,
            sameInstance(expectedEntryAtLocalDateTime)
        )
        assertThat(
            dateTimeTypeResource.repeat,
            sameInstance(scheduleTiming.repeat)
        )
    }
}