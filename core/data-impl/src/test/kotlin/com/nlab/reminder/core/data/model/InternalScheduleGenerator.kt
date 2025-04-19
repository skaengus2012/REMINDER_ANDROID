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

import com.nlab.reminder.core.kotlin.collections.toSetIndexed
import com.nlab.reminder.core.local.database.model.RepeatDetailDTO
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity
import com.nlab.testkit.faker.genInt

typealias ScheduleAndEntity = Pair<Schedule, ScheduleWithDetailsEntity>

/**
 * @author Doohyun
 */
fun genScheduleAndEntity(
    schedule: Schedule = genSchedule(),
    lastRepeatId: Long = -1
): ScheduleAndEntity {
    val contentDTO = schedule.content.toDTO()
    val scheduleEntity = ScheduleEntity(
        scheduleId = schedule.id.rawId,
        title = schedule.content.title.value,
        description = schedule.content.note?.value,
        link = schedule.content.link?.rawLink?.value,
        visiblePriority = schedule.visiblePriority.value,
        isComplete = schedule.isComplete,
        triggerTimeUtc = schedule.content.triggerTime?.utcTime,
        isTriggerTimeDateOnly = schedule.content.triggerTime?.isDateOnly,
        repeatType = contentDTO.repeatDTO?.type,
        repeatInterval = contentDTO.repeatDTO?.interval?.value
    )
    val repeatDetailEntities = RepeatDetailEntities(
        repeatDetailDTOs = contentDTO.repeatDTO?.details.orEmpty(),
        scheduleId = scheduleEntity.scheduleId,
        lastRepeatId = lastRepeatId
    )
    val scheduleWithDetailsEntity = ScheduleWithDetailsEntity(scheduleEntity, repeatDetailEntities)
    return schedule to scheduleWithDetailsEntity
}

@Suppress("TestFunctionName")
private fun RepeatDetailEntities(
    repeatDetailDTOs: Iterable<RepeatDetailDTO>,
    scheduleId: Long,
    lastRepeatId: Long = -1
): Set<RepeatDetailEntity> {
    val pivotRepeatId = lastRepeatId + 1
    return repeatDetailDTOs.toSetIndexed { index, repeatDetailDTO ->
        RepeatDetailEntity(
            repeatId = index.toLong() + pivotRepeatId,
            scheduleId = scheduleId,
            propertyCode = repeatDetailDTO.propertyCode,
            value = repeatDetailDTO.value
        )
    }
}

fun genScheduleAndEntities(
    count: Int = genInt(min = 5, max = 10)
): List<ScheduleAndEntity> {
    val acc = genSchedules(count = count).fold(Pair(emptyList<ScheduleAndEntity>(), -1L)) { acc, schedule ->
        val (prevScheduleAndEntities, prevLastRepeatId) = acc
        val scheduleAndEntity = genScheduleAndEntity(schedule, prevLastRepeatId)
        val newScheduleAndEntities = prevScheduleAndEntities + scheduleAndEntity
        val newLastRepeatId = scheduleAndEntity
            .second
            .repeatDetails
            .maxOfOrNull { it.repeatId }
            ?: prevLastRepeatId
        newScheduleAndEntities to newLastRepeatId
    }
    return acc.first
}