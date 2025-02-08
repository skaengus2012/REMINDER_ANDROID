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
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.kotlin.tryToNonNegativeLongOrZero
import com.nlab.reminder.core.local.database.dao.ScheduleContentDTO
import com.nlab.reminder.core.local.database.dao.TriggerTimeDTO
import com.nlab.reminder.core.local.database.model.ScheduleEntity

/**
 * @author Doohyun
 */
internal fun Schedule(entity: ScheduleEntity): Schedule = Schedule(
    id = ScheduleId(entity.scheduleId),
    content = ScheduleContent(entity),
    visiblePriority = entity.visiblePriority.tryToNonNegativeLongOrZero(),
    isComplete = entity.isComplete
)

internal fun ScheduleContent(entity: ScheduleEntity): ScheduleContent = ScheduleContent(
    title = entity.title.toNonBlankString(),
    note = entity.description.tryToNonBlankStringOrNull(),
    link = entity.link.tryToNonBlankStringOrNull()?.let(::Link),
    triggerTime = entity.triggerTimeUtc?.let { utcTime ->
        TriggerTime(
            utcTime = utcTime,
            isDateOnly = requireNotNull(entity.isTriggerTimeDateOnly)
        )
    }
)

internal fun ScheduleContent.toLocalDTO() = ScheduleContentDTO(
    title = title,
    description = note,
    link = link?.rawLink,
    triggerTimeDTO = triggerTime?.let {
        TriggerTimeDTO(
            utcTime = it.utcTime,
            isDateOnly = it.isDateOnly
        )
    }
)