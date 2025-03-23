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

import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.toSetNotNull
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.kotlin.tryToNonNegativeLongOrZero
import com.nlab.reminder.core.local.database.model.REPEAT_FREQUENCY_DAILY
import com.nlab.reminder.core.local.database.model.REPEAT_FREQUENCY_HOURLY
import com.nlab.reminder.core.local.database.model.REPEAT_FREQUENCY_MONTHLY
import com.nlab.reminder.core.local.database.model.REPEAT_FREQUENCY_SETTING_WEEKLY
import com.nlab.reminder.core.local.database.model.REPEAT_FREQUENCY_SETTING_ZONE_ID
import com.nlab.reminder.core.local.database.model.REPEAT_FREQUENCY_WEEKLY
import com.nlab.reminder.core.local.database.model.REPEAT_WEEK_FRI
import com.nlab.reminder.core.local.database.model.REPEAT_WEEK_MON
import com.nlab.reminder.core.local.database.model.REPEAT_WEEK_SAT
import com.nlab.reminder.core.local.database.model.REPEAT_WEEK_SUN
import com.nlab.reminder.core.local.database.model.REPEAT_WEEK_THU
import com.nlab.reminder.core.local.database.model.REPEAT_WEEK_TUE
import com.nlab.reminder.core.local.database.model.REPEAT_WEEK_WED
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.RepeatFrequency
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

/**
 * @author Doohyun
 */
internal fun Schedule(
    scheduleEntity: ScheduleEntity,
    repeatDetailEntities: Set<RepeatDetailEntity>,
    tagIds: Set<TagId>,
): Schedule = Schedule(
    id = ScheduleId(scheduleEntity.scheduleId),
    content = ScheduleContent(
        scheduleEntity = scheduleEntity,
        repeatDetailEntities = repeatDetailEntities,
        tagIds = tagIds
    ),
    visiblePriority = scheduleEntity.visiblePriority.tryToNonNegativeLongOrZero(),
    isComplete = scheduleEntity.isComplete
)

internal fun ScheduleContent(
    scheduleEntity: ScheduleEntity,
    repeatDetailEntities: Set<RepeatDetailEntity>,
    tagIds: Set<TagId>,
): ScheduleContent = ScheduleContent(
    title = scheduleEntity.title.toNonBlankString(),
    note = scheduleEntity.description.tryToNonBlankStringOrNull(),
    link = scheduleEntity.link.tryToNonBlankStringOrNull()?.let(::Link),
    triggerTime = scheduleEntity.triggerTimeUtc?.let { utcTime ->
        TriggerTime(
            utcTime = utcTime,
            isDateOnly = requireNotNull(scheduleEntity.isTriggerTimeDateOnly)
        )
    },
    repeat = scheduleEntity.repeatFrequency?.let { frequency ->
        Repeat(
            repeatFrequency = frequency,
            repeatFrequencyValue = requireNotNull(scheduleEntity.repeatFrequencyValue),
            repeatDetailEntities = repeatDetailEntities
        )
    },
    tagIds = tagIds
)

internal fun Repeat(
    @RepeatFrequency repeatFrequency: String,
    repeatFrequencyValue: Int,
    repeatDetailEntities: Set<RepeatDetailEntity>,
): Repeat {
    val frequency = repeatFrequencyValue.toPositiveInt()
    when (repeatFrequency) {
        REPEAT_FREQUENCY_HOURLY -> {
            Repeat.Hourly(frequency)
        }
        REPEAT_FREQUENCY_DAILY -> {
            Repeat.Daily(frequency)
        }
        REPEAT_FREQUENCY_WEEKLY -> {
            parseRepeatWeekly(frequency, repeatDetailEntities)
        }
        REPEAT_FREQUENCY_MONTHLY -> {

        }
    }
}

private fun Set<RepeatDetailEntity>.requireTimeZone(): TimeZone {
    val zoneId = requireNotNull(find { it.frequencySetting == REPEAT_FREQUENCY_SETTING_ZONE_ID }).value
    return TimeZone.of(zoneId)
}

private fun parseRepeatWeekly(
    frequency: PositiveInt,
    repeatDetailEntities: Set<RepeatDetailEntity>,
): Repeat.Weekly = Repeat.Weekly(
    frequency,
    timeZone = repeatDetailEntities.requireTimeZone(),
    daysOfWeeks = repeatDetailEntities.toSetNotNull { entity ->
        if (entity.frequencySetting != REPEAT_FREQUENCY_SETTING_WEEKLY) null
        else when (val value = entity.value) {
            REPEAT_WEEK_SUN -> DayOfWeek.SUNDAY
            REPEAT_WEEK_MON -> DayOfWeek.MONDAY
            REPEAT_WEEK_TUE -> DayOfWeek.TUESDAY
            REPEAT_WEEK_WED -> DayOfWeek.WEDNESDAY
            REPEAT_WEEK_THU -> DayOfWeek.THURSDAY
            REPEAT_WEEK_FRI -> DayOfWeek.FRIDAY
            REPEAT_WEEK_SAT -> DayOfWeek.SATURDAY
            else -> error("Invalid day of week type ${value}")
        }
    }
)

private fun parseRepeatMonthly(
    frequency: PositiveInt,
    repeatDetailEntities: Set<RepeatDetailEntity>,
): Repeat.Monthly = Repeat.Monthly(
    frequency,
    timeZone = repeatDetailEntities.
)

/**
internal fun ScheduleContent.toLocalDTO() = ScheduleContentDTO(
    title = title,
    description = note,
    link = link?.rawLink,
    triggerTimeDTO = triggerTime?.let {
        TriggerTimeDTO(
            utcTime = it.utcTime,
            isDateOnly = it.isDateOnly
        )
    },
    frequencyDTO = null // TODO fill frequency DTO
)*/