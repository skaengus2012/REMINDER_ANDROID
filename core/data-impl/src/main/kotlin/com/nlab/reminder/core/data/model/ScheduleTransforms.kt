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
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity

/**
 * @author Doohyun
 */
internal fun Schedule(entity: ScheduleWithDetailsEntity): Schedule = Schedule(
    id = ScheduleId(entity.schedule.scheduleId),
    title = entity.schedule.title.toNonBlankString(),
    note = entity.schedule.description.tryToNonBlankStringOrNull(),
    link = entity.schedule.link.tryToNonBlankStringOrNull()?.let(::Link),
    triggerTime = entity.schedule.triggerTimeUtc?.let { utcTime ->
        TriggerTime(
            utcTime = utcTime,
            isDateOnly = requireNotNull(entity.schedule.isTriggerTimeDateOnly)
        )
    },
    repeat = entity.schedule.repeatType?.let { type ->
        Repeat(
            type = type,
            interval = requireNotNull(entity.schedule.repeatInterval),
            detailEntities = entity.repeatDetails
        )
    },
    visiblePriority = entity.schedule.visiblePriority.tryToNonNegativeLongOrZero(),
    isComplete = entity.schedule.isComplete
)