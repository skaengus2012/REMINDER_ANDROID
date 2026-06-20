/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.toNonNegativeInt
import com.nlab.testkit.faker.genBoolean
import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class ScheduleListElementExtTest {

    @Test
    fun `Given UserScheduleListResource, When userScheduleListResourceOrNull is called, Then returns self`() {
        val resource = genUserScheduleListResource()
        assertThat(resource.userScheduleListResourceOrNull(), equalTo(resource))
    }

    @Test
    fun `Given dummy element, When userScheduleListResourceOrNull is called, Then returns null`() {
        val dummy: ScheduleListElement = mockk()
        assertThat(dummy.userScheduleListResourceOrNull(), nullValue())
    }

    @Test
    fun `Given elements, When toScheduleListStats with all flags true, Then returns correct stats`() {
        val schedule1 = genScheduleListResource(isComplete = true)
        val schedule2 = genScheduleListResource(isComplete = false)
        val schedule3 = genScheduleListResource(isComplete = true)

        val elements = listOf(
            genUserScheduleListResource(schedule = schedule1, selected = true),
            genUserScheduleListResource(schedule = schedule2, selected = true),
            genUserScheduleListResource(schedule = schedule3, selected = false),
            mockk<ScheduleListElement>() // dummy
        )

        val stats = elements.toScheduleListStats(needCompletedCount = true, needSelectedCount = true)
        assertThat(stats.completedCount, equalTo(2.toNonNegativeInt()))
        assertThat(stats.selectedCount, equalTo(2.toNonNegativeInt()))
    }

    @Test
    fun `Given elements, When toScheduleListStats with all flags false, Then returns zero stats`() {
        val schedule1 = genScheduleListResource(isComplete = true)
        val schedule2 = genScheduleListResource(isComplete = false)

        val elements = listOf(
            genUserScheduleListResource(schedule = schedule1, selected = true),
            genUserScheduleListResource(schedule = schedule2, selected = true)
        )

        val stats = elements.toScheduleListStats(needCompletedCount = false, needSelectedCount = false)
        assertThat(stats.completedCount, equalTo(0.toNonNegativeInt()))
        assertThat(stats.selectedCount, equalTo(0.toNonNegativeInt()))
    }

    @Test
    fun `Given elements, When mapToScheduleIds, Then returns filtered set of schedule ids`() {
        val id1 = ScheduleId(1)
        val id2 = ScheduleId(2)
        val id3 = ScheduleId(3)

        val schedule1 = genScheduleListResource(id = id1)
        val schedule2 = genScheduleListResource(id = id2)
        val schedule3 = genScheduleListResource(id = id3)

        val elements = listOf(
            genUserScheduleListResource(schedule = schedule1, selected = true),
            genUserScheduleListResource(schedule = schedule2, selected = false),
            genUserScheduleListResource(schedule = schedule3, selected = true),
            mockk<ScheduleListElement>() // dummy
        )

        val result = elements.mapToScheduleIds { it.selected }
        assertThat(result, equalTo(setOf(id1, id3)))
    }

    @Test
    fun `Given elements, When mapToScheduleIdsList, Then returns filtered list of schedule ids`() {
        val id1 = ScheduleId(1)
        val id2 = ScheduleId(2)
        val id3 = ScheduleId(3)

        val schedule1 = genScheduleListResource(id = id1)
        val schedule2 = genScheduleListResource(id = id2)
        val schedule3 = genScheduleListResource(id = id3)

        val elements = listOf(
            genUserScheduleListResource(schedule = schedule1, selected = true),
            genUserScheduleListResource(schedule = schedule2, selected = false),
            genUserScheduleListResource(schedule = schedule3, selected = true),
            mockk<ScheduleListElement>() // dummy
        )

        val result = elements.mapToScheduleIdsList { it.selected }
        assertThat(result, equalTo(listOf(id1, id3)))
    }

    private fun genUserScheduleListResource(
        schedule: ScheduleListResource = genScheduleListResource(),
        completionChecked: Boolean = genBoolean(),
        selected: Boolean = genBoolean()
    ): UserScheduleListResource = UserScheduleListResource(
        schedule = schedule,
        completionChecked = completionChecked,
        selected = selected
    )
}
