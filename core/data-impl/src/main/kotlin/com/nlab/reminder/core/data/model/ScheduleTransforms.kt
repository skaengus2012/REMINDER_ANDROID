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
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.RepeatType
import com.nlab.reminder.core.local.database.model.ScheduleContentDTO
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.model.ScheduleTimingDTO

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
    val triggerTimeUtc = scheduleEntity.triggerTimeUtc
    val isTriggerTimeDateOnly = scheduleEntity.isTriggerTimeDateOnly
    require((triggerTimeUtc == null) == (isTriggerTimeDateOnly == null)) {
        "Invalid triggerTime [$triggerTimeUtc, $isTriggerTimeDateOnly]"
    }

    val repeat = createRepeatOrNull(
        type = scheduleEntity.repeatType,
        interval = scheduleEntity.repeatInterval,
        detailEntities = repeatDetailEntity
    )
    require(triggerTimeUtc != null || repeat == null) {
        "Repeat is defined without triggerTime: repeat=$repeat"
    }

    return if (triggerTimeUtc == null) null else {
        ScheduleTiming(
            triggerAtUtc = triggerTimeUtc,
            isTriggerAtDateOnly = isTriggerTimeDateOnly!!,
            repeat = repeat
        )
    }
}

/**
 * If RepeatDetails is incorrectly entered, it is not confirmed.
 * In Repeat function, check if there are appropriate repeatDetails.
 *
 * The exactly validity of the repeatDetails should be checked only in the data insertion.
 * @see [com.nlab.reminder.core.local.database.transaction.ScheduleContentValidator]
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

internal fun ScheduleContent.toDTO(): ScheduleContentDTO = ScheduleContentDTO(
    title = title,
    description = note,
    link = link?.rawLink,
    tagIds = tagIds.toSet { it.rawId },
    timingDTO = timing?.toDTO(),
)

internal fun ScheduleTiming.toDTO(): ScheduleTimingDTO = ScheduleTimingDTO(
    triggerTimeUtc = triggerAtUtc,
    isTriggerTimeDateOnly = isTriggerAtDateOnly,
    repeatDTO = repeat?.toDTO()
)