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

import com.nlab.testkit.faker.genBoolean
import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class IsScheduleListResourceChangedUseCaseTest {

    @Test
    fun `Given same schedules, When invoked, Then returns false`() {
        val schedule1 = genScheduleListResource()
        val schedule2 = genScheduleListResource()

        val oldElements = listOf(
            genUserScheduleListResource(
                schedule = schedule1,
                selected = false,
                completionChecked = false
            ),
            genUserScheduleListResource(
                schedule = schedule2,
                selected = false,
                completionChecked = false
            )
        )
        val newElements = listOf(
            genUserScheduleListResource(
                schedule = schedule1,
                selected = true,
                completionChecked = true
            ),
            genUserScheduleListResource(
                schedule = schedule2,
                selected = true,
                completionChecked = false
            )
        )

        val useCase = IsScheduleListResourceChangedUseCase()
        val result = useCase(oldElements, newElements)

        assertThat(result, equalTo(false))
    }

    @Test
    fun `Given different schedules, When invoked, Then returns true`() {
        val schedule1 = genScheduleListResource()
        val schedule2 = genScheduleListResource()
        val schedule3 = genScheduleListResource()

        val oldElements = listOf(
            genUserScheduleListResource(schedule = schedule1),
            genUserScheduleListResource(schedule = schedule2)
        )
        val newElements = listOf(
            genUserScheduleListResource(schedule = schedule1),
            genUserScheduleListResource(schedule = schedule3)
        )

        val useCase = IsScheduleListResourceChangedUseCase()
        val result = useCase(oldElements, newElements)

        assertThat(result, equalTo(true))
    }

    @Test
    fun `Given dummy element, When invoked, Then returns false`() {
        val schedule = genScheduleListResource()
        val dummyElement: ScheduleListElement = mockk()

        val oldElements = listOf(
            genUserScheduleListResource(schedule = schedule)
        )
        val newElements = listOf(
            genUserScheduleListResource(schedule = schedule),
            dummyElement
        )

        val useCase = IsScheduleListResourceChangedUseCase()
        val result = useCase(oldElements, newElements)

        assertThat(result, equalTo(false))
    }
}

private fun genUserScheduleListResource(
    schedule: ScheduleListResource,
    completionChecked: Boolean = genBoolean(),
    selected: Boolean = genBoolean()
): UserScheduleListResource = UserScheduleListResource(
    schedule = schedule,
    completionChecked = completionChecked,
    selected = selected
)
