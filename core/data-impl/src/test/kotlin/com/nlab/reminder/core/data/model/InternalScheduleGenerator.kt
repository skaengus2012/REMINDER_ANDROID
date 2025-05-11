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

import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.collections.toSetIndexed
import com.nlab.reminder.core.local.database.entity.RepeatDetailEntity
import com.nlab.reminder.core.local.database.entity.ScheduleEntity
import com.nlab.reminder.core.local.database.entity.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.transaction.ScheduleRepeatDetailAggregate
import com.nlab.testkit.faker.genInt

typealias ScheduleAndEntity = Pair<Schedule, ScheduleCompositeEntity>

/**
 * @author Doohyun
 */
data class ScheduleCompositeEntity(
    val scheduleEntity: ScheduleEntity,
    val scheduleTagListEntities: Set<ScheduleTagListEntity>,
    val repeatDetailEntities: Set<RepeatDetailEntity>,
)

fun genScheduleAndEntity(
    schedule: Schedule = genSchedule(),
    lastRepeatId: Long = -1
): ScheduleAndEntity {
    val aggregate = schedule.content.toAggregate()
    val scheduleEntity = ScheduleEntity(
        scheduleId = schedule.id.rawId,
        title = aggregate.headline.title.value,
        description = aggregate.headline.description?.value,
        link = aggregate.headline.link?.value,
        visiblePriority = schedule.visiblePriority.value,
        isComplete = schedule.isComplete,
        triggerTimeUtc = aggregate.timing?.triggerTimeUtc,
        isTriggerTimeDateOnly = aggregate.timing?.isTriggerTimeDateOnly,
        repeatType = aggregate.timing?.repeat?.type,
        repeatInterval = aggregate.timing?.repeat?.interval?.value
    )
    val repeatDetailEntities = RepeatDetailEntities(
        scheduleAggregateRepeatDetailAggregates = aggregate.timing?.repeat?.details.orEmpty(),
        scheduleId = scheduleEntity.scheduleId,
        lastRepeatId = lastRepeatId
    )
    val scheduleTagListEntities = schedule.content.tagIds.toSet { tagId ->
        ScheduleTagListEntity(
            scheduleId = schedule.id.rawId,
            tagId = tagId.rawId
        )
    }
    val scheduleWithDetailEntity = ScheduleCompositeEntity(
        scheduleEntity = scheduleEntity,
        scheduleTagListEntities = scheduleTagListEntities,
        repeatDetailEntities = repeatDetailEntities
    )
    return schedule to scheduleWithDetailEntity
}

@Suppress("TestFunctionName")
private fun RepeatDetailEntities(
    scheduleAggregateRepeatDetailAggregates: Iterable<ScheduleRepeatDetailAggregate>,
    scheduleId: Long,
    lastRepeatId: Long = -1
): Set<RepeatDetailEntity> {
    val pivotRepeatId = lastRepeatId + 1
    return scheduleAggregateRepeatDetailAggregates.toSetIndexed { index, aggregate ->
        RepeatDetailEntity(
            repeatId = index.toLong() + pivotRepeatId,
            scheduleId = scheduleId,
            propertyCode = aggregate.propertyCode,
            value = aggregate.value
        )
    }
}

fun genScheduleAndEntities(
    count: Int = genInt(min = 5, max = 10)
): Set<ScheduleAndEntity> {
    val acc = genSchedules(count = count).fold(Pair(emptyList<ScheduleAndEntity>(), -1L)) { acc, schedule ->
        val (prevScheduleAndEntities, prevLastRepeatId) = acc
        val scheduleAndEntity = genScheduleAndEntity(schedule, prevLastRepeatId)
        val newScheduleAndEntities = prevScheduleAndEntities + scheduleAndEntity
        val newLastRepeatId = scheduleAndEntity
            .second
            .repeatDetailEntities
            .maxOfOrNull { it.repeatId }
            ?: prevLastRepeatId
        newScheduleAndEntities to newLastRepeatId
    }
    return acc.first.toSet()
}