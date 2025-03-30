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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity
import com.nlab.testkit.faker.genInt

/**
 * @author Doohyun
 */
internal fun genScheduleAndEntity(schedule: Schedule = genSchedule()): Pair<Schedule, ScheduleWithDetailsEntity> {
    val scheduleEntity = ScheduleEntity(
        scheduleId = schedule.id.rawId,
        title = schedule.content.title.value,
        description = schedule.content.note?.value,
        link = schedule.content.link?.rawLink?.value,
        visiblePriority = schedule.visiblePriority.value,
        isComplete = schedule.isComplete,
        triggerTimeUtc = schedule.content.triggerTime?.utcTime,
        isTriggerTimeDateOnly = schedule.content.triggerTime?.isDateOnly,
        repeatType = schedule.content.repeat?.toRepeatType(),
        repeatInterval = schedule.content.repeat?.toIntervalAsInt()
    )
    val repeatDetailEntity =


}
    schedule to ScheduleEntity(
        scheduleId = schedule.id.rawId,
        title = schedule.content.title.value,
        description = schedule.content.note?.value,
        link = schedule.content.link?.rawLink?.value,
        visiblePriority = schedule.visiblePriority.value,
        isComplete = schedule.isComplete,
        triggerTimeUtc = schedule.content.triggerTime?.utcTime,
        isTriggerTimeDateOnly = schedule.content.triggerTime?.isDateOnly,
        repeatType = schedule.content.repeat?.toRepeatType(),
        repeatInterval = schedule.content.repeat?.toIntervalAsInt()
    )

internal fun genScheduleAndEntities(
    count: Int = genInt(min = 5, max = 10)
): Pair<List<Schedule>, List<ScheduleEntity>> {
    val tables = genSchedules(count = count)
        .map { schedule -> genScheduleAndEntity(schedule) }
        .associateBy(keySelector = { it.first }, valueTransform = { it.second })

    return tables.keys.sortedBy { it.id.rawId } to tables.values.sortedBy { it.scheduleId }
}