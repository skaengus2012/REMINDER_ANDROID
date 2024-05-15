/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.schedule.model

import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class ScheduleElementsKtTest {
    @Test
    fun `Given correct position, When findPosition, Then return scheduleId`() {
        val position = genInt(min = 2, max = 4)
        val scheduleElements = genScheduleElements(position + 5)

        assertThat(scheduleElements.findId(position), equalTo(scheduleElements[position].id))
    }

    @Test
    fun `Given wrong position, When findPosition, Then return null`() {
        val position = genInt(min = 2, max = 4)
        val scheduleElements = genScheduleElements(position - 2)

        assertThat(scheduleElements.findId(position), equalTo(null))
    }

    @Test
    fun `Given selected including items, When getSelectedIds, Then return ids`() {
        val size = genInt(min = 5, max = 10)
        val selectedItems = List(size) { index ->
            com.nlab.reminder.core.schedule.model.genScheduleElement(
                schedule = genSchedule(scheduleId = genScheduleId(index.toLong())),
                isSelected = index % 2 == 0
            )
        }

        assertThat(
            selectedItems.getSelectedIds(),
            equalTo(buildList { for (i in 0 until size step 2) add(genScheduleId(i.toLong())) })
        )
    }
}