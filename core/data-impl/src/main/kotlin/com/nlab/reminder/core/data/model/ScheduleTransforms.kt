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

import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.local.database.model.ScheduleContentDTO
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity

/**
 * @author Doohyun
 */
internal fun Schedule(entity: ScheduleWithDetailsEntity): Schedule = Schedule(
    id = ScheduleId(entity.schedule.scheduleId),
    content = ScheduleContent(entity),
    visiblePriority = entity.schedule.visiblePriority.toNonNegativeLong(),
    isComplete = entity.schedule.isComplete
)

internal fun ScheduleContent(entity: ScheduleWithDetailsEntity): ScheduleContent = ScheduleContent(
    title = entity.schedule.title.toNonBlankString(),
    note = entity.schedule.description.tryToNonBlankStringOrNull(),
    link = entity.schedule.link.tryToNonBlankStringOrNull()?.let(::Link),
    triggerTime = with(entity.schedule) { createTriggerTimeOrNull(triggerTimeUtc, isTriggerTimeDateOnly) },
    repeat = with(entity) { createRepeatOrNull(schedule.repeatType, schedule.repeatInterval, repeatDetails) }
)

internal fun ScheduleContent.toDTO(): ScheduleContentDTO = ScheduleContentDTO(
    title = title,
    description = note,
    link = link?.rawLink,
    triggerTimeDTO = triggerTime?.toDTO(),
    repeatDTO = repeat?.toDTO()
)