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

package com.nlab.reminder.internal.data.model

import com.nlab.reminder.core.data.model.isEmpty
import com.nlab.reminder.core.local.database.ScheduleEntity
import com.nlab.reminder.core.local.database.ScheduleEntityWithTagEntities
import org.junit.Test

/**
 * @author thalys
 */
internal class ScheduleEntitiesTest {
    @Test
    fun testNullToEmptyWhenToModel() {
        val scheduleEntity = ScheduleEntityWithTagEntities(
            scheduleEntity = ScheduleEntity(
                scheduleId = 1,
                title = "title",
                description = null,
                link = null,
                visiblePriority = 1,
                isComplete = false
            ),
            tagEntities = emptyList()
        )
        val schedule = scheduleEntity.toModel()
        assert(schedule.note.isEmpty())
        assert(schedule.link.isEmpty())
    }
}