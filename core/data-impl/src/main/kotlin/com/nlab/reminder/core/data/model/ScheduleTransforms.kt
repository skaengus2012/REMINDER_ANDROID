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

import androidx.annotation.IntRange
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.local.database.dao.ScheduleHeadlineSaveInput
import com.nlab.reminder.core.local.database.entity.RepeatDetailEntity
import com.nlab.reminder.core.local.database.entity.RepeatType
import com.nlab.reminder.core.local.database.entity.ScheduleEntity
import com.nlab.reminder.core.local.database.entity.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.transaction.ScheduleContentAggregate
import com.nlab.reminder.core.local.database.transaction.ScheduleTimingAggregate

/**
 * @author Doohyun
 */
internal fun Schedule(
    scheduleEntity: ScheduleEntity,
    scheduleTagListEntities: Set<ScheduleTagListEntity>,
    repeatDetailEntities: Set<RepeatDetailEntity>,
): Schedule = Schedule(
    id = ScheduleId(scheduleEntity.scheduleId),
    content = ScheduleContent(
        scheduleEntity,
        scheduleTagListEntities,
        repeatDetailEntities
    ),
    visiblePriority = scheduleEntity.visiblePriority.toNonNegativeLong(),
    isComplete = scheduleEntity.isComplete
)

private fun ScheduleContent(
    scheduleEntity: ScheduleEntity,
    scheduleTagListEntities: Set<ScheduleTagListEntity>,
    repeatDetailEntity: Set<RepeatDetailEntity>,
): ScheduleContent = ScheduleContent(
    title = scheduleEntity.title.toNonBlankString(),
    note = scheduleEntity.description.tryToNonBlankStringOrNull(),
    link = scheduleEntity.link.tryToNonBlankStringOrNull()?.let(::Link),
    tagIds = scheduleTagListEntities.toSet { TagId(it.tagId) },
    timing = createScheduleTimingOrNull(scheduleEntity, repeatDetailEntity),
)

private fun createScheduleTimingOrNull(
    scheduleEntity: ScheduleEntity,
    repeatDetailEntity: Set<RepeatDetailEntity>,
): ScheduleTiming? {
    val triggerAt = scheduleEntity.triggerAt
    val isTriggerAtDateOnly = scheduleEntity.isTriggerAtDateOnly
    require((triggerAt == null) == (isTriggerAtDateOnly == null)) {
        "Invalid triggerAt [$triggerAt, $isTriggerAtDateOnly]"
    }

    val repeat = createRepeatOrNull(
        type = scheduleEntity.repeatType,
        interval = scheduleEntity.repeatInterval,
        detailEntities = repeatDetailEntity
    )
    require(isTriggerAtDateOnly != null || repeat == null) {
        "Repeat is defined without triggerAt: repeat=$repeat"
    }
    return if (triggerAt == null) null else {
        if (isTriggerAtDateOnly!!) {
            require(repeat == null || repeat is DateOnlyRepeat) {
                "Repeat is not DateOnlyRepeat: repeat=$repeat, but trigger is dateOnly"
            }
            ScheduleTiming.Date(
                triggerAt = triggerAt,
                dateOnlyRepeat = repeat
            )
        } else {
            ScheduleTiming.DateTime(
                triggerAt = triggerAt,
                repeat = repeat
            )
        }
    }
}

/**
 * If RepeatDetails is incorrectly entered, it is not confirmed.
 * In Repeat function, check if there are appropriate repeatDetails.
 *
 * The exactly validity of the repeatDetails should be checked only in the data insertion.
 * @see [com.nlab.reminder.core.local.database.transaction.ScheduleTimingAggregateValidator]
 */
private fun createRepeatOrNull(
    @RepeatType type: String?,
    @IntRange(from = 1) interval: Int?,
    detailEntities: Collection<RepeatDetailEntity>
): Repeat? {
    require((type == null) == (interval == null)) {
        "Invalid RepeatDetails [$type, $detailEntities]"
    }
    require(type != null || detailEntities.isEmpty()) {
        "Repeat Detail is defined without repeatType: detailEntities=$detailEntities"
    }

    return if (type == null) null else Repeat(type, interval!!, detailEntities)
}

internal fun ScheduleContent.toHeadlineSaveInput(): ScheduleHeadlineSaveInput = ScheduleHeadlineSaveInput(
    title = title,
    description = note,
    link = link?.rawLink
)

internal fun ScheduleContent.toAggregate(): ScheduleContentAggregate = ScheduleContentAggregate(
    headline = toHeadlineSaveInput(),
    timing = timing?.toAggregate(),
    tagIds = tagIds.toSet { it.rawId },
)

internal fun ScheduleTiming.toAggregate(): ScheduleTimingAggregate {
    val isTriggerAtDateOnly: Boolean
    val repeatData: Repeat?
    when (this) {
        is ScheduleTiming.Date -> {
            isTriggerAtDateOnly = true
            repeatData = dateOnlyRepeat
        }
        is ScheduleTiming.DateTime -> {
            isTriggerAtDateOnly = false
            repeatData = repeat
        }
    }
    return ScheduleTimingAggregate(
        triggerAt = triggerAt,
        isTriggerAtDateOnly = isTriggerAtDateOnly,
        repeat = repeatData?.toAggregate()
    )
}