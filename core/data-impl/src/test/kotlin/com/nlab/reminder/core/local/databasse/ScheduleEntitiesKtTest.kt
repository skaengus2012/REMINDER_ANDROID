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

package com.nlab.reminder.core.local.databasse

import com.nlab.reminder.core.data.local.database.toEntity
import com.nlab.reminder.core.data.local.database.toModel
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.isEmpty
import com.nlab.reminder.core.local.database.ScheduleEntity
import com.nlab.reminder.core.local.database.ScheduleEntityWithTagEntities
import com.nlab.testkit.faker.genLong
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class ScheduleEntitiesKtTest {
    @Test
    fun testToEntity() {
        val schedule = genSchedule()
        val scheduleEntity = schedule.toEntity()

        assert(schedule.id.value == scheduleEntity.scheduleId)
        assert(schedule.title == scheduleEntity.title)
        assert(schedule.note == scheduleEntity.description)
        assert(schedule.link.value == scheduleEntity.link)
        assert(schedule.visiblePriority == scheduleEntity.visiblePriority)
        assert(schedule.isComplete == scheduleEntity.isComplete)
    }

    @Test
    fun testToModel() {
        val expectedSchedule = genSchedule()
        val scheduleEntity = ScheduleEntityWithTagEntities(
            scheduleEntity = expectedSchedule.toEntity(),
            tagEntities = expectedSchedule.tags.map { it.toEntity() }
        )
        val actualSchedule = scheduleEntity.toModel()

        assertThat(actualSchedule, equalTo(expectedSchedule))
    }

    @Test
    fun testToModelWithEmptyValue() {
        val scheduleEntity = ScheduleEntityWithTagEntities(
            scheduleEntity = ScheduleEntity(
                scheduleId = genScheduleId().value,
                title = "title",
                description = null,
                link = null,
                visiblePriority = genLong(),
                isComplete = false
            ),
            tagEntities = emptyList()
        )
        val schedule = scheduleEntity.toModel()
        assert(schedule.note.isEmpty())
        assert(schedule.link.isEmpty())
        assert(schedule.tags.isEmpty())
    }
}