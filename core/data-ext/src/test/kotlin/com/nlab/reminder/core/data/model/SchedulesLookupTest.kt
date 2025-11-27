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

import com.nlab.testkit.faker.genIntGreaterThanZero
import com.nlab.testkit.faker.genLongGreaterThanZero
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.not
import org.hamcrest.trueValue
import org.junit.Test

/**
 * @author Thalys
 */
class SchedulesLookupTest {
    @Test
    fun `Given schedules, When SchedulesLookup created, Then values are the same instance`() {
        val schedules = genSchedules()
        val schedulesLookup = SchedulesLookup(schedules)
        assertThat(schedulesLookup.values, sameInstance(schedules))
    }

    @Test
    fun `Given existing ScheduleId, When 'in' operator used, Then returns true`() {
        val target = genSchedule(id = ScheduleId(100))
        val schedulesLookup = SchedulesLookup(
            values = buildSet {
                this += target
                (0 until genIntGreaterThanZero(max = 10)).forEach { index ->
                    this += genSchedule(id = ScheduleId(index + 1L))
                }
            }
        )
        assertThat(
            target.id in schedulesLookup,
            trueValue()
        )
    }

    @Test
    fun `Given non-existing ScheduleId, When 'in' operator used, Then returns false`() {
        val maxRawIdValue = genLongGreaterThanZero(max = 10)
        val schedulesLookup = SchedulesLookup(
            values = List(size = maxRawIdValue.toInt()) { genSchedule(id = ScheduleId(it + 1L)) }.toSet()
        )
        assertThat(
            ScheduleId(rawId = maxRawIdValue * 2) in schedulesLookup,
            not(trueValue())
        )
    }

    @Test
    fun `Given existing ScheduleId, When requireValue invoked, Then returns corresponding schedule`() {
        val targetId = genScheduleId()
        val expectedSchedule = genSchedule(id = targetId)
        val schedulesLookup = SchedulesLookup(
            values = setOf(expectedSchedule)
        )
        assertThat(schedulesLookup.requireValue(targetId), sameInstance(expectedSchedule))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given non-existing ScheduleId, When requireValue invoked, Then throws NoSuchElementException`() {
        val targetId = genScheduleId()
        val schedulesLookup = SchedulesLookup(
            values = setOf(genSchedule(id = ScheduleId(rawId = targetId.rawId + 1)))
        )
        schedulesLookup.requireValue(targetId)
    }
}